package com.rms.repository;
import com.rms.entity.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
public interface StreamRepository extends JpaRepository<Stream, Long> {
    @Query("SELECT s FROM Stream s LEFT JOIN FETCH s.faculty")
    List<Stream> findAllWithFaculty();
//    List<Stream> findByFacultyId(Long facultyId);
}
