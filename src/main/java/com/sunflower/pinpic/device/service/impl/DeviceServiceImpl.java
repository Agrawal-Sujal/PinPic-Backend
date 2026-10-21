package com.sunflower.pinpic.device.service.impl;

import com.sunflower.pinpic.device.dto.DeviceRegistrationRequestDto;
import com.sunflower.pinpic.device.dto.DeviceResponseDto;
import com.sunflower.pinpic.device.entity.Device;
import com.sunflower.pinpic.device.repository.DeviceRepository;
import com.sunflower.pinpic.device.service.DeviceService;
import com.sunflower.pinpic.user.entity.User;
import com.sunflower.pinpic.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DeviceResponseDto registerOrGetDevice(DeviceRegistrationRequestDto request, String userEmail) {
        String resolvedUserId = null;
        if (userEmail != null && !userEmail.isBlank()) {
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isPresent()) {
                resolvedUserId = userOpt.get().getId();
            }
        }
        if (resolvedUserId == null && request.getUserId() != null && !request.getUserId().isBlank()) {
            resolvedUserId = request.getUserId();
        }

        String now = Instant.now().toString();
        Device deviceEntity = null;

        if (request.getDeviceId() != null && !request.getDeviceId().isBlank()) {
            Optional<Device> existingOpt = deviceRepository.findById(request.getDeviceId());
            if (existingOpt.isPresent()) {
                deviceEntity = existingOpt.get();
                deviceEntity.setLastSeenAt(now);
                if (request.getDevice() != null && !request.getDevice().isBlank()) {
                    deviceEntity.setDevice(request.getDevice());
                }
                if (resolvedUserId != null) {
                    deviceEntity.setUserId(resolvedUserId);
                    deviceEntity.setLastLogin(now);
                }
                if (request.getLastAckCursor() != null) {
                    deviceEntity.setLastAckCursor(request.getLastAckCursor());
                }
                deviceEntity = deviceRepository.save(deviceEntity);
            }
        }

        if (deviceEntity == null) {
            String deviceId = (request.getDeviceId() != null && !request.getDeviceId().isBlank())
                    ? request.getDeviceId()
                    : UUID.randomUUID().toString();

            deviceEntity = Device.builder()
                    .deviceId(deviceId)
                    .device(request.getDevice())
                    .userId(resolvedUserId)
                    .lastAckCursor(request.getLastAckCursor())
                    .lastSeenAt(now)
                    .createdAt(now)
                    .lastLogin(resolvedUserId != null ? now : null)
                    .build();

            deviceEntity = deviceRepository.save(deviceEntity);
        }

        return mapToDto(deviceEntity);
    }

    @Override
    public DeviceResponseDto getDeviceById(String deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found with id: " + deviceId));
        return mapToDto(device);
    }

    private DeviceResponseDto mapToDto(Device device) {
        return DeviceResponseDto.builder()
                .deviceId(device.getDeviceId())
                .device(device.getDevice())
                .userId(device.getUserId())
                .lastAckCursor(device.getLastAckCursor())
                .lastSeenAt(device.getLastSeenAt())
                .createdAt(device.getCreatedAt())
                .lastLogin(device.getLastLogin())
                .build();
    }
}
