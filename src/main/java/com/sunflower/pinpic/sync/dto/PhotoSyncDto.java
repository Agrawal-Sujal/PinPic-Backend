package com.sunflower.pinpic.sync.dto;

import com.sunflower.pinpic.photo.entity.Location;
import com.sunflower.pinpic.photo.entity.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoSyncDto {
    private String globalPhotoId;
    private String globalClusterId;
    private String name;
    private String s3Key;
    private String takenAt;
    private String editedAt;
    private Location location;
    private Visibility visibility;
    private Long size;
}

