package com.biharlearning.repository;

import com.biharlearning.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    List<Subject> findByClassIdOrderByDisplayOrderAsc(UUID classId);
    Optional<Subject> findByClassIdAndCode(UUID classId, String code);
}
