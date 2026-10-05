package com.biharlearning.repository;

import com.biharlearning.entity.Syllabus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SyllabusRepository extends JpaRepository<Syllabus, UUID> {
    List<Syllabus> findByClassId(UUID classId);
    List<Syllabus> findByClassIdAndSubjectId(UUID classId, UUID subjectId);
}
