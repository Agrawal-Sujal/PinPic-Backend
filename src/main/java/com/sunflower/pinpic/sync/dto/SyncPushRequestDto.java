package com.sunflower.pinpic.sync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncPushRequestDto {
    private String deviceId;
    private List<ClusterSyncDto> clusters;
    private List<PhotoSyncDto> photos;
}
