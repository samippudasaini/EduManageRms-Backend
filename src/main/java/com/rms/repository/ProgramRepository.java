package com.rms.repository;
import com.rms.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
public interface ProgramRepository extends JpaRepository<Program, Long> {

//    @Query("SELECT fd FROM FacultyDetail fd LEFT JOIN FETCH fd.stream s LEFT JOIN FETCH s.faculty")
//    List<Program> findAllWithStream();

    @Query("SELECT p FROM Program p LEFT JOIN FETCH p.stream s LEFT JOIN FETCH s.faculty")
    List<Program> findAllWithStream();

    List<Program> findByStreamId(Long streamId);
    @Modifying
    @Query(value = "DELETE FROM examinations_faculty_details WHERE faculty_detail_id = :id", nativeQuery = true)
    void detachFromExaminations(@Param("id") Long id);

}
