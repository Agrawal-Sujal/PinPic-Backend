package com.sunflower.pinpic.device.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRegistrationRequestDto {
    private String deviceId;
    private String device;
    private String userId;
    private String lastAckCursor;
}
