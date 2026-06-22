package com.rms.repository;

import com.rms.entity.Examination;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExaminationRepository extends JpaRepository<Examination, Long> {
    List<Examination> findAllByOrderByIdDesc();
}
