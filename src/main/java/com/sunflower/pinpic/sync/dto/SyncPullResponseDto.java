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
public class SyncPullResponseDto {
    private List<SyncPullItemDto> operations;
    private Long lastOperationId;
    private boolean hasMore;
}

