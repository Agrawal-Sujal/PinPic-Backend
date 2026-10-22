package com.sunflower.pinpic.sync.service;

import com.sunflower.pinpic.photo.entity.Photo;
import com.sunflower.pinpic.photo.entity.Visibility;
import com.sunflower.pinpic.photo.repository.ClusterRepository;
import com.sunflower.pinpic.photo.repository.PhotoRepository;
import com.sunflower.pinpic.storage.S3Service;
import com.sunflower.pinpic.sync.dto.SyncPullResponseDto;
import com.sunflower.pinpic.sync.entity.CloudSyncOperation;
import com.sunflower.pinpic.sync.repository.CloudSyncOperationRepository;
import com.sunflower.pinpic.user.entity.User;
import com.sunflower.pinpic.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClusterRepository clusterRepository;

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private CloudSyncOperationRepository cloudSyncOperationRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private SyncService syncService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user-123")
                .email("test@example.com")
                .build();
    }

    @Test
    void testPullOperations_ReturnsExpectedOperationsAndExcludesSameDevice() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // CloudSyncOperation stores only the minimal identifiers
        CloudSyncOperation op1 = CloudSyncOperation.builder()
                .operationId(10L)
                .userId("user-123")
                .deviceId("device-other")
                .entityType("PHOTOS")
                .operation("ADD")
                .entityId("photo-1")
                .createdAt(LocalDateTime.now())
                .build();

        CloudSyncOperation op2 = CloudSyncOperation.builder()
                .operationId(15L)
                .userId("user-123")
                .deviceId("device-other")
                .entityType("PHOTOS")
                .operation("DELETE")
                .entityId("photo-2")
                .createdAt(LocalDateTime.now())
                .build();

        when(cloudSyncOperationRepository.findOperationsToPull(
                eq("user-123"),
                eq(5L),
                eq("device-current"),
                eq(PageRequest.of(0, 10))
        )).thenReturn(List.of(op1, op2));

        // Full photo details fetched from database on pull
        Photo photo1 = Photo.builder()
                .photoId("photo-1")
                .userId("user-123")
                .name("vacation.jpg")
                .s3Key("user-123/photo-1.jpg")
                .visibility(Visibility.PRIVATE)
                .isDeleted(false)
                .build();

        when(photoRepository.findById("photo-1")).thenReturn(Optional.of(photo1));

        when(s3Service.getDownloadPresignedUrl("user-123/photo-1.jpg"))
                .thenReturn("https://s3.amazonaws.com/presigned-photo-1");

        SyncPullResponseDto response = syncService.pullOperations("test@example.com", "device-current", 5L, 10);

        assertNotNull(response);
        assertEquals(2, response.getOperations().size());
        assertEquals(15L, response.getLastOperationId());
        assertFalse(response.isHasMore());

        // Check first operation (ADD)
        assertEquals(10L, response.getOperations().get(0).getOperationId());
        assertEquals("PHOTOS", response.getOperations().get(0).getEntityType());
        assertEquals("ADD", response.getOperations().get(0).getOperation());
        assertEquals("https://s3.amazonaws.com/presigned-photo-1", response.getOperations().get(0).getDownloadUrl());
        assertNotNull(response.getOperations().get(0).getPhoto());
        assertEquals("vacation.jpg", response.getOperations().get(0).getPhoto().getName());
        assertEquals("user-123/photo-1.jpg", response.getOperations().get(0).getPhoto().getS3Key());

        // Check second operation (DELETE)
        assertEquals(15L, response.getOperations().get(1).getOperationId());
        assertEquals("DELETE", response.getOperations().get(1).getOperation());
        assertEquals("photo-2", response.getOperations().get(1).getEntityId());
        assertNull(response.getOperations().get(1).getDownloadUrl());
    }

    @Test
    void testPullOperations_UserNotFoundThrowsException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                syncService.pullOperations("unknown@example.com", "device-1", 0L, 20)
        );
    }
}
