package com.rms.repository;
import com.rms.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    @Query("SELECT a FROM Attendance a WHERE a.student.gradeSection.id = :gsId AND a.date = :date")
    List<Attendance> findByGradeSectionIdAndDate(@Param("gsId") Long gsId, @Param("date") LocalDate date);
    Optional<Attendance> findByStudentIdAndDate(Long studentId, LocalDate date);
    @Query("SELECT COUNT(a), SUM(CASE WHEN a.status = true THEN 1 ELSE 0 END), SUM(CASE WHEN a.status = false THEN 1 ELSE 0 END) FROM Attendance a WHERE a.student.id = :studentId")
    Object[] getAttendanceSummary(@Param("studentId") Long studentId);
}
