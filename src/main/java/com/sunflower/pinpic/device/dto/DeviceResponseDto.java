package com.sunflower.pinpic.device.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceResponseDto {
    private String deviceId;
    private String device;
    private String userId;
    private String lastAckCursor;
    private String lastSeenAt;
    private String createdAt;
    private String lastLogin;
}
