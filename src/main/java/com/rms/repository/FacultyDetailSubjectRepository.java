package com.rms.repository;

import com.rms.entity.FacultyDetailSubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacultyDetailSubjectRepository extends JpaRepository<FacultyDetailSubject, Long> {
    List<FacultyDetailSubject> findByFacultyDetailId(Long facultyDetailId);
    Optional<FacultyDetailSubject> findByFacultyDetailIdAndSubjectId(Long facultyDetailId, Long subjectId);
    void deleteByFacultyDetailIdAndSubjectId(Long facultyDetailId, Long subjectId);
}
