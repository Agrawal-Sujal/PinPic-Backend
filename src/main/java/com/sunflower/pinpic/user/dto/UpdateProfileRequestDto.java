package com.sunflower.pinpic.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequestDto {
    private String name;
    private String mobileNumber;
    private String locale;
    private String pictureUrl;
}
