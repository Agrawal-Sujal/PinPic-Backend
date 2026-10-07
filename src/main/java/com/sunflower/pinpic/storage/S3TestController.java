package com.sunflower.pinpic.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/s3")
public class S3TestController {

    @Autowired
    private S3Service s3Service;

    @GetMapping("/presigned-url")
    public ResponseEntity<String> getPresignedUrl(@RequestParam String objectKey) {
        try {
            String url = s3Service.getDownloadPresignedUrl(objectKey);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error generating presigned URL: " + e.getMessage());
        }
    }

    @GetMapping("/presigned-url-upload")
    public ResponseEntity<String> getUploadPresignedUrl(@RequestParam String objectKey) {
        try {
            String url = s3Service.getUploadPresignedUrl(objectKey);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error generating upload presigned URL: " + e.getMessage());
        }
    }

    @GetMapping("/metadata")
    public ResponseEntity<?> getObjectMetadata(@RequestParam String objectKey) {
        try {
            Map<String, Object> metadata = s3Service.getObjectMetadata(objectKey);
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error retrieving metadata: " + e.getMessage());
        }
    }
}
