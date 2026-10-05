package com.biharlearning.repository;

import com.biharlearning.entity.ChapterDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChapterDocumentRepository extends JpaRepository<ChapterDocument, UUID> {
    List<ChapterDocument> findByChapterId(UUID chapterId);
    List<ChapterDocument> findByDocumentId(UUID documentId);
    long countByDocumentId(UUID documentId);
}
