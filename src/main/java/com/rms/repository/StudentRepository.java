package com.rms.repository;

import com.rms.entity.Student;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
//    List<Student> findByGradeSectionId(Long gradeSectionId);

//    @Query("SELECT s FROM Student s WHERE s.facultyDetail.id = :fdId")
//    List<Student> findByProgramId(@Param("pId") Long pId);

    @Query("SELECT s FROM Student s WHERE s.program.id = :fdId")
    List<Student> findByProgramId(@Param("fdId") Long fdId);

    @Query("SELECT s FROM Student s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%',:name,'%'))")
    List<Student> findByNameContaining(@Param("name") String name);


    @Modifying
    @Transactional
    @Query("UPDATE Student s SET s.program = null WHERE s.program.id = :fdId")
    void unenrollFromProgram(@Param("fdId") Long fdId);

    List<Student> findByGradeSectionId(Long gradeSectionId);

    // NEW — unenrolls students from a grade-section (doesn't delete them),
// used when force-deleting a grade-section that still has students in it.
    @Modifying
    @Transactional
    @Query("UPDATE Student s SET s.gradeSection = null WHERE s.gradeSection.id = :id")
    void unenrollFromGradeSection(@Param("id") Long id);

}
