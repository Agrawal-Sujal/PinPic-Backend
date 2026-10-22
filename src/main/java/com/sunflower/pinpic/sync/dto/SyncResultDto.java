package com.sunflower.pinpic.sync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncResultDto {
    private String operationId;
    private String localEntityId;
    private String entityType;
    private String globalId;
    private String status; // "SUCCESS" or "FAILED"
    private String errorMessage;
}

