package com.biharlearning.repository;

import com.biharlearning.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {
    Page<Book> findBySubjectId(UUID subjectId, Pageable pageable);
    Optional<Book> findBySlug(String slug);
    long countBySubjectId(UUID subjectId);
}
