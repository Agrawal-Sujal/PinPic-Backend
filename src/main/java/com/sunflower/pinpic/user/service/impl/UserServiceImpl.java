package com.sunflower.pinpic.user.service.impl;

import com.sunflower.pinpic.user.dto.UpdateProfileRequestDto;
import com.sunflower.pinpic.user.dto.UserProfileResponseDto;
import com.sunflower.pinpic.photo.dto.InitiateUploadResponseDto;
import com.sunflower.pinpic.storage.S3Service;
import com.sunflower.pinpic.user.entity.Follower;
import com.sunflower.pinpic.user.entity.User;
import com.sunflower.pinpic.user.repository.FollowerRepository;
import com.sunflower.pinpic.user.repository.UserRepository;
import com.sunflower.pinpic.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FollowerRepository followerRepository;
    private final S3Service s3Service;

    @Override
    public void setUsername(String email, String username) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setUsername(username);
        userRepository.save(user);
    }

    @Override
    public UserProfileResponseDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return mapToDto(user);
    }

    @Override
    public void updateProfile(String email, UpdateProfileRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setName(requestDto.getName());
        user.setMobileNumber(requestDto.getMobileNumber());
        user.setLocale(requestDto.getLocale());
        user.setPictureUrl(requestDto.getPictureUrl());
        
        userRepository.save(user);
        mapToDto(user);
    }

    @Override
    public void follow(String email, String followingId) {
        User followerUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (followerUser.getId().equals(followingId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }
        
        User followingUser = userRepository.findById(followingId)
                .orElseThrow(() -> new IllegalArgumentException("Following user not found"));
                
        if (followerRepository.existsByFollowerIdAndFollowingId(followerUser.getId(), followingId)) {
            return;
        }
        
        Follower follow = Follower.builder()
                .followerId(followerUser.getId())
                .followingId(followingId)
                .build();
        followerRepository.save(follow);
        
        followerUser.setFollowingCount(followerUser.getFollowingCount() + 1);
        followingUser.setFollowerCount(followingUser.getFollowerCount() + 1);
        
        userRepository.save(followerUser);
        userRepository.save(followingUser);
    }

    @Override
    public void unfollow(String email, String followingId) {
        User followerUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
        Follower follow = followerRepository.findByFollowerIdAndFollowingId(followerUser.getId(), followingId)
                .orElse(null);
                
        if (follow == null) {
            return;
        }
        
        User followingUser = userRepository.findById(followingId)
                .orElseThrow(() -> new IllegalArgumentException("Following user not found"));
                
        followerRepository.delete(follow);
        
        followerUser.setFollowingCount(followerUser.getFollowingCount() - 1);
        followingUser.setFollowerCount(followingUser.getFollowerCount() - 1);
        
        userRepository.save(followerUser);
        userRepository.save(followingUser);
    }

    @Override
    public List<UserProfileResponseDto> getFollowers(String userId) {
        List<Follower> followers = followerRepository.findAllByFollowingId(userId);
        List<String> followerIds = followers.stream()
                .map(Follower::getFollowerId)
                .collect(Collectors.toList());
                
        return userRepository.findByIdIn(followerIds).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserProfileResponseDto> getFollowings(String userId) {
        List<Follower> followings = followerRepository.findAllByFollowerId(userId);
        List<String> followingIds = followings.stream()
                .map(Follower::getFollowingId)
                .collect(Collectors.toList());
                
        return userRepository.findByIdIn(followingIds).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public InitiateUploadResponseDto initiateProfilePhotoUpload(String email, String extension) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        String s3Key = "profiles/" + user.getId() + "/" + UUID.randomUUID() + (extension != null ? "." + extension : "");
        String presignedUrl = s3Service.getProfileUploadPresignedUrl(s3Key);
        
        return new InitiateUploadResponseDto(s3Key, presignedUrl);
    }

    @Override
    public com.sunflower.pinpic.user.dto.ProfilePhotoConfirmResponseDto confirmProfilePhotoUpload(String email, String s3Key) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        String publicUrl = s3Service.getPublicUrlForProfile(s3Key);
        
        user.setPictureUrl(publicUrl);
        userRepository.save(user);
        
        return new com.sunflower.pinpic.user.dto.ProfilePhotoConfirmResponseDto(publicUrl);
    }

    private UserProfileResponseDto mapToDto(User user) {
        return UserProfileResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .name(user.getName())
                .mobileNumber(user.getMobileNumber())
                .locale(user.getLocale())
                .pictureUrl(user.getPictureUrl())
                .followerCount(user.getFollowerCount())
                .followingCount(user.getFollowingCount())
                .build();
    }
}
