package com.biharlearning.repository;

import com.biharlearning.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChapterRepository extends JpaRepository<Chapter, UUID> {
    List<Chapter> findByBookIdOrderByChapterNumberAsc(UUID bookId);
    Optional<Chapter> findByBookIdAndSlug(UUID bookId, String slug);
    long countByBookId(UUID bookId);
}
