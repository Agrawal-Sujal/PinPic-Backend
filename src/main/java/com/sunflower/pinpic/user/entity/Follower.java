package com.sunflower.pinpic.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "followers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Follower {
    
    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    
    private String followerId;
    
    private String followingId;
}
