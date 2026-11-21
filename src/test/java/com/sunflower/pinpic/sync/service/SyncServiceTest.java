package com.sunflower.pinpic.sync.service;

import com.sunflower.pinpic.photo.entity.Photo;
import com.sunflower.pinpic.photo.entity.Visibility;
import com.sunflower.pinpic.photo.repository.ClusterRepository;
import com.sunflower.pinpic.photo.repository.PhotoRepository;
import com.sunflower.pinpic.storage.S3Service;
import com.sunflower.pinpic.sync.dto.*;
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
    private S3Service s3Service;

    @Mock
    private jakarta.persistence.EntityManager entityManager;

    @Mock
    private com.sunflower.pinpic.device.repository.DeviceRepository deviceRepository;

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
    void testPullDeltas_UserNotFoundThrowsException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                syncService.pullDeltas("unknown@example.com", "device-1", 0L, 20)
        );
    }

    @Test
    void testPullDeltas_DistinguishesEchoAndExternalChanges() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Photo 1: Modified by device-A (Echo for device-A)
        Photo photo1 = Photo.builder()
                .photoId("photo-1")
                .userId("user-123")
                .name("local.jpg")
                .revision(101L)
                .lastModifiedDeviceId("device-A")
                .build();

        // Photo 2: Modified by device-B (External for device-A)
        Photo photo2 = Photo.builder()
                .photoId("photo-2")
                .userId("user-123")
                .name("cloud.jpg")
                .s3Key("user-123/cloud.jpg")
                .revision(102L)
                .lastModifiedDeviceId("device-B")
                .build();

        when(photoRepository.findByUserIdAndRevisionGreaterThanOrderByRevisionAsc(
                eq("user-123"), eq(100L), any(PageRequest.class)
        )).thenReturn(List.of(photo1, photo2));

        when(clusterRepository.findByUserIdAndRevisionGreaterThanOrderByRevisionAsc(
                eq("user-123"), eq(100L), any(PageRequest.class)
        )).thenReturn(List.of());

        when(s3Service.getDownloadPresignedUrl("user-123/cloud.jpg"))
                .thenReturn("https://s3.amazonaws.com/presigned-photo-2");

        var response = syncService.pullDeltas("test@example.com", "device-A", 100L, 50);

        assertNotNull(response);
        assertEquals(102L, response.getNextCursor());
        assertEquals(2, response.getPhotos().size());

        // First item is an ECHO from device-A
        var p1 = response.getPhotos().get(0);
        assertEquals("photo-1", p1.getPhotoId());
        assertTrue(p1.getIsEcho());
        assertNull(p1.getDownloadUrl(), "Echoes must not include heavy download URLs");

        // Second item is an EXTERNAL change from device-B
        var p2 = response.getPhotos().get(1);
        assertEquals("photo-2", p2.getPhotoId());
        assertFalse(p2.getIsEcho());
        assertEquals("https://s3.amazonaws.com/presigned-photo-2", p2.getDownloadUrl());
        assertEquals("cloud.jpg", p2.getName());
    }

    @Test
    void testPushMutations_SavesClustersAndPhotosWithDeviceId() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        var clusterDto = com.sunflower.pinpic.sync.dto.ClusterSyncDto.builder()
                .globalClusterId("cluster-uuid-1")
                .photoCount(5)
                .build();

        var photoDto = com.sunflower.pinpic.sync.dto.PhotoSyncDto.builder()
                .globalPhotoId("photo-uuid-1")
                .globalClusterId("cluster-uuid-1")
                .name("my-photo.jpg")
                .build();

        var request = com.sunflower.pinpic.sync.dto.SyncPushRequestDto.builder()
                .deviceId("device-A")
                .clusters(List.of(clusterDto))
                .photos(List.of(photoDto))
                .build();

        when(clusterRepository.findById("cluster-uuid-1")).thenReturn(Optional.empty());
        when(photoRepository.findById("photo-uuid-1")).thenReturn(Optional.empty());

        var response = syncService.pushMutations(request, "test@example.com");

        assertEquals("SUCCESS", response.getStatus());

        verify(clusterRepository).save(argThat(cluster ->
                "cluster-uuid-1".equals(cluster.getClusterId()) &&
                "device-A".equals(cluster.getLastModifiedDeviceId()) &&
                "user-123".equals(cluster.getUserId())
        ));

        verify(photoRepository).save(argThat(photo ->
                "photo-uuid-1".equals(photo.getPhotoId()) &&
                "device-A".equals(photo.getLastModifiedDeviceId()) &&
                "user-123".equals(photo.getUserId())
        ));
    }
}
