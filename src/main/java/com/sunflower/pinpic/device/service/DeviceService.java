package com.sunflower.pinpic.device.service;

import com.sunflower.pinpic.device.dto.DeviceRegistrationRequestDto;
import com.sunflower.pinpic.device.dto.DeviceResponseDto;

public interface DeviceService {
    DeviceResponseDto registerOrGetDevice(DeviceRegistrationRequestDto request, String userEmail);
    DeviceResponseDto getDeviceById(String deviceId);
}
