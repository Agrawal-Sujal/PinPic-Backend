package com.sunflower.pinpic.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleSignInRequestDto {
    @NotBlank(message = "ID Token String is required")
    private String idTokenString;
}
