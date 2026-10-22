package com.sunflower.pinpic.sync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncPullRequestDto {
    private String deviceId;
    private Long lastOperationId;
    private Integer batchSize;
}

