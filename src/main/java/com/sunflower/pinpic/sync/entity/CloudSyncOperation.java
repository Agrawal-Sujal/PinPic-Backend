package com.sunflower.pinpic.sync.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cloud_sync_operations", indexes = {
        @Index(name = "idx_sync_user_op", columnList = "userId, operationId"),
        @Index(name = "idx_sync_device", columnList = "deviceId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloudSyncOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long operationId;

    @Column(nullable = false)
    private String userId;

    @Column
    private String deviceId;

    @Column(nullable = false)
    private String entityType; // "PHOTOS" or "CLUSTER"

    @Column(nullable = false)
    private String operation;  // "ADD", "UPDATE", "DELETE"

    @Column(nullable = false)
    private String entityId;   // global photo or cluster ID

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
