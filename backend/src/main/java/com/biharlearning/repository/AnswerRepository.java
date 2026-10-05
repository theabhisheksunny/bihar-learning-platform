package com.biharlearning.repository;

import com.biharlearning.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnswerRepository extends JpaRepository<Answer, UUID> {
    List<Answer> findByQuestionId(UUID questionId);
    List<Answer> findByQuestionIdIn(List<UUID> questionIds);
}
