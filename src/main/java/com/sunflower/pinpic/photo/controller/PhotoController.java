package com.sunflower.pinpic.photo.controller;

import com.sunflower.pinpic.photo.dto.ConfirmUploadRequestDto;
import com.sunflower.pinpic.photo.dto.DeletePhotosRequestDto;
import com.sunflower.pinpic.photo.dto.InitiateUploadRequestDto;
import com.sunflower.pinpic.photo.dto.InitiateUploadResponseDto;
import com.sunflower.pinpic.photo.entity.Photo;
import com.sunflower.pinpic.photo.service.PhotoService;
import com.sunflower.pinpic.user.entity.User;
import com.sunflower.pinpic.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;
    private final UserRepository userRepository;

    @PostMapping("/initiate-upload")
    public ResponseEntity<?> initiateUpload(@RequestBody InitiateUploadRequestDto request, Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        InitiateUploadResponseDto response = photoService.initiateUpload(request, user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm-upload")
    public ResponseEntity<Photo> confirmUpload(@RequestBody ConfirmUploadRequestDto request) {
        Photo updatedPhoto = photoService.confirmUpload(request);
        return ResponseEntity.ok(updatedPhoto);
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deletePhoto(@PathVariable String photoId, Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        photoService.deletePhoto(photoId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete-batch")
    public ResponseEntity<Void> deletePhotosBatch(@RequestBody DeletePhotosRequestDto request, Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        photoService.deletePhotos(request.getPhotoIds(), principal.getName());
        return ResponseEntity.ok().build();
    }
}
