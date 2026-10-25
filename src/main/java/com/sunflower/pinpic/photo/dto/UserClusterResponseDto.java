package com.sunflower.pinpic.photo.dto;

import com.sunflower.pinpic.photo.entity.Location;
import com.sunflower.pinpic.photo.entity.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserClusterResponseDto {
    private String clusterId;
    private String userId;
    private Location location;
    private String thumbnailPhotoId;
    private String thumbnailUrl;
    private Integer photoCount;
    private Visibility visibility;
    private List<UserPhotoResponseDto> photos;
}

