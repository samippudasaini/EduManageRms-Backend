package com.rms.controller;
import com.rms.entity.*;
import com.rms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/sections")
public class SectionController {
    @Autowired private SectionRepository repo;
    @GetMapping public List<Section> getAll() { return repo.findAll(); }
    @PostMapping public ResponseEntity<?> create(@RequestBody Map<String,String> body) {
        return ResponseEntity.ok(repo.save(Section.builder().name(body.get("name")).build()));
    }
    @PutMapping("/{id}") public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String,String> body) {
        return repo.findById(id).map(s -> { s.setName(body.get("name")); return ResponseEntity.ok(repo.save(s)); }).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}") public ResponseEntity<?> delete(@PathVariable Long id) { repo.deleteById(id); return ResponseEntity.ok().build(); }
}
