package com.sunflower.pinpic.sync.controller;

import com.sunflower.pinpic.sync.dto.SyncBatchRequestDto;
import com.sunflower.pinpic.sync.dto.SyncBatchResponseDto;
import com.sunflower.pinpic.sync.dto.SyncPullRequestDto;
import com.sunflower.pinpic.sync.dto.SyncPullResponseDto;
import com.sunflower.pinpic.sync.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/batch")
    public ResponseEntity<SyncBatchResponseDto> processBatch(
            @RequestBody SyncBatchRequestDto request,
            Principal principal
    ) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(syncService.processBatch(request, principal.getName()));
    }

    @GetMapping("/pull")
    public ResponseEntity<SyncPullResponseDto> pullOperations(
            @RequestParam(name = "lastOperationId", defaultValue = "0") Long lastOperationId,
            @RequestParam(name = "batchSize", defaultValue = "50") int batchSize,
            @RequestParam(name = "deviceId", required = false) String deviceId,
            Principal principal
    ) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(syncService.pullOperations(principal.getName(), deviceId, lastOperationId, batchSize));
    }

    @PostMapping("/pull")
    public ResponseEntity<SyncPullResponseDto> pullOperationsPost(
            @RequestBody(required = false) SyncPullRequestDto request,
            Principal principal
    ) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String deviceId = request != null ? request.getDeviceId() : null;
        Long lastOperationId = (request != null && request.getLastOperationId() != null) ? request.getLastOperationId() : 0L;
        int batchSize = (request != null && request.getBatchSize() != null) ? request.getBatchSize() : 50;
        return ResponseEntity.ok(syncService.pullOperations(principal.getName(), deviceId, lastOperationId, batchSize));
    }
}
