package com.sunflower.pinpic.photo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeletePhotosRequestDto {
    private List<String> photoIds;
}
