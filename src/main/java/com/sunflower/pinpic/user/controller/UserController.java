package com.sunflower.pinpic.user.controller;

import com.sunflower.pinpic.user.dto.SetUsernameRequestDto;
import com.sunflower.pinpic.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}
