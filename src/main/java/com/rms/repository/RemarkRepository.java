package com.rms.repository;
import com.rms.entity.Remark;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RemarkRepository extends JpaRepository<Remark, Long> {
    List<Remark> findByStudentIdOrderByIdDesc(Long studentId);
}
