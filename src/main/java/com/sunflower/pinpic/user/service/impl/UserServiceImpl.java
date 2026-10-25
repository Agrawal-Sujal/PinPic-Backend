package com.sunflower.pinpic.user.service.impl;

import com.sunflower.pinpic.photo.dto.InitiateUploadResponseDto;
import com.sunflower.pinpic.photo.dto.UserClusterResponseDto;
import com.sunflower.pinpic.photo.dto.UserPhotoResponseDto;
import com.sunflower.pinpic.photo.entity.Cluster;
import com.sunflower.pinpic.photo.entity.Photo;
import com.sunflower.pinpic.photo.entity.Visibility;
import com.sunflower.pinpic.photo.repository.ClusterRepository;
import com.sunflower.pinpic.photo.repository.PhotoRepository;
import com.sunflower.pinpic.storage.S3Service;
import com.sunflower.pinpic.user.dto.ProfilePhotoConfirmResponseDto;
import com.sunflower.pinpic.user.dto.UpdateProfileRequestDto;
import com.sunflower.pinpic.user.dto.UserProfileResponseDto;
import com.sunflower.pinpic.user.entity.Follower;
import com.sunflower.pinpic.user.entity.User;
import com.sunflower.pinpic.user.repository.FollowerRepository;
import com.sunflower.pinpic.user.repository.UserRepository;
import com.sunflower.pinpic.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FollowerRepository followerRepository;
    private final S3Service s3Service;
    private final PhotoRepository photoRepository;
    private final ClusterRepository clusterRepository;

    @Override
    public void setUsername(String email, String username) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setUsername(username);
        userRepository.save(user);
    }

    @Override
    public UserProfileResponseDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return mapToDto(user);
    }

    @Override
    public void updateProfile(String email, UpdateProfileRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setName(requestDto.getName());
        user.setMobileNumber(requestDto.getMobileNumber());
        user.setLocale(requestDto.getLocale());
        user.setPictureUrl(requestDto.getPictureUrl());
        
        userRepository.save(user);
        mapToDto(user);
    }

    @Override
    public void follow(String email, String followingId) {
        User followerUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (followerUser.getId().equals(followingId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }
        
        User followingUser = userRepository.findById(followingId)
                .orElseThrow(() -> new IllegalArgumentException("Following user not found"));
                
        if (followerRepository.existsByFollowerIdAndFollowingId(followerUser.getId(), followingId)) {
            return;
        }
        
        Follower follow = Follower.builder()
                .followerId(followerUser.getId())
                .followingId(followingId)
                .build();
        followerRepository.save(follow);
        
        followerUser.setFollowingCount(followerUser.getFollowingCount() + 1);
        followingUser.setFollowerCount(followingUser.getFollowerCount() + 1);
        
        userRepository.save(followerUser);
        userRepository.save(followingUser);
    }

    @Override
    public void unfollow(String email, String followingId) {
        User followerUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
        Follower follow = followerRepository.findByFollowerIdAndFollowingId(followerUser.getId(), followingId)
                .orElse(null);
                
        if (follow == null) {
            return;
        }
        
        User followingUser = userRepository.findById(followingId)
                .orElseThrow(() -> new IllegalArgumentException("Following user not found"));
                
        followerRepository.delete(follow);
        
        followerUser.setFollowingCount(Math.max(0, followerUser.getFollowingCount() - 1));
        followingUser.setFollowerCount(Math.max(0, followingUser.getFollowerCount() - 1));
        
        userRepository.save(followerUser);
        userRepository.save(followingUser);
    }

    @Override
    public List<UserProfileResponseDto> getFollowers(String userId) {
        List<Follower> followers = followerRepository.findAllByFollowingId(userId);
        List<String> followerIds = followers.stream()
                .map(Follower::getFollowerId)
                .collect(Collectors.toList());
                
        return userRepository.findByIdIn(followerIds).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserProfileResponseDto> getFollowings(String userId) {
        List<Follower> followings = followerRepository.findAllByFollowerId(userId);
        List<String> followingIds = followings.stream()
                .map(Follower::getFollowingId)
                .collect(Collectors.toList());
                
        return userRepository.findByIdIn(followingIds).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserProfileResponseDto> searchUsers(String email, String query) {
        if (query == null || query.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        User currentUser = (email != null) ? userRepository.findByEmail(email).orElse(null) : null;
        String currentUserId = (currentUser != null) ? currentUser.getId() : "";

        List<User> users = userRepository.searchUsers(query.trim(), currentUserId);

        return users.stream().map(user -> {
            UserProfileResponseDto dto = mapToDto(user);
            if (currentUser != null) {
                dto.setIsFollowing(followerRepository.existsByFollowerIdAndFollowingId(currentUser.getId(), user.getId()));
            } else {
                dto.setIsFollowing(false);
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public UserProfileResponseDto getUserProfileById(String email, String userId) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserProfileResponseDto dto = mapToDto(targetUser);
        if (email != null) {
            User currentUser = userRepository.findByEmail(email).orElse(null);
            if (currentUser != null) {
                dto.setIsFollowing(followerRepository.existsByFollowerIdAndFollowingId(currentUser.getId(), targetUser.getId()));
            }
        }
        return dto;
    }

    @Override
    public List<UserPhotoResponseDto> getUserPhotos(String email, String userId) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found"));

        List<Visibility> allowedVisibilities = new ArrayList<>();
        allowedVisibilities.add(Visibility.PUBLIC);

        if (currentUser.getId().equals(userId)) {
            allowedVisibilities.add(Visibility.FRIENDS);
            allowedVisibilities.add(Visibility.PRIVATE);
        } else {
            boolean isFollowing = followerRepository.existsByFollowerIdAndFollowingId(currentUser.getId(), userId);
            boolean isFollower = followerRepository.existsByFollowerIdAndFollowingId(userId, currentUser.getId());
            boolean areFriends = isFollowing || isFollower;

            if (areFriends) {
                allowedVisibilities.add(Visibility.FRIENDS);
            }
        }

        List<Photo> photos = photoRepository.findVisiblePhotosForUser(userId, allowedVisibilities);

        // Sort descending by takenAt, then uploadedAt
        photos.sort((p1, p2) -> {
            LocalDateTime t1 = p1.getTakenAt() != null ? p1.getTakenAt() : p1.getUploadedAt();
            LocalDateTime t2 = p2.getTakenAt() != null ? p2.getTakenAt() : p2.getUploadedAt();
            if (t1 == null && t2 == null) return 0;
            if (t1 == null) return 1;
            if (t2 == null) return -1;
            return t2.compareTo(t1);
        });

        return photos.stream().map(photo -> {
            String downloadUrl = null;
            if (photo.getS3Key() != null && !photo.getS3Key().isBlank()) {
                try {
                    downloadUrl = s3Service.getDownloadPresignedUrl(photo.getS3Key());
                } catch (Exception ignored) {
                }
            }
            return UserPhotoResponseDto.builder()
                    .photoId(photo.getPhotoId())
                    .userId(photo.getUserId())
                    .name(photo.getName())
                    .downloadUrl(downloadUrl)
                    .visibility(photo.getVisibility() != null ? photo.getVisibility().name() : null)
                    .takenAt(photo.getTakenAt() != null ? photo.getTakenAt().toString() : null)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<UserClusterResponseDto> getUserClusters(String email, String userId) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found"));

        List<Visibility> allowedVisibilities = new ArrayList<>();
        allowedVisibilities.add(Visibility.PUBLIC);

        if (currentUser.getId().equals(userId)) {
            allowedVisibilities.add(Visibility.FRIENDS);
            allowedVisibilities.add(Visibility.PRIVATE);
        } else {
            boolean isFollowing = followerRepository.existsByFollowerIdAndFollowingId(currentUser.getId(), userId);
            boolean isFollower = followerRepository.existsByFollowerIdAndFollowingId(userId, currentUser.getId());
            if (isFollowing || isFollower) {
                allowedVisibilities.add(Visibility.FRIENDS);
            }
        }

        List<Cluster> clusters = clusterRepository.findVisibleClustersForUser(userId, allowedVisibilities);
        List<UserClusterResponseDto> result = new ArrayList<>();
        Set<String> processedClusterIds = new HashSet<>();

        for (Cluster cluster : clusters) {
            processedClusterIds.add(cluster.getClusterId());

            List<Photo> clusterPhotos = photoRepository.findByClusterId(cluster.getClusterId());
            List<Photo> visiblePhotos = clusterPhotos.stream()
                    .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
                    .filter(p -> p.getVisibility() != null && allowedVisibilities.contains(p.getVisibility()))
                    .sorted((p1, p2) -> {
                        LocalDateTime t1 = p1.getTakenAt() != null ? p1.getTakenAt() : p1.getUploadedAt();
                        LocalDateTime t2 = p2.getTakenAt() != null ? p2.getTakenAt() : p2.getUploadedAt();
                        if (t1 == null && t2 == null) return 0;
                        if (t1 == null) return 1;
                        if (t2 == null) return -1;
                        return t2.compareTo(t1);
                    })
                    .collect(Collectors.toList());

            if (visiblePhotos.isEmpty()) {
                continue;
            }

            List<UserPhotoResponseDto> photoDtos = visiblePhotos.stream().map(photo -> {
                String downloadUrl = null;
                if (photo.getS3Key() != null && !photo.getS3Key().isBlank()) {
                    try {
                        downloadUrl = s3Service.getDownloadPresignedUrl(photo.getS3Key());
                    } catch (Exception ignored) {
                    }
                }
                return UserPhotoResponseDto.builder()
                        .photoId(photo.getPhotoId())
                        .userId(photo.getUserId())
                        .name(photo.getName())
                        .downloadUrl(downloadUrl)
                        .visibility(photo.getVisibility() != null ? photo.getVisibility().name() : null)
                        .takenAt(photo.getTakenAt() != null ? photo.getTakenAt().toString() : null)
                        .build();
            }).collect(Collectors.toList());

            String thumbnailUrl = null;
            if (cluster.getThumbnailPhotoId() != null) {
                Photo thumb = photoRepository.findById(cluster.getThumbnailPhotoId()).orElse(null);
                if (thumb != null && thumb.getS3Key() != null) {
                    try {
                        thumbnailUrl = s3Service.getDownloadPresignedUrl(thumb.getS3Key());
                    } catch (Exception ignored) {
                    }
                }
            }
            if (thumbnailUrl == null && !photoDtos.isEmpty()) {
                thumbnailUrl = photoDtos.get(0).getDownloadUrl();
            }

            result.add(UserClusterResponseDto.builder()
                    .clusterId(cluster.getClusterId())
                    .userId(cluster.getUserId())
                    .location(cluster.getLocation())
                    .thumbnailPhotoId(cluster.getThumbnailPhotoId())
                    .thumbnailUrl(thumbnailUrl)
                    .photoCount(photoDtos.size())
                    .visibility(cluster.getVisibility() != null ? cluster.getVisibility() : cluster.calculateVisibility())
                    .photos(photoDtos)
                    .build());
        }

        // Include any geo-tagged photos not in a named cluster
        List<Photo> allVisibleUserPhotos = photoRepository.findVisiblePhotosForUser(userId, allowedVisibilities);
        for (Photo p : allVisibleUserPhotos) {
            if (p.getLocation() != null && p.getLocation().getLatitude() != null && p.getLocation().getLongitude() != null) {
                if (p.getClusterId() == null || p.getClusterId().isBlank() || !processedClusterIds.contains(p.getClusterId())) {
                    String downloadUrl = null;
                    if (p.getS3Key() != null && !p.getS3Key().isBlank()) {
                        try {
                            downloadUrl = s3Service.getDownloadPresignedUrl(p.getS3Key());
                        } catch (Exception ignored) {
                        }
                    }
                    UserPhotoResponseDto photoDto = UserPhotoResponseDto.builder()
                            .photoId(p.getPhotoId())
                            .userId(p.getUserId())
                            .name(p.getName())
                            .downloadUrl(downloadUrl)
                            .visibility(p.getVisibility() != null ? p.getVisibility().name() : null)
                            .takenAt(p.getTakenAt() != null ? p.getTakenAt().toString() : null)
                            .build();

                    result.add(UserClusterResponseDto.builder()
                            .clusterId("photo_" + p.getPhotoId())
                            .userId(p.getUserId())
                            .location(p.getLocation())
                            .thumbnailPhotoId(p.getPhotoId())
                            .thumbnailUrl(downloadUrl)
                            .photoCount(1)
                            .visibility(p.getVisibility())
                            .photos(java.util.Collections.singletonList(photoDto))
                            .build());
                }
            }
        }

        return result;
    }

    @Override
    public InitiateUploadResponseDto initiateProfilePhotoUpload(String email, String extension) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        String s3Key = "profiles/" + user.getId() + "/" + UUID.randomUUID() + (extension != null ? "." + extension : "");
        String presignedUrl = s3Service.getProfileUploadPresignedUrl(s3Key);
        
        return new InitiateUploadResponseDto(s3Key, presignedUrl);
    }

    @Override
    public com.sunflower.pinpic.user.dto.ProfilePhotoConfirmResponseDto confirmProfilePhotoUpload(String email, String s3Key) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        String publicUrl = s3Service.getPublicUrlForProfile(s3Key);
        
        return new ProfilePhotoConfirmResponseDto(publicUrl);
    }

    private UserProfileResponseDto mapToDto(User user) {
        return UserProfileResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .name(user.getName())
                .mobileNumber(user.getMobileNumber())
                .locale(user.getLocale())
                .pictureUrl(user.getPictureUrl())
                .followerCount(user.getFollowerCount())
                .followingCount(user.getFollowingCount())
                .build();
    }
}
