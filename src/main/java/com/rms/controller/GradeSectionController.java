package com.rms.controller;
import com.rms.entity.*;
import com.rms.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/grade-sections")
public class GradeSectionController {
    @Autowired private GradeSectionMappingRepository repo;
    @Autowired private GradeRepository gradeRepo;
    @Autowired private SectionRepository sectionRepo;
    @Autowired private AssignmentRepository assignmentRepo;

    @GetMapping public List<Map<String,Object>> getAll() {
        return repo.findAll().stream().map(g -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id", g.getId());
            if (g.getGrade() != null) { m.put("gradeId", g.getGrade().getId()); m.put("gradeName", g.getGrade().getName()); }
            if (g.getSection() != null) { m.put("sectionId", g.getSection().getId()); m.put("sectionName", g.getSection().getName()); }
            return m;
        }).toList();
    }

    @PostMapping public ResponseEntity<?> create(@RequestBody Map<String,Object> body) {
        var grade = gradeRepo.findById(Long.parseLong(body.get("gradeId").toString())).orElse(null);
        Section section = null;
        if (body.get("sectionId") != null) section = sectionRepo.findById(Long.parseLong(body.get("sectionId").toString())).orElse(null);
        return ResponseEntity.ok(repo.save(GradeSectionMapping.builder().grade(grade).section(section).build()));
    }

    @PutMapping("/{id}") public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        return repo.findById(id).map(g -> {
            if (body.get("gradeId") != null) gradeRepo.findById(Long.parseLong(body.get("gradeId").toString())).ifPresent(g::setGrade);
            if (body.get("sectionId") != null) sectionRepo.findById(Long.parseLong(body.get("sectionId").toString())).ifPresent(g::setSection);
            return ResponseEntity.ok(repo.save(g));
        }).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        // Delete child assignments before deleting the grade-section
        assignmentRepo.deleteByGradeSectionId(id);
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }}
