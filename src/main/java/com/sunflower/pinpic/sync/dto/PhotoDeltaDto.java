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
public class PhotoDeltaDto {
    private String photoId;
    private Long revision;
    private Boolean isEcho;
    private Boolean isDeleted;
    private String clusterId;
    private String name;
    private String s3Key;
    private String downloadUrl;
    private String takenAt;
    private String editedAt;
    private Location location;
    private Visibility visibility;
    private Long size;
}
