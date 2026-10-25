package com.sunflower.pinpic.photo.repository;

import com.sunflower.pinpic.photo.entity.Photo;
import com.sunflower.pinpic.photo.entity.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, String> {
    Optional<Photo> findByS3Key(String s3Key);
    List<Photo> findByUserId(String userId);
    List<Photo> findByClusterId(String clusterId);

    @Query("SELECT p FROM Photo p WHERE p.userId = :userId AND (p.isDeleted = false OR p.isDeleted IS NULL) AND p.visibility IN :visibilities")
    List<Photo> findVisiblePhotosForUser(
        @Param("userId") String userId,
        @Param("visibilities") Collection<Visibility> visibilities
    );
}
