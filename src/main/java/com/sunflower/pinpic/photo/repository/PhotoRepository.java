package com.sunflower.pinpic.photo.repository;

import com.sunflower.pinpic.photo.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, String> {
    Optional<Photo> findByS3Key(String s3Key);
}
