package com.rms.repository;

import com.rms.entity.ProgramSubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgramSubjectRepository extends JpaRepository<ProgramSubject, Long> {
    List<ProgramSubject> findByProgramId(Long facultyDetailId);
    Optional<ProgramSubject> findByProgramIdAndSubjectId(Long facultyDetailId, Long subjectId);
    void deleteByProgramIdAndSubjectId(Long facultyDetailId, Long subjectId);
}
