package com.rms.controller;

import com.rms.entity.*;
import com.rms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired private AttendanceRepository repo;
    @Autowired private StudentRepository studentRepo;
    @Autowired private GradeSectionMappingRepository gsRepo;

    @GetMapping("/conduct/{gradeSectionId}")
    public ResponseEntity<?> conductAttendance(@PathVariable Long gradeSectionId) {
        LocalDate today = LocalDate.now();
        List<Attendance> existing = repo.findByGradeSectionIdAndDate(gradeSectionId, today);
        if (existing.isEmpty()) {
            List<Student> students = studentRepo.findByGradeSectionId(gradeSectionId);
            for (Student s : students) {
                repo.save(Attendance.builder().student(s).date(today).status(true).build());
            }
            existing = repo.findByGradeSectionIdAndDate(gradeSectionId, today);
        }
        return ResponseEntity.ok(toMapList(existing));
    }

    @GetMapping("/view/{gradeSectionId}")
    public ResponseEntity<?> viewAttendance(
            @PathVariable Long gradeSectionId,
            @RequestParam(required = false) String date) {
        LocalDate d = (date != null && !date.isBlank()) ? LocalDate.parse(date) : LocalDate.now();
        return ResponseEntity.ok(toMapList(repo.findByGradeSectionIdAndDate(gradeSectionId, d)));
    }

    @PostMapping("/process/{gradeSectionId}")
    public ResponseEntity<?> processAttendance(
            @PathVariable Long gradeSectionId,
            @RequestBody List<Map<String, Object>> attendanceList) {
        for (Map<String, Object> item : attendanceList) {
            Long attId = Long.parseLong(item.get("id").toString());
            boolean status = Boolean.parseBoolean(item.get("status").toString());
            repo.findById(attId).ifPresent(a -> { a.setStatus(status); repo.save(a); });
        }
        return ResponseEntity.ok(Map.of("message", "Attendance saved successfully"));
    }

    private List<Map<String, Object>> toMapList(List<Attendance> list) {
        return list.stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("date", a.getDate() != null ? a.getDate().toString() : "");
            m.put("status", a.getStatus() != null ? a.getStatus() : true);
            if (a.getStudent() != null) {
                m.put("studentId", a.getStudent().getId());
                m.put("studentName", a.getStudent().getName());
            }
            return m;
        }).toList();
    }
}
