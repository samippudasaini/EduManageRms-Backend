package com.rms.controller;

import com.rms.entity.*;
import com.rms.repository.*;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    @Autowired private AssignmentRepository repo;
    @Autowired private AssignmentStudentRepository asRepo;
    @Autowired private StudentRepository studentRepo;
    @Autowired private GradeSectionMappingRepository gsRepo;

    @Value("${app.upload.dir}")
    private String uploadDir;


    // ── Layer 1: Get all grade-sections that have assignments (dashboard cards) ──

    @GetMapping("/dashboard")
    @Transactional
    public List<Map<String, Object>> getDashboard() {
        List<GradeSectionMapping> sections = gsRepo.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (GradeSectionMapping gs : sections) {
            List<Assignment> assignments = repo.findByGradeSectionId(gs.getId());
            if (assignments.isEmpty()) continue;
            long pendingCount = 0;
            for (Assignment a : assignments) {
                List<AssignmentStudent> submissions = asRepo.findByAssignmentId(a.getId());
                pendingCount += submissions.stream()
                        .filter(s -> "PENDING".equals(s.getSubmissionStatus())).count();
            }
            Map<String, Object> card = new LinkedHashMap<>();
            card.put("gradeSectionId", gs.getId());
            card.put("gradeName", gs.getGrade() != null ? gs.getGrade().getName() : "");
            card.put("sectionName", gs.getSection() != null ? gs.getSection().getName() : "");
            card.put("totalAssignments", assignments.size());
            card.put("pendingCount", pendingCount);
            result.add(card);
        }
        return result;
    }

    // ── Layer 2: Get assignments for a grade-section ───────────────────────────

    @GetMapping("/grade/{gsId}")
    public List<Map<String, Object>> getByGrade(@PathVariable Long gsId) {
        return repo.findByGradeSectionId(gsId).stream().map(this::toMap).toList();
    }

    // ── Layer 3: Get student submissions for an assignment ────────────────────

    @GetMapping("/{aId}/submissions")
    @Transactional
    public ResponseEntity<?> getSubmissions(@PathVariable Long aId) {
        Assignment assignment = repo.findById(aId).orElse(null);
        if (assignment == null) return ResponseEntity.notFound().build();

        // Get or create submission rows for all students in this section
        Long gsId = assignment.getGradeSection().getId();
        List<Student> students = studentRepo.findByGradeSectionId(gsId);
        LocalDate date = assignment.getDate() != null
                ? LocalDate.parse(assignment.getDate()) : LocalDate.now();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Student student : students) {
            // Find existing submission or create one
            List<AssignmentStudent> existing = asRepo
                    .findByAssignmentIdAndGradeSectionIdAndDate(aId, gsId, date);
            AssignmentStudent as = existing.stream()
                    .filter(e -> e.getStudent() != null && e.getStudent().getId().equals(student.getId()))
                    .findFirst()
                    .orElseGet(() -> {
                        AssignmentStudent newAs = AssignmentStudent.builder()
                                .assignment(assignment)
                                .student(student)
                                .gradeSection(assignment.getGradeSection())
                                .date(date)
                                .submissionStatus("PENDING")
                                .status(false)
                                .build();
                        return asRepo.save(newAs);
                    });

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", as.getId());
            m.put("studentId", student.getId());
            m.put("studentName", student.getName());
            m.put("submissionStatus", as.getSubmissionStatus() != null ? as.getSubmissionStatus() : "PENDING");
            m.put("remarks", as.getRemarks());
            m.put("updatedAt", as.getUpdatedAt());
            result.add(m);
        }

        // Sort alphabetically by student name
        result.sort(Comparator.comparing(m -> m.get("studentName").toString()));
        return ResponseEntity.ok(result);
    }

    // ── Auto-save: Update single student status (PATCH, no global save button) ─

    @PatchMapping("/submissions/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestBody Map<String, Object> body) {
        return asRepo.findById(id).map(as -> {
            String newStatus = (String) body.get("status");
            String remarks   = (String) body.get("remarks");

            as.setSubmissionStatus(newStatus != null ? newStatus : "PENDING");
            as.setStatus("DONE".equals(newStatus)); // keep backward compat
            if (remarks != null) as.setRemarks(remarks.isBlank() ? null : remarks);
            else if ("DONE".equals(newStatus)) as.setRemarks(null); // clear remarks on DONE
            as.setUpdatedAt(LocalDateTime.now());
            asRepo.save(as);

            return ResponseEntity.ok(Map.of(
                    "id", as.getId(),
                    "submissionStatus", as.getSubmissionStatus(),
                    "remarks", as.getRemarks() != null ? as.getRemarks() : ""
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Mark ALL students as DONE for an assignment ───────────────────────────

    @PatchMapping("/{aId}/mark-all-done")
    @Transactional
    public ResponseEntity<?> markAllDone(@PathVariable Long aId) {
        List<AssignmentStudent> all = asRepo.findByAssignmentId(aId);
        for (AssignmentStudent as : all) {
            as.setSubmissionStatus("DONE");
            as.setStatus(true);
            as.setRemarks(null);
            as.setUpdatedAt(LocalDateTime.now());
            asRepo.save(as);
        }
        return ResponseEntity.ok(Map.of("message", "All marked as DONE", "count", all.size()));
    }

    // ── Create assignment with optional file upload ───────────────────────────

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    @Transactional
    public ResponseEntity<?> create(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("date") String date,
            @RequestParam(value = "dueDate", required = false) String dueDate,
            @RequestParam("gradesId") Long gsId,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        GradeSectionMapping gs = gsRepo.findById(gsId)
                .orElseThrow(() -> new RuntimeException("Grade section not found"));

        Assignment.AssignmentBuilder builder = Assignment.builder()
                .name(name).description(description).date(date)
                .dueDate(dueDate).gradeSection(gs).status("ACTIVE");

        // Handle file upload
        if (file != null && !file.isEmpty()) {
            String savedPath = saveFile(file, "assignments");
            if (savedPath == null)
                return ResponseEntity.badRequest().body(Map.of("message", "File upload failed"));
            builder.fileName(file.getOriginalFilename())
                    .filePath(savedPath)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize());
        }

        Assignment saved = repo.save(builder.build());

        // Create submission rows for all students in this grade-section
        List<Student> students = studentRepo.findByGradeSectionId(gsId);
        LocalDate localDate = LocalDate.parse(date);
        for (Student s : students) {
            asRepo.save(AssignmentStudent.builder()
                    .assignment(saved).student(s).gradeSection(gs)
                    .date(localDate).submissionStatus("PENDING").status(false).build());
        }

        return ResponseEntity.ok(toMap(saved));
    }

    // ── Download / view assignment file ──────────────────────────────────────

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws IOException {
        Assignment a = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        if (a.getFilePath() == null)
            return ResponseEntity.notFound().build();

        Path path = Paths.get(a.getFilePath());
        org.springframework.core.io.Resource resource = new UrlResource(path.toUri());
        if (!resource.exists())
            return ResponseEntity.notFound().build();

        String contentType = a.getFileType() != null
                ? a.getFileType() : "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + a.getFileName() + "\"")
                .body((Resource) resource);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        repo.findById(id).ifPresent(a -> {
            // Delete uploaded file from disk
            if (a.getFilePath() != null) {
                try { Files.deleteIfExists(Paths.get(a.getFilePath())); }
                catch (IOException ignored) {}
            }
        });
        asRepo.deleteByAssignmentId(id);
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ── Helper: save uploaded file to disk ───────────────────────────────────

    private String saveFile(MultipartFile file, String subDir) {
        try {
            // Validate file type — only PDF, images, text allowed
            String contentType = file.getContentType();
            if (contentType == null) return null;
            boolean allowed = contentType.startsWith("image/")
                    || contentType.equals("application/pdf")
                    || contentType.startsWith("text/");
            if (!allowed) return null;

            Path dir = Paths.get(uploadDir, subDir);
            Files.createDirectories(dir);

            // Use UUID to avoid filename conflicts
            String ext = "";
            String orig = file.getOriginalFilename();
            if (orig != null && orig.contains("."))
                ext = orig.substring(orig.lastIndexOf("."));
            String stored = UUID.randomUUID() + ext;

            Path dest = dir.resolve(stored);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            return dest.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private Map<String, Object> toMap(Assignment a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("name", a.getName());
        m.put("description", a.getDescription());
        m.put("date", a.getDate());
        m.put("dueDate", a.getDueDate());
        m.put("status", a.getStatus());
        m.put("hasFile", a.getFilePath() != null);
        m.put("fileName", a.getFileName());
        m.put("fileType", a.getFileType());
        m.put("fileSize", a.getFileSize());
        if (a.getGradeSection() != null) {
            m.put("gradesId", a.getGradeSection().getId());
            if (a.getGradeSection().getGrade() != null)
                m.put("gradeName", a.getGradeSection().getGrade().getName());
            if (a.getGradeSection().getSection() != null)
                m.put("sectionName", a.getGradeSection().getSection().getName());
        }
        return m;
    }

}
