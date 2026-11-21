package com.sunflower.pinpic.sync.controller;

import com.sunflower.pinpic.sync.dto.DeltaSyncPullResponseDto;
import com.sunflower.pinpic.sync.dto.SyncPushRequestDto;
import com.sunflower.pinpic.sync.dto.SyncPushResponseDto;
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

    @PostMapping("/push")
    public ResponseEntity<SyncPushResponseDto> pushMutations(
            @RequestBody SyncPushRequestDto request,
            Principal principal
    ) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(syncService.pushMutations(request, principal.getName()));
    }

    @GetMapping(value = {"/pull", "/deltas"})
    public ResponseEntity<DeltaSyncPullResponseDto> pullDeltas(
            @RequestParam(name = "cursor", defaultValue = "0") Long cursor,
            @RequestParam(name = "limit", defaultValue = "200") int limit,
            @RequestParam(name = "deviceId", required = false) String deviceId,
            Principal principal
    ) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(syncService.pullDeltas(principal.getName(), deviceId, cursor, limit));
    }
}
