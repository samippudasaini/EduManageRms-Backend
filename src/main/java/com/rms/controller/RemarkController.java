package com.rms.controller;
import com.rms.entity.Remark;
import com.rms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController @RequestMapping("/api/remarks")
public class RemarkController {
    @Autowired private RemarkRepository repo;
    @Autowired private StudentRepository studentRepo;

    private Map<String, Object> toMap(Remark r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("description", r.getDescription());
        m.put("date", r.getDate());
        return m;
    }

    @GetMapping("/student/{studentId}")
    public List<Map<String, Object>> getByStudent(@PathVariable Long studentId) {
        return repo.findByStudentIdOrderByIdDesc(studentId).stream().map(this::toMap).toList();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Object sid = body.get("studentId");
        String description = body.get("description") == null ? "" : body.get("description").toString().trim();

        if (sid == null)
            return ResponseEntity.badRequest().body(Map.of("message", "studentId is required"));
        if (description.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("message", "Remark cannot be empty"));

        var student = studentRepo.findById(Long.parseLong(sid.toString())).orElse(null);
        if (student == null)
            return ResponseEntity.status(404).body(Map.of("message", "Student not found"));

        Remark saved = repo.save(Remark.builder()
                .description(description)
                .student(student)
                .date(LocalDate.now())
                .build());

        // Return a plain map, NOT the entity. The entity links back into
        // Student -> Program -> ProgramSubject -> Program (a loop) and can't be serialized.
        return ResponseEntity.ok(toMap(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
