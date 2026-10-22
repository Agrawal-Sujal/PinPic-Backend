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
public class SyncBatchRequestDto {
    private String deviceId;
    private List<SyncItemDto> operations;
}

