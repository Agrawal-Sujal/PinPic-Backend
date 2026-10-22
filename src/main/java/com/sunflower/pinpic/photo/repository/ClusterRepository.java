package com.sunflower.pinpic.photo.repository;

import com.sunflower.pinpic.photo.entity.Cluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClusterRepository extends JpaRepository<Cluster, String> {
    List<Cluster> findByUserId(String userId);
}
