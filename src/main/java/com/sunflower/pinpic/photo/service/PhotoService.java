package com.sunflower.pinpic.photo.service;

import com.sunflower.pinpic.photo.dto.ConfirmUploadRequestDto;
import com.sunflower.pinpic.photo.dto.InitiateUploadRequestDto;
import com.sunflower.pinpic.photo.dto.InitiateUploadResponseDto;
import com.sunflower.pinpic.photo.entity.Photo;
import com.sunflower.pinpic.photo.repository.PhotoRepository;
import com.sunflower.pinpic.storage.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class PhotoService {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private S3Service s3Service;

    public InitiateUploadResponseDto initiateUpload(InitiateUploadRequestDto request, String userId) {
        String uniqueId = UUID.randomUUID().toString();
        // Create s3 key like: userId/uniqueId_filename
        String s3Key = userId + "/" + uniqueId + "_" + request.getName();

        Photo photo = Photo.builder()
                .userId(userId)
                .name(request.getName())
                .s3Key(s3Key)
                .takenAt(request.getTakenAt())
                .location(request.getLocation())
                .build();

        photoRepository.save(photo);

        String presignedUrl = s3Service.getUploadPresignedUrl(s3Key);

        return new InitiateUploadResponseDto(s3Key, presignedUrl);
    }

    public Photo confirmUpload(ConfirmUploadRequestDto request) {
        Photo photo = photoRepository.findByS3Key(request.getS3Key())
                .orElseThrow(() -> new RuntimeException("Photo not found for S3 key: " + request.getS3Key()));

        Map<String, Object> metadata = s3Service.getObjectMetadata(request.getS3Key());
        
        Long contentLength = (Long) metadata.get("contentLength");
        photo.setSize(contentLength);
        
        LocalDateTime now = LocalDateTime.now();
        photo.setUploadedAt(now);
        photo.setEditedAt(now);

        return photoRepository.save(photo);
    }
}
