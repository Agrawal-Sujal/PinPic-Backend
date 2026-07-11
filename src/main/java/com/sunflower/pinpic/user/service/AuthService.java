package com.sunflower.pinpic.user.service;

import com.sunflower.pinpic.user.dto.AuthResponseDto;
import com.sunflower.pinpic.user.dto.GoogleSignInRequestDto;
import com.sunflower.pinpic.user.dto.LoginRequestDto;
import com.sunflower.pinpic.user.dto.RegisterRequestDto;

public interface AuthService {
    AuthResponseDto login(LoginRequestDto request);
    void register(RegisterRequestDto request);
    AuthResponseDto googleSignIn(GoogleSignInRequestDto request);
}
