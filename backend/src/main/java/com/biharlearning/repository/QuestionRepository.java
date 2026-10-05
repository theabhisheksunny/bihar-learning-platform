package com.biharlearning.repository;

import com.biharlearning.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findByChapterId(UUID chapterId);
    List<Question> findByChapterIdAndCategory(UUID chapterId, String category);
}
