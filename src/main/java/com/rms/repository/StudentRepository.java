package com.rms.repository;

import com.rms.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByGradeSectionId(Long gradeSectionId);
    List<Student> findByFacultyDetailId(Long facultyDetailId);
    @Query("SELECT s FROM Student s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%',:name,'%'))")
    List<Student> findByNameContaining(@Param("name") String name);
}
