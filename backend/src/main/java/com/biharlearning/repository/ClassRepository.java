package com.biharlearning.repository;

import com.biharlearning.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClassRepository extends JpaRepository<ClassEntity, UUID> {
    List<ClassEntity> findAllByOrderByDisplayOrderAsc();
    Optional<ClassEntity> findByCode(String code);
}
