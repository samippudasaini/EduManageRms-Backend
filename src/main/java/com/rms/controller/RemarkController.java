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

    @GetMapping("/student/{studentId}") public List<Map<String,Object>> getByStudent(@PathVariable Long studentId) {
        return repo.findByStudentIdOrderByIdDesc(studentId).stream().map(r -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id", r.getId()); m.put("description", r.getDescription()); m.put("date", r.getDate());
            return m;
        }).toList();
    }

    @PostMapping public ResponseEntity<?> create(@RequestBody Map<String,Object> body) {
        var student = studentRepo.findById(Long.parseLong(body.get("studentId").toString())).orElseThrow();
        var r = Remark.builder().description((String) body.get("description")).student(student).date(LocalDate.now()).build();
        return ResponseEntity.ok(repo.save(r));
    }
}
