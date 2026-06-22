package com.rms.controller;

import com.rms.entity.*;
import com.rms.repository.*;
import com.rms.service.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/examinations")
public class ExaminationController {

    @Autowired private ExaminationRepository repo;
    @Autowired private FacultyDetailRepository fdRepo;
    @Autowired private ResultService resultService;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return repo.findAllByOrderByIdDesc().stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId()); m.put("name", e.getName()); m.put("year", e.getYear());
            return m;
        }).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return repo.findById(id).map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", e.getId()); m.put("name", e.getName()); m.put("year", e.getYear());
            List<Map<String, Object>> fds = e.getFacultyDetails().stream().map(fd -> {
                Map<String, Object> fm = new LinkedHashMap<>();
                fm.put("id", fd.getId()); fm.put("name", fd.getName());
                return fm;
            }).toList();
            m.put("facultyDetails", fds);
            return ResponseEntity.ok(m);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Examination e = Examination.builder()
                .name((String) body.get("name"))
                .year((String) body.get("year"))
                .build();
        Examination saved = repo.save(e);
        return ResponseEntity.ok(Map.of("id", saved.getId(), "name", saved.getName(), "year", saved.getYear()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return repo.findById(id).map(e -> {
            if (body.get("name") != null) e.setName((String) body.get("name"));
            if (body.get("year") != null) e.setYear((String) body.get("year"));
            Examination saved = repo.save(e);
            return ResponseEntity.ok(Map.of("id", saved.getId(), "name", saved.getName(), "year", saved.getYear()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    @PostMapping("/{examId}/faculty-details")
    public ResponseEntity<?> addFacultyDetail(@PathVariable Long examId, @RequestBody Map<String, Object> body) {
        Examination exam = repo.findById(examId).orElseThrow(() -> new RuntimeException("Exam not found"));
        FacultyDetail fd = fdRepo.findById(Long.parseLong(body.get("facultyDetailId").toString()))
                .orElseThrow(() -> new RuntimeException("FacultyDetail not found"));
        boolean alreadyExists = exam.getFacultyDetails().stream().anyMatch(f -> f.getId().equals(fd.getId()));
        if (!alreadyExists) {
            exam.getFacultyDetails().add(fd);
            repo.save(exam);
            resultService.createResultsForExamination(exam, fd);
        }
        return ResponseEntity.ok(Map.of("message", "Program assigned successfully"));
    }

    @DeleteMapping("/{examId}/faculty-details/{fdId}")
    public ResponseEntity<?> removeFacultyDetail(@PathVariable Long examId, @PathVariable Long fdId) {
        Examination exam = repo.findById(examId).orElseThrow();
        exam.getFacultyDetails().removeIf(fd -> fd.getId().equals(fdId));
        repo.save(exam);
        return ResponseEntity.ok(Map.of("message", "Removed"));
    }

    @GetMapping("/{examId}/results/{fdId}")
    public ResponseEntity<?> viewResults(@PathVariable Long examId, @PathVariable Long fdId) {
        return ResponseEntity.ok(resultService.getResultsWithMarks(examId, fdId));
    }

    @GetMapping("/{examId}/add-results/{fdId}")
    public ResponseEntity<?> addResults(@PathVariable Long examId, @PathVariable Long fdId) {
        return ResponseEntity.ok(resultService.getResultsWithMarks(examId, fdId));
    }
}
