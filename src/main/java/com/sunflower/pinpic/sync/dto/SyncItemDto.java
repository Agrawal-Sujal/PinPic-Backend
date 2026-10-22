package com.sunflower.pinpic.sync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncItemDto {
    private String operationId;
    private String entityType; // "CLUSTER" or "PHOTOS"
    private String operation;  // "ADD", "UPDATE", "DELETE"
    private String localEntityId;
    private ClusterSyncDto cluster;
    private PhotoSyncDto photo;
}

