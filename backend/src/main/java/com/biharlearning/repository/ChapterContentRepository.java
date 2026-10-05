package com.biharlearning.repository;

import com.biharlearning.entity.ChapterContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChapterContentRepository extends JpaRepository<ChapterContent, UUID> {
    List<ChapterContent> findByChapterId(UUID chapterId);
    List<ChapterContent> findByChapterIdAndContentType(UUID chapterId, String contentType);
    List<ChapterContent> findByAiReviewStatus(String aiReviewStatus);
}
