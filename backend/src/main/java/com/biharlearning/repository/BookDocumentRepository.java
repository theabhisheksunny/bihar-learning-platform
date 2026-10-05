package com.biharlearning.repository;

import com.biharlearning.entity.BookDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookDocumentRepository extends JpaRepository<BookDocument, UUID> {
    List<BookDocument> findByBookId(UUID bookId);
    List<BookDocument> findByDocumentId(UUID documentId);
    long countByDocumentId(UUID documentId);
}
