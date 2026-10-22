package com.sunflower.pinpic.photo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Photo {
    @Id
    @Builder.Default
    private String photoId = UUID.randomUUID().toString();

    private String userId;

    private String clusterId;

    private String name;

    private String s3Key;

    private LocalDateTime takenAt;

    private LocalDateTime uploadedAt;

    private LocalDateTime editedAt;

    @Embedded
    private Location location;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Visibility visibility = Visibility.PRIVATE;

    @Builder.Default
    private Boolean isDeleted = false;
    
    private Long size;
}
