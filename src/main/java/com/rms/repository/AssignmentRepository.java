package com.rms.repository;

import com.rms.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByGradeSectionId(Long gradeSectionId);
    void deleteByGradeSectionId(Long gradeSectionId);


}