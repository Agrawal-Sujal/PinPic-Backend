package com.sunflower.pinpic.photo.service;

import com.sunflower.pinpic.photo.dto.ConfirmUploadRequestDto;
import com.sunflower.pinpic.photo.dto.InitiateUploadRequestDto;
import com.sunflower.pinpic.photo.dto.InitiateUploadResponseDto;
import com.sunflower.pinpic.photo.entity.Photo;
import com.sunflower.pinpic.photo.repository.PhotoRepository;
import com.sunflower.pinpic.storage.S3Service;
import com.sunflower.pinpic.sync.entity.CloudSyncOperation;
import com.sunflower.pinpic.sync.repository.CloudSyncOperationRepository;
import com.sunflower.pinpic.user.entity.User;
import com.sunflower.pinpic.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PhotoService {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private CloudSyncOperationRepository cloudSyncOperationRepository;

    public InitiateUploadResponseDto initiateUpload(InitiateUploadRequestDto request, String userId) {
        String uniqueId = UUID.randomUUID().toString();
        // Create s3 key like: userId/uniqueId_filename
        String s3Key = userId + "/" + uniqueId + "_" + request.getName();

        String presignedUrl = s3Service.getUploadPresignedUrl(s3Key);

        return new InitiateUploadResponseDto(s3Key, presignedUrl);
    }

    public Photo confirmUpload(ConfirmUploadRequestDto request) {
        String s3Key = request.getS3Key();

        Map<String, Object> metadata = s3Service.getObjectMetadata(s3Key);
        Long contentLength = (Long) metadata.get("contentLength");

        LocalDateTime now = LocalDateTime.now();

        // Extract userId and photo name from s3Key (format: userId/uniqueId_name)
        String userId = null;
        String name = request.getName();

        if (s3Key != null && s3Key.contains("/")) {
            int slashIndex = s3Key.indexOf("/");
            userId = s3Key.substring(0, slashIndex);
            if (name == null || name.isBlank()) {
                String remaining = s3Key.substring(slashIndex + 1);
                int underscoreIndex = remaining.indexOf("_");
                if (underscoreIndex != -1 && underscoreIndex + 1 < remaining.length()) {
                    name = remaining.substring(underscoreIndex + 1);
                } else {
                    name = remaining;
                }
            }
        }

        LocalDateTime takenAt = request.getTakenAt() != null ? request.getTakenAt() : now;

        Photo photo = photoRepository.findByS3Key(s3Key).orElse(null);
        if (photo == null) {
            photo = Photo.builder()
                    .userId(userId)
                    .clusterId(request.getClusterId())
                    .name(name)
                    .s3Key(s3Key)
                    .takenAt(takenAt)
                    .location(request.getLocation())
                    .size(contentLength)
                    .uploadedAt(now)
                    .editedAt(now)
                    .build();
        } else {
            photo.setSize(contentLength);
            photo.setUploadedAt(now);
            photo.setEditedAt(now);
            if (request.getClusterId() != null) {
                photo.setClusterId(request.getClusterId());
            }
            if (request.getLocation() != null) {
                photo.setLocation(request.getLocation());
            }
            if (request.getTakenAt() != null) {
                photo.setTakenAt(request.getTakenAt());
            }
            if (name != null) {
                photo.setName(name);
            }
        }

        return photoRepository.save(photo);
    }

    public void deletePhoto(String photoId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("Photo not found: " + photoId));

        if (!user.getId().equals(photo.getUserId())) {
            throw new IllegalArgumentException("Unauthorized to delete this photo");
        }

        photo.setIsDeleted(true);
        photo.setEditedAt(LocalDateTime.now());
        photoRepository.save(photo);

        try {
            cloudSyncOperationRepository.save(CloudSyncOperation.builder()
                    .userId(user.getId())
                    .entityType("PHOTOS")
                    .operation("DELETE")
                    .entityId(photoId)
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (Exception ignored) {}
    }

    public void deletePhotos(List<String> photoIds, String userEmail) {
        if (photoIds == null || photoIds.isEmpty()) return;

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        List<Photo> photos = photoRepository.findAllById(photoIds);
        LocalDateTime now = LocalDateTime.now();
        for (Photo photo : photos) {
            if (user.getId().equals(photo.getUserId())) {
                photo.setIsDeleted(true);
                photo.setEditedAt(now);
                try {
                    cloudSyncOperationRepository.save(CloudSyncOperation.builder()
                            .userId(user.getId())
                            .entityType("PHOTOS")
                            .operation("DELETE")
                            .entityId(photo.getPhotoId())
                            .createdAt(now)
                            .build());
                } catch (Exception ignored) {}
            }
        }
        photoRepository.saveAll(photos);
    }
}
