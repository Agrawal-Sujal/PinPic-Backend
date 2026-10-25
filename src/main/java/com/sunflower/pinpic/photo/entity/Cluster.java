package com.sunflower.pinpic.photo.entity;

import jakarta.persistence.*;
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

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Visibility visibility = Visibility.PRIVATE;

    @Builder.Default
    private Integer publicPhotoCount = 0;

    @Builder.Default
    private Integer privatePhotoCount = 0;

    @Builder.Default
    private Integer friendPhotoCount = 0;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Visibility calculateVisibility() {
        if (publicPhotoCount != null && publicPhotoCount > 0) {
            return Visibility.PUBLIC;
        } else if (friendPhotoCount != null && friendPhotoCount > 0) {
            return Visibility.FRIENDS;
        } else {
            return Visibility.PRIVATE;
        }
    }

    public void updateVisibility() {
        this.visibility = calculateVisibility();
    }

    public Integer getFriendsPhotoCount() {
        return friendPhotoCount;
    }

    public void setFriendsPhotoCount(Integer count) {
        this.friendPhotoCount = count;
    }
}
