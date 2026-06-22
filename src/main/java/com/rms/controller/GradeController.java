package com.rms.controller;
import com.rms.entity.*;
import com.rms.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/grades")
public class GradeController {
    @Autowired private GradeRepository repo;
    @Autowired private AssignmentRepository assignmentRepo;
    @Autowired private GradeSectionMappingRepository gradeSectionRepo;

    @GetMapping public List<Grade> getAll() { return repo.findAll(); }
    @PostMapping public ResponseEntity<?> create(@RequestBody Map<String,String> body) {
        return ResponseEntity.ok(repo.save(Grade.builder().name(body.get("name")).build()));
    }
    @PutMapping("/{id}") public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String,String> body) {
        return repo.findById(id).map(g -> { g.setName(body.get("name")); return ResponseEntity.ok(repo.save(g)); }).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        // First remove assignments linked to sections of this grade
        List<GradeSectionMapping> sections = gradeSectionRepo.findByGradeId(id);
        for (GradeSectionMapping section : sections) {
            assignmentRepo.deleteByGradeSectionId(section.getId());
        }
        // Then remove the sections themselves
        gradeSectionRepo.deleteByGradeId(id);
        // Now safe to delete the grade
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
