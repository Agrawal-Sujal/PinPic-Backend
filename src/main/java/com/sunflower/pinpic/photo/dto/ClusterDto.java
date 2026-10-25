package com.sunflower.pinpic.photo.dto;

import com.sunflower.pinpic.photo.entity.Location;
import com.sunflower.pinpic.photo.entity.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClusterDto {
    private String clusterId;
    private String userId;
    private Location location;
    private String thumbnailPhotoId;
    private Integer photoCount;
    private Visibility visibility;
    private Integer publicPhotoCount;
    private Integer privatePhotoCount;
    private Integer friendPhotoCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
