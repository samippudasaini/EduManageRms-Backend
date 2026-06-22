package com.rms.controller;
import com.rms.entity.*;
import com.rms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/students")
public class StudentController {
    @Autowired private StudentRepository repo;
    @Autowired private FacultyDetailRepository fdRepo;
    @Autowired private GradeSectionMappingRepository gsRepo;

    @GetMapping public List<Map<String,Object>> getAll() { return repo.findAll().stream().map(this::toMap).toList(); }

    @GetMapping("/{id}") public ResponseEntity<?> getById(@PathVariable Long id) {
        return repo.findById(id).map(s -> ResponseEntity.ok(toMap(s))).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search") public List<Map<String,Object>> search(@RequestParam(required=false) String name, @RequestParam(required=false) Long id) {
        if (id != null) return repo.findById(id).map(s -> List.of(toMap(s))).orElse(List.of());
        if (name != null && !name.isBlank()) return repo.findByNameContaining(name).stream().map(this::toMap).toList();
        return repo.findAll().stream().map(this::toMap).toList();
    }

    @PostMapping public ResponseEntity<?> create(@RequestBody Map<String,Object> body) {
        var s = Student.builder().name((String) body.get("name")).address((String) body.get("address"))
                .guardianName((String) body.get("guardianName")).contact((String) body.get("contact")).build();
        if (body.get("facultyDetailsId") != null) fdRepo.findById(Long.parseLong(body.get("facultyDetailsId").toString())).ifPresent(s::setFacultyDetail);
        if (body.get("gradesId") != null) gsRepo.findById(Long.parseLong(body.get("gradesId").toString())).ifPresent(s::setGradeSection);
        return ResponseEntity.ok(toMap(repo.save(s)));
    }

    @PutMapping("/{id}") public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        return repo.findById(id).map(s -> {
            if (body.get("name") != null) s.setName((String) body.get("name"));
            if (body.get("address") != null) s.setAddress((String) body.get("address"));
            if (body.get("guardianName") != null) s.setGuardianName((String) body.get("guardianName"));
            if (body.get("contact") != null) s.setContact((String) body.get("contact"));
            if (body.get("facultyDetailsId") != null) fdRepo.findById(Long.parseLong(body.get("facultyDetailsId").toString())).ifPresent(s::setFacultyDetail);
            if (body.get("gradesId") != null) gsRepo.findById(Long.parseLong(body.get("gradesId").toString())).ifPresent(s::setGradeSection);
            return ResponseEntity.ok(toMap(repo.save(s)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}") public ResponseEntity<?> delete(@PathVariable Long id) {
        repo.deleteById(id); return ResponseEntity.ok().build();
    }

    private Map<String,Object> toMap(Student s) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("id", s.getId()); m.put("name", s.getName());
        m.put("address", s.getAddress()); m.put("guardianName", s.getGuardianName()); m.put("contact", s.getContact());
        if (s.getFacultyDetail() != null) { m.put("facultyDetailsId", s.getFacultyDetail().getId()); m.put("facultyName", s.getFacultyDetail().getName()); }
        if (s.getGradeSection() != null) {
            m.put("gradesId", s.getGradeSection().getId());
            if (s.getGradeSection().getGrade() != null) { m.put("gradeId", s.getGradeSection().getGrade().getId()); m.put("gradeName", s.getGradeSection().getGrade().getName()); }
            if (s.getGradeSection().getSection() != null) { m.put("sectionId", s.getGradeSection().getSection().getId()); m.put("sectionName", s.getGradeSection().getSection().getName()); }
        }
        return m;
    }
}
