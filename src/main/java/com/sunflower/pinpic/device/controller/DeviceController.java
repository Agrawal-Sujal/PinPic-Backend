package com.sunflower.pinpic.device.controller;

import com.sunflower.pinpic.device.dto.DeviceRegistrationRequestDto;
import com.sunflower.pinpic.device.dto.DeviceResponseDto;
import com.sunflower.pinpic.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    public ResponseEntity<DeviceResponseDto> registerDevice(
            @RequestBody DeviceRegistrationRequestDto request,
            Principal principal
    ) {
        String userEmail = principal != null ? principal.getName() : null;
        DeviceResponseDto response = deviceService.registerOrGetDevice(request, userEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceResponseDto> getDevice(@PathVariable String deviceId) {
        return ResponseEntity.ok(deviceService.getDeviceById(deviceId));
    }
}
