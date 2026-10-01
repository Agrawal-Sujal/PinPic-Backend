package com.sunflower.pinpic.user.service;

import com.sunflower.pinpic.user.dto.UpdateProfileRequestDto;
import com.sunflower.pinpic.user.dto.UserProfileResponseDto;

public interface UserService {
    void setUsername(String email, String username);
    UserProfileResponseDto getProfile(String email);
    UserProfileResponseDto updateProfile(String email, UpdateProfileRequestDto requestDto);
}
