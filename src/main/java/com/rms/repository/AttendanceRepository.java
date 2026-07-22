package com.rms.repository;
import com.rms.entity.Attendance;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {


    @Query("SELECT a FROM Attendance a WHERE a.student.gradeSection.id = :gsId AND a.date = :date")
    List<Attendance> findByGradeSectionIdAndDate(@Param("gsId") Long gsId, @Param("date") LocalDate date);

    Optional<Attendance> findByStudentIdAndDate(Long studentId, LocalDate date);

    @Query("SELECT a FROM Attendance a WHERE a.student.gradeSection.id = :gsId " +
            "AND a.date >= :start AND a.date <= :end ORDER BY a.student.name, a.date")
    List<Attendance> findByGradeSectionAndRange(
            @Param("gsId") Long gsId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT COUNT(a), " +
            "SUM(CASE WHEN a.attendanceStatus = 'P' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN a.attendanceStatus = 'A' THEN 1 ELSE 0 END) " +
            "FROM Attendance a WHERE a.student.id = :studentId")
    Object[] getAttendanceSummary(@Param("studentId") Long studentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Attendance a WHERE a.student.id = :studentId")
    void deleteByStudentId(@Param("studentId") Long studentId);
}
