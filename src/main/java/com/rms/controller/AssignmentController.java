package com.rms.controller;

import com.rms.entity.*;
import com.rms.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    @Autowired private AssignmentRepository repo;
    @Autowired private AssignmentStudentRepository asRepo;
    @Autowired private StudentRepository studentRepo;
    @Autowired private GradeSectionMappingRepository gsRepo;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return repo.findAll().stream().map(this::toMap).toList();
    }

    @GetMapping("/grade/{gsId}")
    public List<Map<String, Object>> getByGrade(@PathVariable Long gsId) {
        return repo.findByGradeSectionId(gsId).stream().map(this::toMap).toList();
    }

    @GetMapping("/{aId}/grade/{gsId}/date/{date}")
    public ResponseEntity<?> getClassAssignment(
            @PathVariable Long aId, @PathVariable Long gsId, @PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        List<AssignmentStudent> list = asRepo.findByAssignmentIdAndGradeSectionIdAndDate(aId, gsId, localDate);
        List<Map<String, Object>> result = list.stream().map(as -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", as.getId());
            m.put("status", as.getStatus() != null ? as.getStatus() : false);
            if (as.getStudent() != null) {
                m.put("studentId", as.getStudent().getId());
                m.put("studentName", as.getStudent().getName());
            }
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Long gsId = Long.parseLong(body.get("gradesId").toString());
        GradeSectionMapping gs = gsRepo.findById(gsId)
                .orElseThrow(() -> new RuntimeException("Grade section not found"));
        String dateStr = (String) body.get("date");
        Assignment a = repo.save(Assignment.builder()
                .name((String) body.get("name"))
                .date(dateStr)
                .gradeSection(gs)
                .build());
        List<Student> students = studentRepo.findByGradeSectionId(gs.getId());
        LocalDate date = LocalDate.parse(dateStr);
        for (Student s : students) {
            asRepo.save(AssignmentStudent.builder()
                    .assignment(a).student(s).status(false).gradeSection(gs).date(date).build());
        }
        return ResponseEntity.ok(toMap(a));
    }

    @PostMapping("/process/{aId}/{gsId}/{date}")
    public ResponseEntity<?> processAssignment(
            @PathVariable Long aId, @PathVariable Long gsId, @PathVariable String date,
            @RequestBody List<Map<String, Object>> list) {
        for (Map<String, Object> item : list) {
            Long asId = Long.parseLong(item.get("id").toString());
            boolean status = Boolean.parseBoolean(item.get("status").toString());
            asRepo.findById(asId).ifPresent(as -> { as.setStatus(status); asRepo.save(as); });
        }
        return ResponseEntity.ok(Map.of("message", "Saved successfully"));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        asRepo.deleteByAssignmentId(id);
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    private Map<String, Object> toMap(Assignment a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("name", a.getName());
        m.put("date", a.getDate());
        if (a.getGradeSection() != null) {
            m.put("gradesId", a.getGradeSection().getId());
            if (a.getGradeSection().getGrade() != null) m.put("gradeName", a.getGradeSection().getGrade().getName());
            if (a.getGradeSection().getSection() != null) m.put("sectionName", a.getGradeSection().getSection().getName());
        }
        return m;
    }
}
