package com.sunflower.pinpic.user.controller;

import com.sunflower.pinpic.user.dto.AuthResponseDto;
import com.sunflower.pinpic.user.dto.GoogleSignInRequestDto;
import com.sunflower.pinpic.user.dto.LoginRequestDto;
import com.sunflower.pinpic.user.dto.RegisterRequestDto;
import com.sunflower.pinpic.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequestDto request) {
        authService.register(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponseDto> googleSignIn(@Valid @RequestBody GoogleSignInRequestDto request) {
        return ResponseEntity.ok(authService.googleSignIn(request));
    }
}
