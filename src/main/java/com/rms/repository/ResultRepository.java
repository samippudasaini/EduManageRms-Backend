package com.rms.repository;

import com.rms.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {

    @Query("SELECT r FROM Result r LEFT JOIN FETCH r.examination LEFT JOIN FETCH r.student WHERE r.examination.id = :examId AND r.facultyDetail.id = :fdId")
    List<Result> findByExaminationIdAndFacultyDetailId(@Param("examId") Long examId, @Param("fdId") Long fdId);

    @Query("SELECT r FROM Result r LEFT JOIN FETCH r.examination WHERE r.student.id = :studentId")
    List<Result> findByStudentId(@Param("studentId") Long studentId);

    List<Result> findByExaminationId(Long examinationId);

    Optional<Result> findByExaminationIdAndFacultyDetailIdAndStudentId(Long examId, Long fdId, Long studentId);

    @Query("SELECT r FROM Result r WHERE r.examination.id = :examId AND r.grade <> 'NG' AND r.percentage IS NOT NULL")
    List<Result> findRankableResults(@Param("examId") Long examId);
}