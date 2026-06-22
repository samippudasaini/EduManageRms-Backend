package com.rms.repository;
import com.rms.entity.FacultyDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
public interface FacultyDetailRepository extends JpaRepository<FacultyDetail, Long> {

    @Query("SELECT fd FROM FacultyDetail fd LEFT JOIN FETCH fd.stream s LEFT JOIN FETCH s.faculty")
    List<FacultyDetail> findAllWithStream();

    List<FacultyDetail> findByStreamId(Long streamId);
//    List<FacultyDetail> findByStreamId(Long streamId);
//    java.util.Optional<FacultyDetail> findByName(String name);
}
