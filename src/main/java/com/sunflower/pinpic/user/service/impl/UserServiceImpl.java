package com.sunflower.pinpic.user.service.impl;

import com.sunflower.pinpic.user.dto.UpdateProfileRequestDto;
import com.sunflower.pinpic.user.dto.UserProfileResponseDto;
import com.sunflower.pinpic.user.entity.User;
import com.sunflower.pinpic.user.repository.UserRepository;
import com.sunflower.pinpic.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

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
    public UserProfileResponseDto updateProfile(String email, UpdateProfileRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (requestDto.getName() != null) user.setName(requestDto.getName());
        if (requestDto.getMobileNumber() != null) user.setMobileNumber(requestDto.getMobileNumber());
        if (requestDto.getLocale() != null) user.setLocale(requestDto.getLocale());
        if (requestDto.getPictureUrl() != null) user.setPictureUrl(requestDto.getPictureUrl());
        
        userRepository.save(user);
        return mapToDto(user);
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
                .build();
    }
}
