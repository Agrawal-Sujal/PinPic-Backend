package com.sunflower.pinpic.user.controller;

import com.sunflower.pinpic.user.dto.SetUsernameRequestDto;
import com.sunflower.pinpic.user.dto.UpdateProfileRequestDto;
import com.sunflower.pinpic.user.dto.UserProfileResponseDto;
import com.sunflower.pinpic.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

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

    @PostMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequestDto request, Principal principal) {
        userService.updateProfile(principal.getName(), request);
        return ResponseEntity.ok(java.util.Collections.emptyMap());
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserProfileResponseDto>> searchUsers(@RequestParam("query") String query, Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(userService.searchUsers(email, query));
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponseDto> getUserProfileById(@PathVariable String userId, Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(userService.getUserProfileById(email, userId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponseDto> getUserById(@PathVariable String userId, Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(userService.getUserProfileById(email, userId));
    }

    @GetMapping("/{userId}/photos")
    public ResponseEntity<List<com.sunflower.pinpic.photo.dto.UserPhotoResponseDto>> getUserPhotos(@PathVariable String userId, Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.getUserPhotos(principal.getName(), userId));
    }

    @GetMapping("/{userId}/clusters")
    public ResponseEntity<List<com.sunflower.pinpic.photo.dto.UserClusterResponseDto>> getUserClusters(@PathVariable String userId, Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.getUserClusters(principal.getName(), userId));
    }

    @PostMapping("/follow/{followingId}")
    public ResponseEntity<Void> follow(@PathVariable String followingId, Principal principal) {
        userService.follow(principal.getName(), followingId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/follow/{followingId}")
    public ResponseEntity<Void> unfollow(@PathVariable String followingId, Principal principal) {
        userService.unfollow(principal.getName(), followingId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserProfileResponseDto>> getFollowers(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getFollowers(userId));
    }

    @GetMapping("/{userId}/followings")
    public ResponseEntity<List<UserProfileResponseDto>> getFollowings(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getFollowings(userId));
    }
}
