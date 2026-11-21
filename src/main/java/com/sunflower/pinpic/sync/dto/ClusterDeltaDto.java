package com.sunflower.pinpic.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClusterDeltaDto {
    private String clusterId;
    private Long revision;
    private Boolean isEcho;
    private Boolean isDeleted;
    private Location location;
    private String thumbnailPhotoId;
    private Integer photoCount;
    private Visibility visibility;
    private Integer publicPhotoCount;
    private Integer privatePhotoCount;
    private Integer friendPhotoCount;
}
