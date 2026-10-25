package com.sunflower.pinpic.user.service;

import com.sunflower.pinpic.user.dto.UpdateProfileRequestDto;
import com.sunflower.pinpic.user.dto.UserProfileResponseDto;
import com.sunflower.pinpic.photo.dto.InitiateUploadResponseDto;
import java.util.List;

public interface UserService {
    void setUsername(String email, String username);
    UserProfileResponseDto getProfile(String email);
    void updateProfile(String email, UpdateProfileRequestDto requestDto);
    
    void follow(String email, String followingId);
    void unfollow(String email, String followingId);
    List<UserProfileResponseDto> getFollowers(String userId);
    List<UserProfileResponseDto> getFollowings(String userId);
    List<UserProfileResponseDto> searchUsers(String email, String query);
    UserProfileResponseDto getUserProfileById(String email, String userId);
    List<com.sunflower.pinpic.photo.dto.UserPhotoResponseDto> getUserPhotos(String email, String userId);
    
    InitiateUploadResponseDto initiateProfilePhotoUpload(String email, String extension);
    com.sunflower.pinpic.user.dto.ProfilePhotoConfirmResponseDto confirmProfilePhotoUpload(String email, String s3Key);
}
