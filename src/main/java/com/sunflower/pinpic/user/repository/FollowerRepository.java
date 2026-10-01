package com.sunflower.pinpic.user.repository;

import com.sunflower.pinpic.user.entity.Follower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowerRepository extends JpaRepository<Follower, String> {
    Optional<Follower> findByFollowerIdAndFollowingId(String followerId, String followingId);
    List<Follower> findAllByFollowerId(String followerId);
    List<Follower> findAllByFollowingId(String followingId);
    boolean existsByFollowerIdAndFollowingId(String followerId, String followingId);
}
