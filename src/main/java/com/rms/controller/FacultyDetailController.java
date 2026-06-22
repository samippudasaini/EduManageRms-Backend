package com.rms.controller;

import com.rms.entity.*;
import com.rms.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/faculty-details")
public class FacultyDetailController {

    @Autowired private FacultyDetailRepository repo;
    @Autowired private StreamRepository streamRepo;
    @Autowired private SubjectRepository subjectRepo;
    @Autowired private FacultyDetailSubjectRepository fdsRepo;

    @GetMapping
    @Transactional
    public List<Map<String, Object>> getAll() {
        return repo.findAllWithStream().stream().map(this::toMap).toList();
    }

    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return repo.findById(id)
                .map(fd -> ResponseEntity.ok(toMap(fd)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        FacultyDetail fd = FacultyDetail.builder().name((String) body.get("name")).build();
        if (body.get("streamId") != null)
            streamRepo.findById(Long.parseLong(body.get("streamId").toString())).ifPresent(fd::setStream);
        FacultyDetail saved = repo.save(fd);
        applySubjectIds(saved, body.get("subjectIds"));
        return ResponseEntity.ok(toMap(repo.findById(saved.getId()).orElse(saved)));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return repo.findById(id).map(fd -> {
            if (body.get("name") != null) fd.setName((String) body.get("name"));
            if (body.get("streamId") != null)
                streamRepo.findById(Long.parseLong(body.get("streamId").toString())).ifPresent(fd::setStream);
            FacultyDetail saved = repo.save(fd);
            if (body.get("subjectIds") != null) applySubjectIds(saved, body.get("subjectIds"));
            return ResponseEntity.ok(toMap(repo.findById(saved.getId()).orElse(saved)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    @PostMapping("/{id}/subjects")
    @Transactional
    public ResponseEntity<?> addSubject(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return repo.findById(id).map(fd -> {
            Long subjectId = Long.parseLong(body.get("subjectId").toString());
            boolean isOptional = Boolean.TRUE.equals(body.get("optional"))
                    || "true".equalsIgnoreCase(String.valueOf(body.get("optional")));
            Subject subject = subjectRepo.findById(subjectId).orElse(null);
            if (subject == null) return ResponseEntity.badRequest().body(Map.of("message", "Subject not found"));
            var existing = fdsRepo.findByFacultyDetailIdAndSubjectId(id, subjectId);
            if (existing.isPresent()) {
                existing.get().setOptional(isOptional);
                fdsRepo.save(existing.get());
            } else {
                fdsRepo.save(FacultyDetailSubject.builder().facultyDetail(fd).subject(subject).optional(isOptional).build());
            }
            return ResponseEntity.ok(toMap(repo.findById(id).orElse(fd)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/subjects/{subjectId}")
    @Transactional
    public ResponseEntity<?> updateSubjectType(@PathVariable Long id, @PathVariable Long subjectId,
                                               @RequestBody Map<String, Object> body) {
        return fdsRepo.findByFacultyDetailIdAndSubjectId(id, subjectId).map(fds -> {
            fds.setOptional(Boolean.TRUE.equals(body.get("optional"))
                    || "true".equalsIgnoreCase(String.valueOf(body.get("optional"))));
            fdsRepo.save(fds);
            return repo.findById(id).map(fd -> ResponseEntity.ok(toMap(fd)))
                    .orElse(ResponseEntity.notFound().build());
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{fdId}/subjects/{subjectId}")
    @Transactional
    public ResponseEntity<?> removeSubject(@PathVariable Long fdId, @PathVariable Long subjectId) {
        return repo.findById(fdId).map(fd -> {
            fdsRepo.deleteByFacultyDetailIdAndSubjectId(fdId, subjectId);
            return ResponseEntity.ok(toMap(repo.findById(fdId).orElse(fd)));
        }).orElse(ResponseEntity.notFound().build());
    }

    private void applySubjectIds(FacultyDetail fd, Object subjectIdsObj) {
        if (subjectIdsObj == null) return;
        List<Long> ids = ((List<?>) subjectIdsObj).stream().map(o -> Long.parseLong(o.toString())).toList();
        List<FacultyDetailSubject> current = fdsRepo.findByFacultyDetailId(fd.getId());
        for (FacultyDetailSubject fds : current)
            if (!ids.contains(fds.getSubject().getId())) fdsRepo.delete(fds);
        for (Long subjectId : ids)
            if (current.stream().noneMatch(fds -> fds.getSubject().getId().equals(subjectId)))
                subjectRepo.findById(subjectId).ifPresent(s -> fdsRepo.save(
                        FacultyDetailSubject.builder().facultyDetail(fd).subject(s).optional(false).build()));
    }

    private Map<String, Object> toMap(FacultyDetail fd) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", fd.getId());
        m.put("name", fd.getName());
        if (fd.getStream() != null) {
            m.put("streamId", fd.getStream().getId());
            m.put("streamName", fd.getStream().getName());
            if (fd.getStream().getFaculty() != null) {
                m.put("facultyId", fd.getStream().getFaculty().getId());
                m.put("facultyName", fd.getStream().getFaculty().getName());
            }
        }
        List<Map<String, Object>> subjects = fd.getSubjectMappings().stream().map(fds -> {
            Subject s = fds.getSubject();
            Map<String, Object> sm = new LinkedHashMap<>();
            sm.put("id", s.getId()); sm.put("name", s.getName());
            sm.put("fullMarks", s.getFullMarks()); sm.put("passMarks", s.getPassMarks());
            sm.put("theory", s.getTheory()); sm.put("practical", s.getPractical());
            sm.put("optional", fds.getOptional());
            sm.put("type", Boolean.TRUE.equals(fds.getOptional()) ? "Optional" : "Compulsory");
            return sm;
        }).toList();
        m.put("subjects", subjects);
        return m;
    }
}