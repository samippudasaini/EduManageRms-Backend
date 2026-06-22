package com.rms.repository;
import com.rms.entity.AssignmentStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
public interface AssignmentStudentRepository extends JpaRepository<AssignmentStudent, Long> {
    List<AssignmentStudent> findByAssignmentId(Long assignmentId);
    @Query("SELECT a FROM AssignmentStudent a WHERE a.assignment.id = :aId AND a.gradeSection.id = :gsId AND a.date = :date")
    List<AssignmentStudent> findByAssignmentIdAndGradeSectionIdAndDate(@Param("aId") Long aId, @Param("gsId") Long gsId, @Param("date") LocalDate date);
    @Query("SELECT COUNT(a), SUM(CASE WHEN a.status = true THEN 1 ELSE 0 END), SUM(CASE WHEN a.status = false THEN 1 ELSE 0 END) FROM AssignmentStudent a WHERE a.student.id = :studentId")
    Object[] getAssignmentSummary(@Param("studentId") Long studentId);
    @Transactional
    void deleteByAssignmentId(Long assignmentId);
}
