package com.sunflower.pinpic.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDto {
    private String id;
    private String email;
    private String username;
    private String name;
    private String mobileNumber;
    private String locale;
    private String pictureUrl;
    private int followerCount;
    private int followingCount;
}
