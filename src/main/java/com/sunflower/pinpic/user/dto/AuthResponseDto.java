package com.sunflower.pinpic.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private String jwt;
    private String username;
    private String userId;
    private String profileUrl;
    private String emailId;
}
