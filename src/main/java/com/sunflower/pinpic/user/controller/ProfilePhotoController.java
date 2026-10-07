package com.sunflower.pinpic.user.controller;

import com.sunflower.pinpic.photo.dto.ConfirmUploadRequestDto;
import com.sunflower.pinpic.photo.dto.InitiateUploadResponseDto;
import com.sunflower.pinpic.user.dto.ProfilePhotoConfirmResponseDto;
import com.sunflower.pinpic.user.dto.ProfilePhotoInitiateRequestDto;
import com.sunflower.pinpic.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/profile-photo")
@RequiredArgsConstructor
public class ProfilePhotoController {

    private final UserService userService;

    @PostMapping("/initiate-upload")
    public ResponseEntity<InitiateUploadResponseDto> initiateUpload(@RequestBody ProfilePhotoInitiateRequestDto request, Principal principal) {
        return ResponseEntity.ok(userService.initiateProfilePhotoUpload(principal.getName(), request.getName()));
    }

    @PostMapping("/confirm-upload")
    public ResponseEntity<ProfilePhotoConfirmResponseDto> confirmUpload(@RequestBody ConfirmUploadRequestDto request, Principal principal) {
        return ResponseEntity.ok(userService.confirmProfilePhotoUpload(principal.getName(), request.getS3Key()));
    }
}
