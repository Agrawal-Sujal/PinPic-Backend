package com.sunflower.pinpic.user.service;

import com.sunflower.pinpic.user.dto.UpdateProfileRequestDto;
import com.sunflower.pinpic.user.dto.UserProfileResponseDto;
import java.util.List;

public interface UserService {
    void setUsername(String email, String username);
    UserProfileResponseDto getProfile(String email);
    UserProfileResponseDto updateProfile(String email, UpdateProfileRequestDto requestDto);
    
    void follow(String email, String followingId);
    void unfollow(String email, String followingId);
    List<UserProfileResponseDto> getFollowers(String userId);
    List<UserProfileResponseDto> getFollowings(String userId);
}
