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
public class DeltaSyncPullResponseDto {
    private List<ClusterDeltaDto> clusters;
    private List<PhotoDeltaDto> photos;
    private Long nextCursor;
    private Boolean hasMore;
    private Boolean resetRequired;
}
