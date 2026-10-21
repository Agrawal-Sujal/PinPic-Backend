package com.sunflower.pinpic.device.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @Builder.Default
    private String deviceId = UUID.randomUUID().toString();

    private String device;

    private String userId;

    private String lastAckCursor;

    private String lastSeenAt;

    private String createdAt;

    private String lastLogin;
}
