package com.sunflower.pinpic.sync.service;

import com.sunflower.pinpic.device.repository.DeviceRepository;
import com.sunflower.pinpic.photo.entity.Cluster;
import com.sunflower.pinpic.photo.entity.Location;
import com.sunflower.pinpic.photo.entity.Photo;
import com.sunflower.pinpic.photo.entity.Visibility;
import com.sunflower.pinpic.photo.repository.ClusterRepository;
import com.sunflower.pinpic.photo.repository.PhotoRepository;
import com.sunflower.pinpic.storage.S3Service;
import com.sunflower.pinpic.sync.dto.*;
import com.sunflower.pinpic.sync.entity.CloudSyncOperation;
import com.sunflower.pinpic.sync.repository.CloudSyncOperationRepository;
import com.sunflower.pinpic.user.entity.User;
import com.sunflower.pinpic.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {

    private final UserRepository userRepository;
    private final ClusterRepository clusterRepository;
    private final PhotoRepository photoRepository;
    private final DeviceRepository deviceRepository;
    private final CloudSyncOperationRepository cloudSyncOperationRepository;
    private final S3Service s3Service;

    @Transactional
    public SyncBatchResponseDto processBatch(SyncBatchRequestDto request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        if (request.getDeviceId() != null && !request.getDeviceId().isBlank()) {
            deviceRepository.findById(request.getDeviceId()).ifPresent(device -> {
                device.setLastSeenAt(Instant.now().toString());
                deviceRepository.save(device);
            });
        }

        List<SyncResultDto> results = new ArrayList<>();

        if (request.getOperations() == null || request.getOperations().isEmpty()) {
            return new SyncBatchResponseDto(results);
        }

        for (SyncItemDto item : request.getOperations()) {
            SyncResultDto result = processSingleOperation(item, user, request.getDeviceId());
            results.add(result);
        }

        return new SyncBatchResponseDto(results);
    }

    private SyncResultDto processSingleOperation(SyncItemDto item, User user, String deviceId) {
        String entityType = item.getEntityType() != null ? item.getEntityType().toUpperCase() : "";
        String operation = item.getOperation() != null ? item.getOperation().toUpperCase() : "";

        try {
            String globalId = null;

            if ("CLUSTER".equals(entityType)) {
                globalId = processClusterOperation(item, operation, user);
                recordSyncOperation(user.getId(), deviceId, "CLUSTER", operation, globalId);
            } else if ("PHOTOS".equals(entityType)) {
                globalId = processPhotoOperation(item, operation, user);
                recordSyncOperation(user.getId(), deviceId, "PHOTOS", operation, globalId);
            } else {
                return SyncResultDto.builder()
                        .operationId(item.getOperationId())
                        .localEntityId(item.getLocalEntityId())
                        .entityType(item.getEntityType())
                        .status("FAILED")
                        .errorMessage("Unknown entity type: " + entityType)
                        .build();
            }

            return SyncResultDto.builder()
                    .operationId(item.getOperationId())
                    .localEntityId(item.getLocalEntityId())
                    .entityType(item.getEntityType())
                    .globalId(globalId)
                    .status("SUCCESS")
                    .build();

        } catch (Exception e) {
            log.error("Failed to process sync operation: {}", item.getOperationId(), e);
            return SyncResultDto.builder()
                    .operationId(item.getOperationId())
                    .localEntityId(item.getLocalEntityId())
                    .entityType(item.getEntityType())
                    .status("FAILED")
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    private void recordSyncOperation(String userId, String deviceId, String entityType, String operation, String entityId) {
        try {
            cloudSyncOperationRepository.save(CloudSyncOperation.builder()
                    .userId(userId)
                    .deviceId(deviceId)
                    .entityType(entityType)
                    .operation(operation)
                    .entityId(entityId)
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Failed to record sync operation for entityId: {}", entityId, e);
        }
    }

    @Transactional(readOnly = true)
    public SyncPullResponseDto pullOperations(String userEmail, String deviceId, Long lastOperationId, int batchSize) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        long sinceId = (lastOperationId != null) ? lastOperationId : 0L;
        int limit = (batchSize > 0) ? batchSize : 50;

        List<CloudSyncOperation> operations = cloudSyncOperationRepository.findOperationsToPull(
                user.getId(),
                sinceId,
                deviceId,
                PageRequest.of(0, limit)
        );

        List<SyncPullItemDto> items = new ArrayList<>();
        long newLastOpId = sinceId;

        for (CloudSyncOperation op : operations) {
            newLastOpId = Math.max(newLastOpId, op.getOperationId());

            SyncPullItemDto.SyncPullItemDtoBuilder itemBuilder = SyncPullItemDto.builder()
                    .operationId(op.getOperationId())
                    .entityType(op.getEntityType())
                    .operation(op.getOperation())
                    .entityId(op.getEntityId())
                    .deviceId(op.getDeviceId());

            if ("PHOTOS".equalsIgnoreCase(op.getEntityType())) {
                if ("DELETE".equalsIgnoreCase(op.getOperation())) {
                    items.add(itemBuilder.build());
                    continue;
                }

                Photo photo = photoRepository.findById(op.getEntityId()).orElse(null);
                if (photo == null || Boolean.TRUE.equals(photo.getIsDeleted())) {
                    items.add(itemBuilder.operation("DELETE").build());
                    continue;
                }

                PhotoSyncDto photoDto = PhotoSyncDto.builder()
                        .globalPhotoId(photo.getPhotoId())
                        .globalClusterId(photo.getClusterId())
                        .name(photo.getName())
                        .s3Key(photo.getS3Key())
                        .takenAt(photo.getTakenAt() != null ? photo.getTakenAt().toString() : null)
                        .editedAt(photo.getEditedAt() != null ? photo.getEditedAt().toString() : null)
                        .location(photo.getLocation())
                        .visibility(photo.getVisibility())
                        .size(photo.getSize())
                        .build();

                itemBuilder.photo(photoDto);

                if (photo.getS3Key() != null && !photo.getS3Key().isBlank()) {
                    try {
                        String downloadUrl = s3Service.getDownloadPresignedUrl(photo.getS3Key());
                        itemBuilder.downloadUrl(downloadUrl);
                    } catch (Exception e) {
                        log.warn("Failed to generate download presigned URL for s3Key: {}", photo.getS3Key(), e);
                    }
                }
            } else if ("CLUSTER".equalsIgnoreCase(op.getEntityType())) {
                if ("DELETE".equalsIgnoreCase(op.getOperation())) {
                    items.add(itemBuilder.build());
                    continue;
                }

                Cluster cluster = clusterRepository.findById(op.getEntityId()).orElse(null);
                if (cluster == null) {
                    items.add(itemBuilder.operation("DELETE").build());
                    continue;
                }

                ClusterSyncDto clusterDto = ClusterSyncDto.builder()
                        .globalClusterId(cluster.getClusterId())
                        .location(cluster.getLocation())
                        .thumbnailPhotoGlobalId(cluster.getThumbnailPhotoId())
                        .photoCount(cluster.getPhotoCount())
                        .build();

                itemBuilder.cluster(clusterDto);
            }

            items.add(itemBuilder.build());
        }

        boolean hasMore = operations.size() == limit;

        return SyncPullResponseDto.builder()
                .operations(items)
                .lastOperationId(newLastOpId)
                .hasMore(hasMore)
                .build();
    }

    private String processClusterOperation(SyncItemDto item, String operation, User user) {
        ClusterSyncDto data = item.getCluster();
        LocalDateTime now = LocalDateTime.now();

        if ("ADD".equals(operation) || "UPDATE".equals(operation)) {
            String clusterId = data != null ? data.getGlobalClusterId() : null;
            Cluster cluster = null;

            if (clusterId != null && !clusterId.isBlank()) {
                cluster = clusterRepository.findById(clusterId).orElse(null);
            }

            if (cluster != null) {
                if (!user.getId().equals(cluster.getUserId())) {
                    throw new IllegalArgumentException("Unauthorized to modify cluster " + clusterId);
                }
                if (data.getLocation() != null) cluster.setLocation(data.getLocation());
                if (data.getThumbnailPhotoGlobalId() != null) cluster.setThumbnailPhotoId(data.getThumbnailPhotoGlobalId());
                if (data.getPhotoCount() != null) cluster.setPhotoCount(data.getPhotoCount());
                cluster.setUpdatedAt(now);
                cluster = clusterRepository.save(cluster);
                return cluster.getClusterId();
            } else {
                String newId = (clusterId != null && !clusterId.isBlank()) ? clusterId : UUID.randomUUID().toString();
                cluster = Cluster.builder()
                        .clusterId(newId)
                        .userId(user.getId())
                        .location(data != null ? data.getLocation() : null)
                        .thumbnailPhotoId(data != null ? data.getThumbnailPhotoGlobalId() : null)
                        .photoCount(data != null && data.getPhotoCount() != null ? data.getPhotoCount() : 0)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                cluster = clusterRepository.save(cluster);
                return cluster.getClusterId();
            }
        } else if ("DELETE".equals(operation)) {
            String clusterId = data != null ? data.getGlobalClusterId() : null;
            if (clusterId != null && !clusterId.isBlank()) {
                Cluster cluster = clusterRepository.findById(clusterId).orElse(null);
                if (cluster != null && user.getId().equals(cluster.getUserId())) {
                    clusterRepository.delete(cluster);
                }
            }
            return clusterId;
        }

        throw new IllegalArgumentException("Unsupported cluster operation: " + operation);
    }

    private String processPhotoOperation(SyncItemDto item, String operation, User user) {
        PhotoSyncDto data = item.getPhoto();
        LocalDateTime now = LocalDateTime.now();

        if ("ADD".equals(operation) || "UPDATE".equals(operation)) {
            String photoId = data != null ? data.getGlobalPhotoId() : null;
            String s3Key = data != null ? data.getS3Key() : null;

            Photo photo = null;
            if (photoId != null && !photoId.isBlank()) {
                photo = photoRepository.findById(photoId).orElse(null);
            } else if (s3Key != null && !s3Key.isBlank()) {
                photo = photoRepository.findByS3Key(s3Key).orElse(null);
            }

            if (photo != null) {
                if (!user.getId().equals(photo.getUserId())) {
                    throw new IllegalArgumentException("Unauthorized to modify photo: " + photo.getPhotoId());
                }
                if (data.getName() != null) photo.setName(data.getName());
                if (data.getGlobalClusterId() != null) photo.setClusterId(data.getGlobalClusterId());
                if (data.getS3Key() != null) photo.setS3Key(data.getS3Key());
                if (data.getLocation() != null) photo.setLocation(data.getLocation());
                if (data.getVisibility() != null) photo.setVisibility(data.getVisibility());
                if (data.getEditedAt() != null) photo.setEditedAt(parseDateTime(data.getEditedAt()));
                photo = photoRepository.save(photo);
                return photo.getPhotoId();
            } else {
                String newId = (photoId != null && !photoId.isBlank()) ? photoId : UUID.randomUUID().toString();
                photo = Photo.builder()
                        .photoId(newId)
                        .userId(user.getId())
                        .clusterId(data != null ? data.getGlobalClusterId() : null)
                        .name(data != null ? data.getName() : "Untitled")
                        .s3Key(s3Key)
                        .takenAt(data != null ? parseDateTime(data.getTakenAt()) : now)
                        .uploadedAt(now)
                        .editedAt(data != null ? parseDateTime(data.getEditedAt()) : now)
                        .location(data != null ? data.getLocation() : null)
                        .visibility(data != null && data.getVisibility() != null ? data.getVisibility() : Visibility.PRIVATE)
                        .size(data != null ? data.getSize() : null)
                        .isDeleted(false)
                        .build();
                photo = photoRepository.save(photo);
                return photo.getPhotoId();
            }
        } else if ("DELETE".equals(operation)) {
            String photoId = data != null ? data.getGlobalPhotoId() : null;
            if (photoId != null && !photoId.isBlank()) {
                Photo photo = photoRepository.findById(photoId).orElse(null);
                if (photo != null && user.getId().equals(photo.getUserId())) {
                    photo.setIsDeleted(true);
                    photoRepository.save(photo);
                }
            }
            return photoId;
        }

        throw new IllegalArgumentException("Unsupported photo operation: " + operation);
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception ignored) {
        }
        try {
            return LocalDate.parse(dateStr).atStartOfDay();
        } catch (Exception ignored) {
        }
        try {
            return Instant.parse(dateStr).atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception ignored) {
        }
        return LocalDateTime.now();
    }
}

