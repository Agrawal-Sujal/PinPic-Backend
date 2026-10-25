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
public class ClusterSyncDto {
    private String globalClusterId;
    private Location location;
    private String thumbnailPhotoGlobalId;
    private Integer photoCount;
    private Visibility visibility;
    private Integer publicPhotoCount;
    private Integer privatePhotoCount;
    private Integer friendPhotoCount;
}

