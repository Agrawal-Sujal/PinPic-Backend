package com.sunflower.pinpic.photo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InitiateUploadResponseDto {
    private String s3Key;
    private String presignedUrl;
}
