package com.sunflower.pinpic.sync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncPullItemDto {
    private Long operationId;
    private String entityType; // "PHOTOS" or "CLUSTER"
    private String operation;  // "ADD", "UPDATE", "DELETE"
    private String entityId;   // global photo or cluster ID
    private String deviceId;
    private PhotoSyncDto photo;
    private ClusterSyncDto cluster;
    private String downloadUrl;
}

