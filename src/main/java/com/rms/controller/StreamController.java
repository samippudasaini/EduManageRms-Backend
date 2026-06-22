package com.rms.controller;

import com.rms.entity.Stream;
import com.rms.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/streams")
public class StreamController {

    @Autowired private StreamRepository repo;
    @Autowired private FacultyRepository facultyRepo;
    @Autowired private FacultyDetailRepository facultyDetailRepo;

    @GetMapping
    @Transactional
    public List<Map<String, Object>> getAll() {
        return repo.findAllWithFaculty().stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId()); m.put("name", s.getName());
            if (s.getFaculty() != null) {
                m.put("facultyId", s.getFaculty().getId());
                m.put("facultyName", s.getFaculty().getName());
            }
            return m;
        }).toList();
    }

    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return repo.findById(id).map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId()); m.put("name", s.getName());
            if (s.getFaculty() != null) {
                m.put("facultyId", s.getFaculty().getId());
                m.put("facultyName", s.getFaculty().getName());
            }
            return ResponseEntity.ok(m);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        var faculty = facultyRepo.findById(Long.parseLong(body.get("facultyId").toString())).orElse(null);
        Stream saved = repo.save(Stream.builder().name((String) body.get("name")).faculty(faculty).build());
        return ResponseEntity.ok(toMap(saved));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return repo.findById(id).map(s -> {
            s.setName((String) body.get("name"));
            if (body.get("facultyId") != null)
                facultyRepo.findById(Long.parseLong(body.get("facultyId").toString())).ifPresent(s::setFaculty);
            return ResponseEntity.ok(toMap(repo.save(s)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        // Null out stream FK on any programs using this stream before deleting
        facultyDetailRepo.findByStreamId(id).forEach(fd -> {
            fd.setStream(null);
            facultyDetailRepo.save(fd);
        });
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Stream deleted"));
    }

    private Map<String, Object> toMap(Stream s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId()); m.put("name", s.getName());
        if (s.getFaculty() != null) {
            m.put("facultyId", s.getFaculty().getId());
            m.put("facultyName", s.getFaculty().getName());
        }
        return m;
    }
}