package com.rms.repository;

import com.rms.entity.ProgramSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ProgramSubjectRepository extends JpaRepository<ProgramSubject, Long> {
    List<ProgramSubject> findByProgramId(Long facultyDetailId);
    Optional<ProgramSubject> findByProgramIdAndSubjectId(Long facultyDetailId, Long subjectId);
    void deleteByProgramIdAndSubjectId(Long facultyDetailId, Long subjectId);

    @Modifying
    @Transactional
    void deleteBySubjectId(Long subjectId);
}
