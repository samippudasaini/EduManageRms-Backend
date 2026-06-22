package com.rms.repository;

import com.rms.entity.GradeSectionMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GradeSectionMappingRepository extends JpaRepository<GradeSectionMapping, Long> {
    List<GradeSectionMapping> findByGradeId(Long gradeId);
    void deleteByGradeId(Long gradeId);
}
