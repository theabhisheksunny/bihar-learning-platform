package com.biharlearning.repository;

import com.biharlearning.entity.ContentChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContentChangeLogRepository extends JpaRepository<ContentChangeLog, UUID> {
    List<ContentChangeLog> findByStatusOrderByDetectedAtDesc(String status);
    long countByStatus(String status);
}
