package com.rms.repository;
import com.rms.entity.Remark;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
public interface RemarkRepository extends JpaRepository<Remark, Long> {
//    List<Remark> findByStudentIdOrderByIdDesc(Long studentId);

    List<Remark> findByStudentIdOrderByIdDesc(Long studentId);

    // NEW: delete all remarks for a student
    @Modifying
    @Transactional
    @Query("DELETE FROM Remark r WHERE r.student.id = :studentId")
    void deleteByStudentId(@Param("studentId") Long studentId);
}
