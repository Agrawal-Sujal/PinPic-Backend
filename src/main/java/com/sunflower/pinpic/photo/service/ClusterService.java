package com.sunflower.pinpic.photo.service;

import com.sunflower.pinpic.photo.dto.ClusterDto;
import com.sunflower.pinpic.photo.entity.Cluster;
import com.sunflower.pinpic.photo.repository.ClusterRepository;
import com.sunflower.pinpic.user.entity.User;
import com.sunflower.pinpic.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClusterService {

    private final ClusterRepository clusterRepository;
    private final UserRepository userRepository;

    public ClusterDto saveOrUpdateCluster(ClusterDto dto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        LocalDateTime now = LocalDateTime.now();
        Cluster cluster;

        if (dto.getClusterId() != null && !dto.getClusterId().isBlank()) {
            cluster = clusterRepository.findById(dto.getClusterId()).orElse(null);
            if (cluster != null) {
                if (!user.getId().equals(cluster.getUserId())) {
                    throw new IllegalArgumentException("Unauthorized to modify this cluster");
                }
                cluster.setLocation(dto.getLocation());
                cluster.setThumbnailPhotoId(dto.getThumbnailPhotoId());
                cluster.setPhotoCount(dto.getPhotoCount() != null ? dto.getPhotoCount() : cluster.getPhotoCount());
                if (dto.getPublicPhotoCount() != null) cluster.setPublicPhotoCount(dto.getPublicPhotoCount());
                if (dto.getPrivatePhotoCount() != null) cluster.setPrivatePhotoCount(dto.getPrivatePhotoCount());
                if (dto.getFriendPhotoCount() != null) cluster.setFriendPhotoCount(dto.getFriendPhotoCount());
                if (dto.getVisibility() != null) {
                    cluster.setVisibility(dto.getVisibility());
                } else {
                    cluster.updateVisibility();
                }
                cluster.setUpdatedAt(now);
                return mapToDto(clusterRepository.save(cluster));
            }
        }

        String clusterId = (dto.getClusterId() != null && !dto.getClusterId().isBlank())
                ? dto.getClusterId()
                : UUID.randomUUID().toString();

        cluster = Cluster.builder()
                .clusterId(clusterId)
                .userId(user.getId())
                .location(dto.getLocation())
                .thumbnailPhotoId(dto.getThumbnailPhotoId())
                .photoCount(dto.getPhotoCount() != null ? dto.getPhotoCount() : 0)
                .publicPhotoCount(dto.getPublicPhotoCount() != null ? dto.getPublicPhotoCount() : 0)
                .privatePhotoCount(dto.getPrivatePhotoCount() != null ? dto.getPrivatePhotoCount() : 0)
                .friendPhotoCount(dto.getFriendPhotoCount() != null ? dto.getFriendPhotoCount() : 0)
                .visibility(dto.getVisibility())
                .createdAt(now)
                .updatedAt(now)
                .build();

        if (cluster.getVisibility() == null) {
            cluster.updateVisibility();
        }

        return mapToDto(clusterRepository.save(cluster));
    }

    public List<ClusterDto> getUserClusters(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        return clusterRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public void deleteCluster(String clusterId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> new IllegalArgumentException("Cluster not found: " + clusterId));

        if (!user.getId().equals(cluster.getUserId())) {
            throw new IllegalArgumentException("Unauthorized to delete this cluster");
        }

        clusterRepository.delete(cluster);
    }

    private ClusterDto mapToDto(Cluster cluster) {
        return ClusterDto.builder()
                .clusterId(cluster.getClusterId())
                .userId(cluster.getUserId())
                .location(cluster.getLocation())
                .thumbnailPhotoId(cluster.getThumbnailPhotoId())
                .photoCount(cluster.getPhotoCount())
                .visibility(cluster.getVisibility())
                .publicPhotoCount(cluster.getPublicPhotoCount())
                .privatePhotoCount(cluster.getPrivatePhotoCount())
                .friendPhotoCount(cluster.getFriendPhotoCount())
                .createdAt(cluster.getCreatedAt())
                .updatedAt(cluster.getUpdatedAt())
                .build();
    }
}
