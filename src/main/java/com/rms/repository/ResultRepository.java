//package com.rms.repository;
//
//import com.rms.entity.Result;
//import jakarta.transaction.Transactional;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import java.util.List;
//import java.util.Optional;
//
//public interface ResultRepository extends JpaRepository<Result, Long> {
//
//
//    @Query("SELECT r FROM Result r LEFT JOIN FETCH r.examination LEFT JOIN FETCH r.student " +
//            "WHERE r.examination.id = :examId AND r.facultyDetail.id = :fdId")
//    List<Result> findByExaminationIdAndProgramId(@Param("examId") Long examId,
//                                                       @Param("fdId") Long fdId);
//
//    @Query("SELECT r FROM Result r LEFT JOIN FETCH r.examination WHERE r.student.id = :studentId")
//    List<Result> findByStudentId(@Param("studentId") Long studentId);
//
//    List<Result> findByExaminationId(Long examinationId);
//
//    Optional<Result> findByExaminationIdAndProgramIdAndStudentId(
//            Long examId, Long fdId, Long studentId);
//
//    @Query("SELECT r FROM Result r WHERE r.examination.id = :examId " +
//            "AND r.grade <> 'NG' AND r.percentage IS NOT NULL")
//    List<Result> findRankableResults(@Param("examId") Long examId);
//
//    // ── Native SQL to delete result_marks rows for a student's results ────────
//    // Must use native SQL because result_marks is a join table not mapped as entity
//    @Modifying
//    @Transactional
//    @Query(value = "DELETE FROM result_marks WHERE result_id IN " +
//            "(SELECT id FROM results WHERE student_id = :studentId)",
//            nativeQuery = true)
//    void deleteResultMarksByStudentId(@Param("studentId") Long studentId);
//
//    // ── Native SQL to delete result rows for a student ────────────────────────
//    @Modifying
//    @Transactional
//    @Query(value = "DELETE FROM results WHERE student_id = :studentId", nativeQuery = true)
//    void deleteResultsByStudentId(@Param("studentId") Long studentId);
//
//    @Query("SELECT COUNT(r) FROM Result r WHERE r.facultyDetail.id = :fdId")
//    long countByFacultyDetailId(@Param("fdId") Long fdId);
//
//
//    @Modifying
//    @Transactional
//    @Query(value = "DELETE FROM result_marks WHERE result_id IN " +
//            "(SELECT id FROM results WHERE faculty_detail_id = :fdId)",
//            nativeQuery = true)
//    void deleteResultMarksByFacultyDetailId(@Param("fdId") Long fdId);
//
//    @Modifying
//    @Transactional
//    @Query(value = "DELETE FROM results WHERE faculty_detail_id = :fdId", nativeQuery = true)
//    void deleteResultsByFacultyDetailId(@Param("fdId") Long fdId);
//}




package com.rms.repository;

import com.rms.entity.Result;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {

    @Query("SELECT r FROM Result r LEFT JOIN FETCH r.examination LEFT JOIN FETCH r.student " +
            "WHERE r.examination.id = :examId AND r.program.id = :fdId")
    List<Result> findByExaminationIdAndProgramId(@Param("examId") Long examId,
                                                 @Param("fdId") Long fdId);

    @Query("SELECT r FROM Result r LEFT JOIN FETCH r.examination WHERE r.student.id = :studentId")
    List<Result> findByStudentId(@Param("studentId") Long studentId);

    List<Result> findByExaminationId(Long examinationId);

    @Query("SELECT COUNT(r) FROM Result r WHERE r.program.id = :fdId")
    long countByProgramId(@Param("fdId") Long fdId);

    Optional<Result> findByExaminationIdAndProgramIdAndStudentId(
            Long examId, Long fdId, Long studentId);

    @Query("SELECT r FROM Result r WHERE r.examination.id = :examId " +
            "AND r.grade <> 'NG' AND r.percentage IS NOT NULL")
    List<Result> findRankableResults(@Param("examId") Long examId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM result_marks WHERE result_id IN " +
            "(SELECT id FROM results WHERE student_id = :studentId)",
            nativeQuery = true)
    void deleteResultMarksByStudentId(@Param("studentId") Long studentId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM results WHERE student_id = :studentId", nativeQuery = true)
    void deleteResultsByStudentId(@Param("studentId") Long studentId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM result_marks WHERE result_id IN " +
            "(SELECT id FROM results WHERE faculty_detail_id = :fdId)",
            nativeQuery = true)
    void deleteResultMarksByProgramId(@Param("fdId") Long fdId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM results WHERE faculty_detail_id = :fdId", nativeQuery = true)
    void deleteResultsByProgramId(@Param("fdId") Long fdId);
}