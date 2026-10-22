package com.sunflower.pinpic.sync.repository;

import com.sunflower.pinpic.sync.entity.CloudSyncOperation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CloudSyncOperationRepository extends JpaRepository<CloudSyncOperation, Long> {

    @Query("SELECT o FROM CloudSyncOperation o WHERE o.userId = :userId " +
           "AND o.operationId > :lastOperationId " +
           "AND (:deviceId IS NULL OR o.deviceId IS NULL OR o.deviceId <> :deviceId) " +
           "ORDER BY o.operationId ASC")
    List<CloudSyncOperation> findOperationsToPull(
            @Param("userId") String userId,
            @Param("lastOperationId") Long lastOperationId,
            @Param("deviceId") String deviceId,
            Pageable pageable
    );
}

