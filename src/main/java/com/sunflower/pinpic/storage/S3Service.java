package com.sunflower.pinpic.storage;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;

@Service
public class S3Service {

    @Autowired
    private AmazonS3 s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String generatePresignedUrl(String objectKey, HttpMethod httpMethod, int expirationInMinutes) {
        // Set the presigned URL to expire after specified minutes
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000L * 60 * expirationInMinutes;
        expiration.setTime(expTimeMillis);

        // Generate the presigned URL
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, objectKey)
                        .withMethod(httpMethod)
                        .withExpiration(expiration);

        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    public String getDownloadPresignedUrl(String objectKey) {
        // Default to 60 minutes for download URLs
        return generatePresignedUrl(objectKey, HttpMethod.GET, 60);
    }

    public String getUploadPresignedUrl(String objectKey) {
        // Default to 15 minutes for upload URLs
        return generatePresignedUrl(objectKey, HttpMethod.PUT, 15);
    }

    public Map<String, Object> getObjectMetadata(String objectKey) {
        ObjectMetadata metadata = s3Client.getObjectMetadata(bucketName, objectKey);
        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put("contentType", metadata.getContentType());
        metadataMap.put("contentLength", metadata.getContentLength());
        metadataMap.put("lastModified", metadata.getLastModified());
        metadataMap.put("userMetadata", metadata.getUserMetadata());
        return metadataMap;
    }
}
