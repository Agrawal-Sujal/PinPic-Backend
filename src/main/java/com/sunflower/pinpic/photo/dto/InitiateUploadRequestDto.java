package com.sunflower.pinpic.photo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sunflower.pinpic.photo.entity.Location;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InitiateUploadRequestDto {
    private String name;
}
