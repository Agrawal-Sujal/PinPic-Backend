package com.sunflower.pinpic.photo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sunflower.pinpic.photo.entity.Location;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConfirmUploadRequestDto {
    private String s3Key;
    private String name;
    private String clusterId;

    @JsonFormat(pattern = "[yyyy-MM-dd'T'HH:mm:ss[.SSS]][yyyy-MM-dd'T'HH:mm:ss][yyyy-MM-dd HH:mm:ss][yyyy-MM-dd]")
    private LocalDateTime takenAt;

    private Location location;
}
