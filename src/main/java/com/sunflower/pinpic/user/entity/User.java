package com.sunflower.pinpic.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    
    private String email;
    
    private String password;
    
    @Enumerated(EnumType.STRING)
    private Role role;
    
    private String username;
    
    private String mobileNumber;
    
    private String name;
    
    private String locale;
    
    private String pictureUrl;
    
    @Column(columnDefinition = "integer default 0", nullable = false)
    @Builder.Default
    private int followerCount = 0;
    
    @Column(columnDefinition = "integer default 0", nullable = false)
    @Builder.Default
    private int followingCount = 0;
}
