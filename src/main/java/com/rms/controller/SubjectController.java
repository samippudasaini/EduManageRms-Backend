package com.rms.controller;

import com.rms.entity.Subject;
import com.rms.repository.MarksRepository;
import com.rms.repository.SubjectRepository;
import com.rms.service.SubjectDeletionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/subjects")
public class SubjectController {
    @Autowired private SubjectRepository repo;
    @Autowired private SubjectDeletionService subjectDeletionService;

    @GetMapping public List<Subject> getAll() { return repo.findAll(); }
    @GetMapping("/{id}") public ResponseEntity<?> getById(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        var s = Subject.builder()
                .name((String) body.get("name"))
                .theory(Float.parseFloat(body.get("theory").toString()))
                .practical(Float.parseFloat(body.get("practical").toString()))
                .passMarks(Float.parseFloat(body.get("passMarks").toString()))
                .fullMarks(Float.parseFloat(body.get("fullMarks").toString()))
                .creditHour(Integer.parseInt(body.get("creditHour").toString()))
                .build();
        return ResponseEntity.ok(repo.save(s));
    }
    @PutMapping("/{id}") public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return repo.findById(id).map(s -> {
            s.setName((String) body.get("name"));
            s.setTheory(Float.parseFloat(body.get("theory").toString()));
            s.setPractical(Float.parseFloat(body.get("practical").toString()));
            s.setPassMarks(Float.parseFloat(body.get("passMarks").toString()));
            s.setFullMarks(Float.parseFloat(body.get("fullMarks").toString()));
            s.setCreditHour(Integer.parseInt(body.get("creditHour").toString()));
            return ResponseEntity.ok(repo.save(s));
        }).orElse(ResponseEntity.notFound().build());
    }
//    @DeleteMapping("/{id}") public ResponseEntity<?> delete(@PathVariable Long id) {
//        repo.deleteById(id); return ResponseEntity.ok().build();
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @RequestParam(name = "force", defaultValue = "false") boolean force) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            subjectDeletionService.deleteAndFlush(id, force);
        } catch (com.rms.service.SubjectDeletionService.BlockedByMarksException e) {
            return ResponseEntity.status(409).body(Map.of(
                    "message", "This subject has " + e.marksCount +
                            " recorded mark(s). Delete anyway? Those marks will be permanently deleted.",
                    "requiresForce", true,
                    "marksCount", e.marksCount
            ));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(409).body(Map.of(
                    "message", "Cannot delete this subject because it is still referenced elsewhere."
            ));
        }
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
