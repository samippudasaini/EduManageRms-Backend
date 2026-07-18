package com.rms.repository;
import com.rms.entity.Marks;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MarksRepository extends JpaRepository<Marks, Long> {
    // NEW: delete marks rows by their IDs
    @Modifying
    @Transactional
    @Query("DELETE FROM Marks m WHERE m.id IN :ids")
    void deleteByIdIn(@Param("ids") List<Long> ids);
}
