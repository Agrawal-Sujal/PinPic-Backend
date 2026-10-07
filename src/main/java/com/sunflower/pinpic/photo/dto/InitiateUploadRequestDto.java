package com.sunflower.pinpic.photo.dto;

import com.sunflower.pinpic.photo.entity.Location;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InitiateUploadRequestDto {
    private String name;
    private LocalDateTime takenAt;
    private Location location;
}
