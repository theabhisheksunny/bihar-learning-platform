package com.biharlearning.repository;

import com.biharlearning.entity.SyncJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SyncJobRepository extends JpaRepository<SyncJob, UUID> {
    List<SyncJob> findTop50ByOrderByCreatedAtDesc();
    List<SyncJob> findBySourceIdOrderByCreatedAtDesc(UUID sourceId);
    long countByStatus(String status);
}
