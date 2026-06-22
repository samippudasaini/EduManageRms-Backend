package com.rms.controller;

import com.rms.entity.Faculty;
import com.rms.repository.FacultyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/api/faculties")
public class FacultyController {
    @Autowired private FacultyRepository repo;
    @GetMapping public List<Faculty> getAll() { return repo.findAll(); }
    @GetMapping("/{id}") public ResponseEntity<?> getById(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(repo.save(Faculty.builder().name(body.get("name")).build()));
    }
    @PutMapping("/{id}") public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return repo.findById(id).map(f -> { f.setName(body.get("name")); return ResponseEntity.ok(repo.save(f)); })
                .orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}") public ResponseEntity<?> delete(@PathVariable Long id) {
        repo.deleteById(id); return ResponseEntity.ok().build();
    }
}
