package com.biharlearning.repository;

import com.biharlearning.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {
    List<DocumentVersion> findByDocumentIdOrderByVersionDesc(UUID documentId);
    long countByDocumentId(UUID documentId);
}
