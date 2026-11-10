package com.sunflower.pinpic.photo.dto;

import com.sunflower.pinpic.photo.entity.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPhotoResponseDto {
    private String photoId;
    private String userId;
    private String clusterId;
    private Location location;
    private String name;
    private String downloadUrl;
    private String visibility;
    private String takenAt;
}

