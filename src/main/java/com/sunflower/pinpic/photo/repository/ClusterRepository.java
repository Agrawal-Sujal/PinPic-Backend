package com.sunflower.pinpic.photo.repository;

import com.sunflower.pinpic.photo.entity.Cluster;
import com.sunflower.pinpic.photo.entity.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ClusterRepository extends JpaRepository<Cluster, String> {
    List<Cluster> findByUserId(String userId);

    @Query("SELECT c FROM Cluster c WHERE c.userId = :userId AND (c.visibility IN :visibilities OR c.visibility IS NULL) AND c.location IS NOT NULL")
    List<Cluster> findVisibleClustersForUser(
            @Param("userId") String userId,
            @Param("visibilities") Collection<Visibility> visibilities
    );
}
