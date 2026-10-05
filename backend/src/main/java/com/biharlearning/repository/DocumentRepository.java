package com.biharlearning.repository;

import com.biharlearning.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    /** Deduplication lookup: find an existing document by its SHA-256. */
    Optional<Document> findByDocumentHash(String documentHash);

    List<Document> findByStatus(String status);

    List<Document> findByCandidateForDeletionTrue();

    List<Document> findByHosted(boolean hosted);

    @Query("select coalesce(sum(d.fileSize),0) from Document d where d.hosted = true")
    long sumHostedBytes();

    long countByHosted(boolean hosted);

    long countByStatus(String status);
}
