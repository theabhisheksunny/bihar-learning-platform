package com.biharlearning.repository;

import com.biharlearning.entity.Mcq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface McqRepository extends JpaRepository<Mcq, UUID> {
    List<Mcq> findByChapterId(UUID chapterId);
}
