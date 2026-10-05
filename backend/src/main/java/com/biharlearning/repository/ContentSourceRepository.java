package com.biharlearning.repository;

import com.biharlearning.entity.ContentSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ContentSourceRepository extends JpaRepository<ContentSource, UUID> {
    Optional<ContentSource> findBySourceKey(String sourceKey);
}
