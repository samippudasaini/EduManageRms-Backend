package com.rms.repository;

import com.rms.entity.Examination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExaminationRepository extends JpaRepository<Examination, Long> {
    List<Examination> findAllByOrderByIdDesc();

//    @Query("SELECT e FROM Examination e JOIN e.facultyDetails fd WHERE fd.id = :programId")
//    List<Examination> findAllByFacultyDetailId(@Param("facultyDetailId") Long facultyDetailId);


    @Query("SELECT e FROM Examination e JOIN e.programs p WHERE p.id = :programId")
    List<Examination> findAllByProgramId(@Param("programId") Long programId);
}
