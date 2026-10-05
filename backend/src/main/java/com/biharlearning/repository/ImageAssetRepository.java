package com.biharlearning.repository;

import com.biharlearning.entity.ImageAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ImageAssetRepository extends JpaRepository<ImageAsset, UUID> {
    Optional<ImageAsset> findByImageHashAndVariant(String imageHash, String variant);

    @Query("select coalesce(sum(i.fileSize),0) from ImageAsset i where i.hosted = true")
    long sumHostedBytes();

    long countByHosted(boolean hosted);
}
