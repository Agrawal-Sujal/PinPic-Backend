package com.sunflower.pinpic.photo.controller;

import com.sunflower.pinpic.photo.dto.ClusterDto;
import com.sunflower.pinpic.photo.service.ClusterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/clusters")
@RequiredArgsConstructor
public class ClusterController {

    private final ClusterService clusterService;

    @PostMapping
    public ResponseEntity<ClusterDto> saveOrUpdateCluster(@RequestBody ClusterDto dto, Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(clusterService.saveOrUpdateCluster(dto, principal.getName()));
    }

    @GetMapping
    public ResponseEntity<List<ClusterDto>> getUserClusters(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(clusterService.getUserClusters(principal.getName()));
    }

    @DeleteMapping("/{clusterId}")
    public ResponseEntity<Void> deleteCluster(@PathVariable String clusterId, Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        clusterService.deleteCluster(clusterId, principal.getName());
        return ResponseEntity.ok().build();
    }
}
