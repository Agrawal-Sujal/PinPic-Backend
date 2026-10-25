package com.sunflower.pinpic.user.repository;

import com.sunflower.pinpic.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    List<User> findByIdIn(List<String> ids);

    @Query("SELECT u FROM User u WHERE (:currentUserId = '' OR u.id != :currentUserId) AND ((u.name IS NOT NULL AND LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%'))) OR (u.username IS NOT NULL AND LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))))")
    List<User> searchUsers(@Param("query") String query, @Param("currentUserId") String currentUserId);
}
