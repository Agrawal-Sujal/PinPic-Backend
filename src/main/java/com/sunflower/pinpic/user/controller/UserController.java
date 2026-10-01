package com.sunflower.pinpic.user.controller;

import com.sunflower.pinpic.user.dto.SetUsernameRequestDto;
import com.sunflower.pinpic.user.dto.UpdateProfileRequestDto;
import com.sunflower.pinpic.user.dto.UserProfileResponseDto;
import com.sunflower.pinpic.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/username")
    public ResponseEntity<Void> setUsername(@Valid @RequestBody SetUsernameRequestDto request, Principal principal) {
        userService.setUsername(principal.getName(), request.getUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponseDto> getProfile(Principal principal) {
        return ResponseEntity.ok(userService.getProfile(principal.getName()));
    }

    @PatchMapping("/profile")
    public ResponseEntity<UserProfileResponseDto> updateProfile(@Valid @RequestBody UpdateProfileRequestDto request, Principal principal) {
        return ResponseEntity.ok(userService.updateProfile(principal.getName(), request));
    }
}
