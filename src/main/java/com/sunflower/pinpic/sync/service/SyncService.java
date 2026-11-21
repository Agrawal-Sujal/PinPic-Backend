package com.sunflower.pinpic.sync.service;

import com.sunflower.pinpic.device.repository.DeviceRepository;
import com.sunflower.pinpic.photo.entity.Cluster;
import com.sunflower.pinpic.photo.entity.Photo;
import com.sunflower.pinpic.photo.entity.Visibility;
import com.sunflower.pinpic.photo.repository.ClusterRepository;
import com.sunflower.pinpic.photo.repository.PhotoRepository;
import com.sunflower.pinpic.storage.S3Service;
import com.sunflower.pinpic.sync.dto.*;
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
    private final S3Service s3Service;
    private final jakarta.persistence.EntityManager entityManager;

    @Transactional
    public SyncPushResponseDto pushMutations(SyncPushRequestDto request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        String deviceId = request.getDeviceId();
        if (deviceId != null && !deviceId.isBlank()) {
            deviceRepository.findById(deviceId).ifPresent(device -> {
                device.setLastSeenAt(Instant.now().toString());
                deviceRepository.save(device);
            });
        }

        try {
            entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(hashtext(:lockKey))")
                    .setParameter("lockKey", user.getId())
                    .getSingleResult();
        } catch (Exception ignored) {
            // Lock may fail if in unit tests or non-PostgreSQL DB
        }

        LocalDateTime now = LocalDateTime.now();

        // 1. Process Clusters
        if (request.getClusters() != null) {
            for (ClusterSyncDto dto : request.getClusters()) {
                String clusterId = dto.getGlobalClusterId();
                if (clusterId == null || clusterId.isBlank()) {
                    clusterId = UUID.randomUUID().toString();
                }

                Cluster cluster = clusterRepository.findById(clusterId).orElse(null);
                if (cluster != null) {
                    if (!user.getId().equals(cluster.getUserId())) {
                        throw new IllegalArgumentException("Unauthorized to modify cluster " + clusterId);
                    }
                } else {
                    cluster = Cluster.builder()
                            .clusterId(clusterId)
                            .userId(user.getId())
                            .createdAt(now)
                            .build();
                }

                cluster.setLastModifiedDeviceId(deviceId);
                cluster.setUpdatedAt(now);

                if (Boolean.TRUE.equals(dto.getIsDeleted())) {
                    cluster.setDeletedAt(now);
                } else {
                    cluster.setDeletedAt(null);
                    if (dto.getLocation() != null) cluster.setLocation(dto.getLocation());
                    if (dto.getThumbnailPhotoGlobalId() != null) cluster.setThumbnailPhotoId(dto.getThumbnailPhotoGlobalId());
                    if (dto.getPhotoCount() != null) cluster.setPhotoCount(dto.getPhotoCount());
                    if (dto.getPublicPhotoCount() != null) cluster.setPublicPhotoCount(dto.getPublicPhotoCount());
                    if (dto.getPrivatePhotoCount() != null) cluster.setPrivatePhotoCount(dto.getPrivatePhotoCount());
                    if (dto.getFriendPhotoCount() != null) cluster.setFriendPhotoCount(dto.getFriendPhotoCount());
                    if (dto.getVisibility() != null) {
                        cluster.setVisibility(dto.getVisibility());
                    } else {
                        cluster.updateVisibility();
                    }
                }
                clusterRepository.save(cluster);
            }
        }

        // 2. Process Photos
        if (request.getPhotos() != null) {
            for (PhotoSyncDto dto : request.getPhotos()) {
                String photoId = dto.getGlobalPhotoId();
                if (photoId == null || photoId.isBlank()) {
                    photoId = UUID.randomUUID().toString();
                }

                Photo photo = photoRepository.findById(photoId).orElse(null);
                if (photo != null) {
                    if (!user.getId().equals(photo.getUserId())) {
                        throw new IllegalArgumentException("Unauthorized to modify photo " + photoId);
                    }
                } else {
                    photo = Photo.builder()
                            .photoId(photoId)
                            .userId(user.getId())
                            .uploadedAt(now)
                            .build();
                }

                photo.setLastModifiedDeviceId(deviceId);
                photo.setEditedAt(now);

                if (Boolean.TRUE.equals(dto.getIsDeleted())) {
                    photo.setDeletedAt(now);
                    photo.setIsDeleted(true);
                } else {
                    photo.setDeletedAt(null);
                    photo.setIsDeleted(false);
                    if (dto.getName() != null) photo.setName(dto.getName());
                    if (dto.getGlobalClusterId() != null) photo.setClusterId(dto.getGlobalClusterId());
                    if (dto.getS3Key() != null) photo.setS3Key(dto.getS3Key());
                    if (dto.getLocation() != null) photo.setLocation(dto.getLocation());
                    if (dto.getVisibility() != null) photo.setVisibility(dto.getVisibility());
                    if (dto.getSize() != null) photo.setSize(dto.getSize());
                    if (dto.getTakenAt() != null) photo.setTakenAt(parseDateTime(dto.getTakenAt()));
                }
                photoRepository.save(photo);
            }
        }

        return SyncPushResponseDto.builder()
                .status("SUCCESS")
                .message("Mutations pushed successfully")
                .build();
    }

    @Transactional(readOnly = true)
    public DeltaSyncPullResponseDto pullDeltas(String userEmail, String requestingDeviceId, Long cursor, int limit) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        long sinceCursor = (cursor != null) ? cursor : 0L;
        int pageSize = (limit > 0) ? limit : 200;

        List<Cluster> clusters = clusterRepository.findByUserIdAndRevisionGreaterThanOrderByRevisionAsc(
                user.getId(), sinceCursor, PageRequest.of(0, pageSize));
        List<Photo> photos = photoRepository.findByUserIdAndRevisionGreaterThanOrderByRevisionAsc(
                user.getId(), sinceCursor, PageRequest.of(0, pageSize));

        long maxClusterRev = clusters.stream().mapToLong(c -> c.getRevision() != null ? c.getRevision() : 0L).max().orElse(sinceCursor);
        long maxPhotoRev = photos.stream().mapToLong(p -> p.getRevision() != null ? p.getRevision() : 0L).max().orElse(sinceCursor);
        long nextCursor = Math.max(sinceCursor, Math.max(maxClusterRev, maxPhotoRev));

        List<ClusterDeltaDto> clusterDtos = clusters.stream().map(c -> {
            boolean isEcho = requestingDeviceId != null && requestingDeviceId.equals(c.getLastModifiedDeviceId());
            boolean isDeleted = c.getDeletedAt() != null;

            if (isEcho) {
                return ClusterDeltaDto.builder()
                        .clusterId(c.getClusterId())
                        .revision(c.getRevision())
                        .isEcho(true)
                        .isDeleted(isDeleted)
                        .build();
            }

            return ClusterDeltaDto.builder()
                    .clusterId(c.getClusterId())
                    .revision(c.getRevision())
                    .isEcho(false)
                    .isDeleted(isDeleted)
                    .location(c.getLocation())
                    .thumbnailPhotoId(c.getThumbnailPhotoId())
                    .photoCount(c.getPhotoCount())
                    .visibility(c.getVisibility())
                    .publicPhotoCount(c.getPublicPhotoCount())
                    .privatePhotoCount(c.getPrivatePhotoCount())
                    .friendPhotoCount(c.getFriendPhotoCount())
                    .build();
        }).toList();

        List<PhotoDeltaDto> photoDtos = photos.stream().map(p -> {
            boolean isEcho = requestingDeviceId != null && requestingDeviceId.equals(p.getLastModifiedDeviceId());
            boolean isDeleted = p.getDeletedAt() != null || Boolean.TRUE.equals(p.getIsDeleted());

            if (isEcho) {
                return PhotoDeltaDto.builder()
                        .photoId(p.getPhotoId())
                        .revision(p.getRevision())
                        .isEcho(true)
                        .isDeleted(isDeleted)
                        .build();
            }

            String downloadUrl = null;
            if (!isDeleted && p.getS3Key() != null && !p.getS3Key().isBlank()) {
                try {
                    downloadUrl = s3Service.getDownloadPresignedUrl(p.getS3Key());
                } catch (Exception ignored) {}
            }

            return PhotoDeltaDto.builder()
                    .photoId(p.getPhotoId())
                    .revision(p.getRevision())
                    .isEcho(false)
                    .isDeleted(isDeleted)
                    .clusterId(p.getClusterId())
                    .name(p.getName())
                    .s3Key(p.getS3Key())
                    .downloadUrl(downloadUrl)
                    .takenAt(p.getTakenAt() != null ? p.getTakenAt().toString() : null)
                    .editedAt(p.getEditedAt() != null ? p.getEditedAt().toString() : null)
                    .location(p.getLocation())
                    .visibility(p.getVisibility())
                    .size(p.getSize())
                    .build();
        }).toList();

        boolean hasMore = clusters.size() == pageSize || photos.size() == pageSize;

        return DeltaSyncPullResponseDto.builder()
                .clusters(clusterDtos)
                .photos(photoDtos)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .resetRequired(false)
                .build();
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

