package com.sunflower.pinpic.user.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.sunflower.pinpic.security.JwtTokenProvider;
import com.sunflower.pinpic.user.dto.AuthResponseDto;
import com.sunflower.pinpic.user.dto.GoogleSignInRequestDto;
import com.sunflower.pinpic.user.dto.LoginRequestDto;
import com.sunflower.pinpic.user.dto.RegisterRequestDto;
import com.sunflower.pinpic.user.entity.Role;
import com.sunflower.pinpic.user.entity.User;
import com.sunflower.pinpic.user.repository.UserRepository;
import com.sunflower.pinpic.user.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleIdTokenVerifier googleVerifier;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtTokenProvider jwtTokenProvider,
                           @Value("${google.client.id}") String googleClientId) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.googleVerifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    @Override
    public AuthResponseDto login(LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtTokenProvider.generateToken(user.getEmail());
        return new AuthResponseDto(token, user.getUsername());
    }

    @Override
    @Transactional
    public void register(RegisterRequestDto request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    @Override
    @Transactional
    public AuthResponseDto googleSignIn(GoogleSignInRequestDto request) {
        try {
            GoogleIdToken idToken = googleVerifier.verify(request.getIdTokenString());
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();

                User user = userRepository.findByEmail(email).orElseGet(() -> {
                    // Create new user if doesn't exist
                    User newUser = User.builder()
                            .email(email)
                            .role(Role.USER)
                            .name(payload.get("name").toString())
                            .pictureUrl((String) payload.get("picture"))
                            .build();
                    return userRepository.save(newUser);
                });

                String token = jwtTokenProvider.generateToken(user.getEmail());
                return new AuthResponseDto(token, user.getUsername());
            } else {
                throw new IllegalArgumentException("Invalid ID token");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ID token: " + e.getMessage());
        }
    }
}
