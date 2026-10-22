package com.sunflower.pinpic.photo.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clusters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cluster {

    @Id
    @Builder.Default
    private String clusterId = UUID.randomUUID().toString();

    private String userId;

    @Embedded
    private Location location;

    private String thumbnailPhotoId;

    @Builder.Default
    private Integer photoCount = 0;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
