////package com.rms.controller;
////import com.rms.entity.*;
////import com.rms.repository.*;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.*;
////import java.util.*;
////
////@RestController @RequestMapping("/api/students")
////public class StudentController {
////    @Autowired private StudentRepository repo;
////    @Autowired private FacultyDetailRepository fdRepo;
////    @Autowired private GradeSectionMappingRepository gsRepo;
////
////    @GetMapping public List<Map<String,Object>> getAll() { return repo.findAll().stream().map(this::toMap).toList(); }
////
////    @GetMapping("/{id}") public ResponseEntity<?> getById(@PathVariable Long id) {
////        return repo.findById(id).map(s -> ResponseEntity.ok(toMap(s))).orElse(ResponseEntity.notFound().build());
////    }
////
////    @GetMapping("/search") public List<Map<String,Object>> search(@RequestParam(required=false) String name, @RequestParam(required=false) Long id) {
////        if (id != null) return repo.findById(id).map(s -> List.of(toMap(s))).orElse(List.of());
////        if (name != null && !name.isBlank()) return repo.findByNameContaining(name).stream().map(this::toMap).toList();
////        return repo.findAll().stream().map(this::toMap).toList();
////    }
////
////    @PostMapping public ResponseEntity<?> create(@RequestBody Map<String,Object> body) {
////        var s = Student.builder().name((String) body.get("name")).address((String) body.get("address"))
////                .guardianName((String) body.get("guardianName")).contact((String) body.get("contact")).build();
////        if (body.get("facultyDetailsId") != null) fdRepo.findById(Long.parseLong(body.get("facultyDetailsId")
////                .toString())).ifPresent(s::setFacultyDetail);
////        if (body.get("gradesId") != null) gsRepo.findById(Long.parseLong(body.get("gradesId")
////                .toString())).ifPresent(s::setGradeSection);
////        return ResponseEntity.ok(toMap(repo.save(s)));
////    }
////
////    @PutMapping("/{id}") public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String,Object> body) {
////        return repo.findById(id).map(s -> {
////            if (body.get("name") != null) s.setName((String) body.get("name"));
////            if (body.get("address") != null) s.setAddress((String) body.get("address"));
////            if (body.get("guardianName") != null) s.setGuardianName((String) body.get("guardianName"));
////            if (body.get("contact") != null) s.setContact((String) body.get("contact"));
////            if (body.get("facultyDetailsId") != null) fdRepo.findById(Long.parseLong(body.get("facultyDetailsId").toString())).ifPresent(s::setFacultyDetail);
////            if (body.get("gradesId") != null) gsRepo.findById(Long.parseLong(body.get("gradesId").toString())).ifPresent(s::setGradeSection);
////            return ResponseEntity.ok(toMap(repo.save(s)));
////        }).orElse(ResponseEntity.notFound().build());
////    }
////
////    @DeleteMapping("/{id}") public ResponseEntity<?> delete(@PathVariable Long id) {
////        repo.deleteById(id); return ResponseEntity.ok().build();
////    }
////
////    private Map<String,Object> toMap(Student s) {
////        Map<String,Object> m = new LinkedHashMap<>();
////        m.put("id", s.getId()); m.put("name", s.getName());
////        m.put("address", s.getAddress()); m.put("guardianName", s.getGuardianName()); m.put("contact", s.getContact());
////        if (s.getFacultyDetail() != null) { m.put("facultyDetailsId", s.getFacultyDetail().getId()); m.put("facultyName", s.getFacultyDetail().getName()); }
////        if (s.getGradeSection() != null) {
////            m.put("gradesId", s.getGradeSection().getId());
////            if (s.getGradeSection().getGrade() != null) { m.put("gradeId", s.getGradeSection().getGrade().getId()); m.put("gradeName", s.getGradeSection().getGrade().getName()); }
////            if (s.getGradeSection().getSection() != null) { m.put("sectionId", s.getGradeSection().getSection().getId()); m.put("sectionName", s.getGradeSection().getSection().getName()); }
////        }
////        return m;
////    }
////}
//
//package com.rms.controller;
//
//import com.rms.entity.*;
//import com.rms.repository.*;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.transaction.Transactional;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.InputStream;
//import java.util.*;
//
//@RestController
//@RequestMapping("/api/students")
//public class StudentController {
//
//    @Autowired private StudentRepository repo;
//    @Autowired private FacultyDetailRepository fdRepo;
//    @Autowired private GradeSectionMappingRepository gsRepo;
//
//    @GetMapping
//    public List<Map<String, Object>> getAll() {
//        return repo.findAll().stream().map(this::toMap).toList();
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<?> getById(@PathVariable Long id) {
//        return repo.findById(id).map(s -> ResponseEntity.ok(toMap(s))).orElse(ResponseEntity.notFound().build());
//    }
//
//    @GetMapping("/search")
//    public List<Map<String, Object>> search(@RequestParam(required = false) String name,
//                                            @RequestParam(required = false) Long id) {
//        if (id != null) return repo.findById(id).map(s -> List.of(toMap(s))).orElse(List.of());
//        if (name != null && !name.isBlank()) return repo.findByNameContaining(name).stream().map(this::toMap).toList();
//        return repo.findAll().stream().map(this::toMap).toList();
//    }
//
//    @GetMapping("/grouped")
//    @Transactional
//    public List<Map<String, Object>> getGrouped() {
//        List<Student> all = repo.findAll();
//
//        // Group by program (facultyDetail)
//        Map<Long, Map<String, Object>> programMap = new LinkedHashMap<>();
//        for (Student s : all) {
//            Long programId = s.getFacultyDetail() != null ? s.getFacultyDetail().getId() : 0L;
//            String programName = s.getFacultyDetail() != null ? s.getFacultyDetail().getName() : "Unassigned";
//
//            programMap.putIfAbsent(programId, new LinkedHashMap<>(Map.of(
//                    "programId", programId,
//                    "programName", programName,
//                    "grades", new LinkedHashMap<Long, Map<String, Object>>(),
//                    "totalCount", 0
//            )));
//
//            Map<String, Object> progGroup = programMap.get(programId);
//
//            // Group by grade inside the program
//            Long gradeId = (s.getGradeSection() != null && s.getGradeSection().getGrade() != null)
//                    ? s.getGradeSection().getGrade().getId() : 0L;
//            String gradeName = (s.getGradeSection() != null && s.getGradeSection().getGrade() != null)
//                    ? s.getGradeSection().getGrade().getName() : "Unassigned";
//            String sectionName = (s.getGradeSection() != null && s.getGradeSection().getSection() != null)
//                    ? s.getGradeSection().getSection().getName() : "";
//
//            @SuppressWarnings("unchecked")
//            Map<Long, Map<String, Object>> grades =
//                    (Map<Long, Map<String, Object>>) progGroup.get("grades");
//
//            grades.putIfAbsent(gradeId, new LinkedHashMap<>(Map.of(
//                    "gradeId", gradeId,
//                    "gradeName", gradeName,
//                    "sectionName", sectionName,
//                    "students", new ArrayList<Map<String, Object>>()
//            )));
//
//            @SuppressWarnings("unchecked")
//            List<Map<String, Object>> studentList =
//                    (List<Map<String, Object>>) grades.get(gradeId).get("students");
//            studentList.add(toMap(s));
//
//            progGroup.put("totalCount", (int) progGroup.get("totalCount") + 1);
//        }
//
//        // Convert grades map to list
//        List<Map<String, Object>> result = new ArrayList<>();
//        for (Map<String, Object> prog : programMap.values()) {
//            @SuppressWarnings("unchecked")
//            Map<Long, Map<String, Object>> gradesMap =
//                    (Map<Long, Map<String, Object>>) prog.get("grades");
//            prog.put("grades", new ArrayList<>(gradesMap.values()));
//            result.add(prog);
//        }
//        return result;
//    }
//
//    @PostMapping
//    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
//        var s = Student.builder()
//                .name((String) body.get("name"))
//                .address((String) body.get("address"))
//                .guardianName((String) body.get("guardianName"))
//                .contact((String) body.get("contact")).build();
//        if (body.get("facultyDetailsId") != null)
//            fdRepo.findById(Long.parseLong(body.get("facultyDetailsId").toString())).ifPresent(s::setFacultyDetail);
//        if (body.get("gradesId") != null)
//            gsRepo.findById(Long.parseLong(body.get("gradesId").toString())).ifPresent(s::setGradeSection);
//        return ResponseEntity.ok(toMap(repo.save(s)));
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
//        return repo.findById(id).map(s -> {
//            if (body.get("name") != null) s.setName((String) body.get("name"));
//            if (body.get("address") != null) s.setAddress((String) body.get("address"));
//            if (body.get("guardianName") != null) s.setGuardianName((String) body.get("guardianName"));
//            if (body.get("contact") != null) s.setContact((String) body.get("contact"));
//            if (body.get("facultyDetailsId") != null)
//                fdRepo.findById(Long.parseLong(body.get("facultyDetailsId").toString())).ifPresent(s::setFacultyDetail);
//            if (body.get("gradesId") != null)
//                gsRepo.findById(Long.parseLong(body.get("gradesId").toString())).ifPresent(s::setGradeSection);
//            return ResponseEntity.ok(toMap(repo.save(s)));
//        }).orElse(ResponseEntity.notFound().build());
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> delete(@PathVariable Long id) {
//        repo.deleteById(id);
//        return ResponseEntity.ok().build();
//    }
//
//    /** Download Excel template for bulk student upload */
//    @GetMapping("/excel-template")
//    public void downloadTemplate(HttpServletResponse response) {
//        try (Workbook wb = new XSSFWorkbook()) {
//            Sheet sheet = wb.createSheet("Students");
//            Sheet programSheet = wb.createSheet("Programs_Reference");
//            Sheet gradeSheet = wb.createSheet("Grades_Reference");
//
//            // Header style
//            CellStyle headerStyle = wb.createCellStyle();
//            Font font = wb.createFont();
//            font.setBold(true);
//            headerStyle.setFont(font);
//            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
//            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//
//            // Student sheet headers
//            Row header = sheet.createRow(0);
//            String[] cols = {"Name*", "Address", "GuardianName", "Contact", "ProgramId*", "GradeSectionId*"};
//            for (int i = 0; i < cols.length; i++) {
//                Cell c = header.createCell(i);
//                c.setCellValue(cols[i]);
//                c.setCellStyle(headerStyle);
//                sheet.setColumnWidth(i, 5000);
//            }
//
//            // Sample row
//            Row sample = sheet.createRow(1);
//            sample.createCell(0).setCellValue("Student Name Here");
//            sample.createCell(1).setCellValue("Address");
//            sample.createCell(2).setCellValue("Guardian Name");
//            sample.createCell(3).setCellValue("9800000000");
//            sample.createCell(4).setCellValue("See Programs_Reference sheet for ID");
//            sample.createCell(5).setCellValue("See Grades_Reference sheet for ID");
//
//            // Programs reference sheet
//            Row ph = programSheet.createRow(0);
//            ph.createCell(0).setCellValue("ProgramId"); ph.createCell(0).setCellStyle(headerStyle);
//            ph.createCell(1).setCellValue("ProgramName"); ph.createCell(1).setCellStyle(headerStyle);
//            int pr = 1;
//            for (var fd : fdRepo.findAll()) {
//                Row r = programSheet.createRow(pr++);
//                r.createCell(0).setCellValue(fd.getId());
//                r.createCell(1).setCellValue(fd.getName());
//            }
//            programSheet.setColumnWidth(0, 3000); programSheet.setColumnWidth(1, 8000);
//
//            // Grade sections reference sheet
//            Row gh = gradeSheet.createRow(0);
//            gh.createCell(0).setCellValue("GradeSectionId"); gh.createCell(0).setCellStyle(headerStyle);
//            gh.createCell(1).setCellValue("Grade"); gh.createCell(1).setCellStyle(headerStyle);
//            gh.createCell(2).setCellValue("Section"); gh.createCell(2).setCellStyle(headerStyle);
//            int gr = 1;
//            for (var gs : gsRepo.findAll()) {
//                Row r = gradeSheet.createRow(gr++);
//                r.createCell(0).setCellValue(gs.getId());
//                r.createCell(1).setCellValue(gs.getGrade() != null ? gs.getGrade().getName() : "");
//                r.createCell(2).setCellValue(gs.getSection() != null ? gs.getSection().getName() : "");
//            }
//            gradeSheet.setColumnWidth(0, 4000); gradeSheet.setColumnWidth(1, 4000); gradeSheet.setColumnWidth(2, 4000);
//
//            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//            response.setHeader("Content-Disposition", "attachment; filename=\"students_template.xlsx\"");
//            wb.write(response.getOutputStream());
//        } catch (Exception e) {
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    /** Bulk upload students from Excel */
//    @PostMapping("/bulk-upload")
//    @Transactional
//    public ResponseEntity<?> bulkUpload(@RequestParam("file") MultipartFile file) {
//        if (file.isEmpty())
//            return ResponseEntity.badRequest().body(Map.of("message", "File is empty"));
//
//        int created = 0, skipped = 0;
//        List<String> errors = new ArrayList<>();
//
//        try (InputStream is = file.getInputStream(); Workbook wb = new XSSFWorkbook(is)) {
//            Sheet sheet = wb.getSheetAt(0);
//
//            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
//                Row row = sheet.getRow(i);
//                if (row == null) continue;
//
//                String name = getCellString(row.getCell(0));
//                if (name.isBlank()) continue; // skip empty rows
//
//                String address   = getCellString(row.getCell(1));
//                String guardian  = getCellString(row.getCell(2));
//                String contact   = getCellString(row.getCell(3));
//                String fdIdStr   = getCellString(row.getCell(4));
//                String gsIdStr   = getCellString(row.getCell(5));
//
//                try {
//                    Student s = Student.builder()
//                            .name(name).address(address)
//                            .guardianName(guardian).contact(contact).build();
//
//                    if (!fdIdStr.isBlank()) {
//                        long fdId = (long) Double.parseDouble(fdIdStr);
//                        int finalI1 = i;
//                        fdRepo.findById(fdId).ifPresentOrElse(
//                                s::setFacultyDetail,
//                                () -> errors.add("Row " + (finalI1 +1) + ": Program ID " + fdIdStr + " not found")
//                        );
//                    }
//                    if (!gsIdStr.isBlank()) {
//                        long gsId = (long) Double.parseDouble(gsIdStr);
//                        int finalI = i;
//                        gsRepo.findById(gsId).ifPresentOrElse(
//                                s::setGradeSection,
//                                () -> errors.add("Row " + (finalI +1) + ": Grade Section ID " + gsIdStr + " not found")
//                        );
//                    }
//
//                    repo.save(s);
//                    created++;
//                } catch (Exception e) {
//                    errors.add("Row " + (i+1) + ": " + e.getMessage());
//                    skipped++;
//                }
//            }
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of("message", "Failed to parse file: " + e.getMessage()));
//        }
//
//        return ResponseEntity.ok(Map.of(
//                "message", "Upload complete",
//                "created", created,
//                "skipped", skipped,
//                "errors", errors
//        ));
//    }
//
//    private String getCellString(Cell cell) {
//        if (cell == null) return "";
//        return switch (cell.getCellType()) {
//            case STRING -> cell.getStringCellValue().trim();
//            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
//            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
//            default -> "";
//        };
//    }
//
//    private Map<String, Object> toMap(Student s) {
//        Map<String, Object> m = new LinkedHashMap<>();
//        m.put("id", s.getId()); m.put("name", s.getName());
//        m.put("address", s.getAddress()); m.put("guardianName", s.getGuardianName());
//        m.put("contact", s.getContact());
//        if (s.getFacultyDetail() != null) {
//            m.put("facultyDetailsId", s.getFacultyDetail().getId());
//            m.put("facultyName", s.getFacultyDetail().getName());
//        }
//        if (s.getGradeSection() != null) {
//            m.put("gradesId", s.getGradeSection().getId());
//            if (s.getGradeSection().getGrade() != null) {
//                m.put("gradeId", s.getGradeSection().getGrade().getId());
//                m.put("gradeName", s.getGradeSection().getGrade().getName());
//            }
//            if (s.getGradeSection().getSection() != null) {
//                m.put("sectionId", s.getGradeSection().getSection().getId());
//                m.put("sectionName", s.getGradeSection().getSection().getName());
//            }
//        }
//        return m;
//    }
//}



package com.rms.controller;

import com.rms.entity.*;
import com.rms.repository.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired private StudentRepository repo;
    @Autowired private ProgramRepository fdRepo;
    @Autowired private GradeSectionMappingRepository gsRepo;
    @Autowired private ResultRepository resultRepo;
    @Autowired private MarksRepository marksRepo;
    @Autowired private AttendanceRepository attendanceRepo;
    @Autowired private AssignmentStudentRepository assignmentStudentRepo;
    @Autowired private RemarkRepository remarkRepo;

    /**
     * Builds a lowercase, trimmed composite key from the 5 identity fields.
     * Used as the element in the HashSet for duplicate detection.
     * Algorithm: String concatenation with pipe separator
     */
    private String buildDuplicateKey(String name, String guardianName,
                                     String contact, Long facultyDetailsId,
                                     Long gradesId) {
        return (name == null ? "" : name.trim().toLowerCase()) + "|"
                + (guardianName == null ? "" : guardianName.trim().toLowerCase()) + "|"
                + (contact == null ? "" : contact.trim().toLowerCase()) + "|"
                + (facultyDetailsId == null ? "0" : facultyDetailsId) + "|"
                + (gradesId == null ? "0" : gradesId);
    }

    /**
     * Builds a HashSet of composite keys from all existing students in DB.
     * Used before any insert (single or bulk) to detect duplicates in
     * Algorithm: HashMap → HashSet population
     */
    private Set<String> buildExistingKeySet() {
        // HashMap: id → compositeKey (intermediate — used to populate HashSet)
        // HashSet: stores all unique composite keys for O(1) contains() check
        Set<String> existingKeys = new HashSet<>();
        for (Student s : repo.findAll()) {
            Long fdId = s.getProgram() != null ? s.getProgram().getId() : null;
            Long gsId = s.getGradeSection() != null ? s.getGradeSection().getId() : null;
            String key = buildDuplicateKey(s.getName(), s.getGuardianName(),
                    s.getContact(), fdId, gsId);
            // HashSet.add()  average, ignores duplicates automatically
            existingKeys.add(key);
        }
        return existingKeys;
    }


    @GetMapping
    public List<Map<String, Object>> getAll() {
        return repo.findAll().stream().map(this::toMap).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return repo.findById(id).map(s -> ResponseEntity.ok(toMap(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam(required = false) String name,
                                            @RequestParam(required = false) Long id) {
        if (id != null) return repo.findById(id).map(s -> List.of(toMap(s))).orElse(List.of());
        if (name != null && !name.isBlank())
            return repo.findByNameContaining(name).stream().map(this::toMap).toList();
        return repo.findAll().stream().map(this::toMap).toList();
    }

    @GetMapping("/grouped")
    @Transactional
    public List<Map<String, Object>> getGrouped() {
        List<Student> all = repo.findAll();

        // Algorithm: HashMap for O(1) group lookup by programId
        // Key: programId (Long)
        // Value: program group map containing nested HashMap keyed by gradeId
        Map<Long, Map<String, Object>> programMap = new LinkedHashMap<>();

        for (Student s : all) {
            // Program grouping
            // HashMap.putIfAbsent() → O(1) — creates group only on first encounter
            Long programId = s.getProgram() != null ? s.getProgram().getId() : 0L;
            String programName = s.getProgram() != null ? s.getProgram().getName() : "Unassigned";

            programMap.putIfAbsent(programId, new LinkedHashMap<>(Map.of(
                    "programId", programId,
                    "programName", programName,
                    "grades", new LinkedHashMap<Long, Map<String, Object>>(),
                    "totalCount", 0
            )));
            Map<String, Object> progGroup = programMap.get(programId); // O(1)

            //Grade grouping
            // Nested HashMap.putIfAbsent() → O(1)
            Long gradeId = (s.getGradeSection() != null && s.getGradeSection().getGrade() != null)
                    ? s.getGradeSection().getGrade().getId() : 0L;
            String gradeName = (s.getGradeSection() != null && s.getGradeSection().getGrade() != null)
                    ? s.getGradeSection().getGrade().getName() : "Unassigned";
            String sectionName = (s.getGradeSection() != null && s.getGradeSection().getSection() != null)
                    ? s.getGradeSection().getSection().getName() : "";

            @SuppressWarnings("unchecked")
            Map<Long, Map<String, Object>> grades =
                    (Map<Long, Map<String, Object>>) progGroup.get("grades");

            grades.putIfAbsent(gradeId, new LinkedHashMap<>(Map.of(
                    "gradeId", gradeId,
                    "gradeName", gradeName,
                    "sectionName", sectionName,
                    "students", new ArrayList<Map<String, Object>>()
            )));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> studentList =
                    (List<Map<String, Object>>) grades.get(gradeId).get("students");
            studentList.add(toMap(s));
            progGroup.put("totalCount", (int) progGroup.get("totalCount") + 1);
        }

        // Convert nested grade HashMaps to Lists for JSON serialization
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> prog : programMap.values()) {
            @SuppressWarnings("unchecked")
            Map<Long, Map<String, Object>> gradesMap =
                    (Map<Long, Map<String, Object>>) prog.get("grades");
            prog.put("grades", new ArrayList<>(gradesMap.values()));
            result.add(prog);
        }
        return result;
    }

    /**
     * Single student add with duplicate detection.
     * Algorithm:
     * 1. Build HashSet of existing composite keys
     * 2. Build new student's composite key
     * 3. HashSet.contains() check
     * 4. If duplicate → reject with 409 Conflict
     * 5. If unique → save
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String guardianName = (String) body.get("guardianName");
        String contact = (String) body.get("contact");
        String email = (String) body.get("email");
        Long fdId = body.get("facultyDetailsId") != null
                ? Long.parseLong(body.get("facultyDetailsId").toString()) : null;
        Long gsId = body.get("gradesId") != null
                ? Long.parseLong(body.get("gradesId").toString()) : null;

        if (name == null || name.trim().isEmpty())
            return ResponseEntity.badRequest().body(Map.of("message", "Name is required"));

        // Step 1: Build HashSet of all existing keys
        Set<String> existingKeys = buildExistingKeySet();

        // Step 2 & 3: Build and check new student's key
        String newKey = buildDuplicateKey(name, guardianName, contact, fdId, gsId);
        if (existingKeys.contains(newKey)) {
            // Duplicate detected — all 5 fields match an existing student
            return ResponseEntity.status(409).body(Map.of(
                    "message", "Duplicate student: a student with the same name, guardian, " +
                            "contact, program and grade already exists.",
                    "algorithm", "HashSet composite key lookup — O(1)"
            ));
        }

        // Step 4: Save unique student
        Student s = Student.builder()
                .name(name.trim())
                .address((String) body.get("address"))
                .guardianName(guardianName)
                .contact(contact)
                .email(email)
                .build();
        if (fdId != null) fdRepo.findById(fdId).ifPresent(s::setProgram);
        if (gsId != null) gsRepo.findById(gsId).ifPresent(s::setGradeSection);
        return ResponseEntity.ok(toMap(repo.save(s)));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return repo.findById(id).map(s -> {
            String name = (String) body.get("name");
            String guardianName = (String) body.get("guardianName");
            String contact = (String) body.get("contact");
            Long fdId = body.get("facultyDetailsId") != null
                    ? Long.parseLong(body.get("facultyDetailsId").toString()) : null;
            Long gsId = body.get("gradesId") != null
                    ? Long.parseLong(body.get("gradesId").toString()) : null;

            // On update: check for duplicates EXCLUDING the student being updated
            // Algorithm: Build HashSet, remove own key first, then check
            Set<String> existingKeys = buildExistingKeySet();

            // Remove own current key so self-update doesn't trigger false duplicate
            Long currentFdId = s.getProgram() != null ? s.getProgram().getId() : null;
            Long currentGsId = s.getGradeSection() != null ? s.getGradeSection().getId() : null;
            String ownKey = buildDuplicateKey(s.getName(), s.getGuardianName(),
                    s.getContact(), currentFdId, currentGsId);
            existingKeys.remove(ownKey); // O(1) removal

            // Check if new values would clash with another existing student
            String newKey = buildDuplicateKey(name, guardianName, contact, fdId, gsId);
            if (existingKeys.contains(newKey)) {
                return ResponseEntity.status(409).body(Map.of(
                        "message", "Duplicate: another student with all these same details already exists."
                ));
            }

            if (name != null) s.setName(name.trim());
            if (body.get("address") != null) s.setAddress((String) body.get("address"));
            if (guardianName != null) s.setGuardianName(guardianName);
            if (contact != null) s.setContact(contact);
            if (body.get("email") != null) s.setEmail((String) body.get("email"));
            if (fdId != null) fdRepo.findById(fdId).ifPresent(s::setProgram);
            if (gsId != null) gsRepo.findById(gsId).ifPresent(s::setGradeSection);
            return ResponseEntity.ok(toMap(repo.save(s)));
        }).orElse(ResponseEntity.notFound().build());
    }


    /**
     * Delete a student and ALL their related data.
     * Correct deletion order (must follow FK chain):
     *   1. result_marks  — join table, references both results and marks
     *   2. results       — references student
     *   3. attendance    — references student
     *   4. assignment_student — references student
     *   5. remarks       — references student
     *   6. student       — finally safe to delete
     * Uses native SQL for steps 1-2 to bypass Hibernate flush-order issues
     * that caused the previous FK constraint violation.
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repo.existsById(id))
            return ResponseEntity.notFound().build();

        // Step 1: Delete result_marks join table rows (native SQL — avoids Hibernate flush issue)
        resultRepo.deleteResultMarksByStudentId(id);

        // Step 2: Delete results rows for this student (native SQL)
        resultRepo.deleteResultsByStudentId(id);

        // Step 3: Delete attendance records
        attendanceRepo.deleteByStudentId(id);

        // Step 4: Delete assignment_student records
        assignmentStudentRepo.deleteByStudentId(id);

        // Step 5: Delete remarks
        remarkRepo.deleteByStudentId(id);

        // Step 6: Now safe to delete the student
        repo.deleteById(id);

        return ResponseEntity.ok(Map.of("message", "Student deleted successfully"));
    }


    /** Download Excel template using NAMES (not IDs) for easy filling */
    @GetMapping("/excel-template")
    public void downloadTemplate(HttpServletResponse response) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet        = wb.createSheet("Students");
            Sheet programSheet = wb.createSheet("Programs_Reference");
            Sheet gradeSheet   = wb.createSheet("Grades_Reference");

            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);


            CellStyle noteStyle = wb.createCellStyle();
            Font noteFont = wb.createFont();
            noteFont.setItalic(true);
            noteFont.setColor(IndexedColors.DARK_RED.getIndex());
            noteStyle.setFont(noteFont);

            // Students sheet — columns use NAMES not IDs
            String[] cols = {
                    "Name*", "Address", "GuardianName", "Contact", "Email",
                    "ProgramName*", "GradeName*", "SectionName"
            };
            Row header = sheet.createRow(0);
            for (int i = 0; i < cols.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 6000);
            }

            // Note row explaining the format
            Row noteRow = sheet.createRow(1);
            Cell noteCell = noteRow.createCell(0);
            noteCell.setCellValue(
                    "NOTE: Fill ProgramName exactly as shown in Programs_Reference sheet. " +
                            "GradeName and SectionName must match Grades_Reference sheet. " +
                            "Students are saved immediately on upload — no separate save needed.");
            noteCell.setCellStyle(noteStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 7));

            // Sample data row
            Row sample = sheet.createRow(2);
            sample.createCell(0).setCellValue("Student Name");
            sample.createCell(1).setCellValue("Kathmandu");
            sample.createCell(2).setCellValue("Guardian Name");
            sample.createCell(3).setCellValue("9800000000");
            sample.createCell(4).setCellValue("student@example.com");

            // Fill sample program/grade from DB if available
            List<Program> programs = fdRepo.findAll();
            List<GradeSectionMapping> gradeSections = gsRepo.findAll();
            if (!programs.isEmpty())
                sample.createCell(5).setCellValue(programs.get(0).getName());
            else
                sample.createCell(5).setCellValue("e.g. Chemistry First Year");

            if (!gradeSections.isEmpty() && gradeSections.get(0).getGrade() != null) {
                sample.createCell(6).setCellValue(gradeSections.get(0).getGrade().getName());
                if (gradeSections.get(0).getSection() != null)
                    sample.createCell(7).setCellValue(gradeSections.get(0).getSection().getName());
            } else {
                sample.createCell(6).setCellValue("e.g. 11");
                sample.createCell(7).setCellValue("e.g. A");
            }

            //  Programs reference sheet
            Row ph = programSheet.createRow(0);
            Cell phc0 = ph.createCell(0); phc0.setCellValue("ProgramName (use exactly as written)");
            phc0.setCellStyle(headerStyle);
            programSheet.setColumnWidth(0, 12000);

            int pr = 1;
            for (Program fd : programs) {
                programSheet.createRow(pr++).createCell(0).setCellValue(fd.getName());
            }

            //  Grades reference sheet
            Row gh = gradeSheet.createRow(0);
            Cell ghc0 = gh.createCell(0); ghc0.setCellValue("GradeName");   ghc0.setCellStyle(headerStyle);
            Cell ghc1 = gh.createCell(1); ghc1.setCellValue("SectionName"); ghc1.setCellStyle(headerStyle);
            Cell ghc2 = gh.createCell(2); ghc2.setCellValue("GradeSectionId (for reference only)");
            ghc2.setCellStyle(headerStyle);
            gradeSheet.setColumnWidth(0, 5000);
            gradeSheet.setColumnWidth(1, 5000);
            gradeSheet.setColumnWidth(2, 7000);

            int gr = 1;
            for (GradeSectionMapping gs : gradeSections) {
                Row r = gradeSheet.createRow(gr++);
                r.createCell(0).setCellValue(gs.getGrade() != null ? gs.getGrade().getName() : "");
                r.createCell(1).setCellValue(gs.getSection() != null ? gs.getSection().getName() : "");
                r.createCell(2).setCellValue(gs.getId());
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"students_template.xlsx\"");
            wb.write(response.getOutputStream());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Bulk upload students from Excel using NAMES (not IDs).
     * Algorithm — HashMap for name-to-ID lookup:
     * Phase 0: Build two HashMaps from DB for O(1) name→ID resolution:
     *   programMap:      HashMap<lowerCaseName, FacultyDetail>
     *   gradeSectionMap: HashMap<"gradeName|sectionName", GradeSectionMapping>
     * Phase 1: Build HashSet of existing composite keys from DB
     * Phase 2: For each Excel row:
     *   a. Resolve program name → ID using HashMap.get()
     *   b. Resolve grade+section name → ID using HashMap.get()
     *   c. Check duplicate using HashSet —
     *   d. Check intra-file duplicate using session HashMap
     *   e. Save if unique
     */
    @PostMapping("/bulk-upload")
    @Transactional
    public ResponseEntity<?> bulkUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("message", "File is empty"));

        int created = 0, skippedDuplicate = 0, skippedError = 0;
        List<String> duplicates = new ArrayList<>();
        List<String> errors     = new ArrayList<>();

        //  Phase 0: Build HashMaps from DB for O(1) name lookup

        // HashMap: programName (lowercase) → FacultyDetail entity
        Map<String, Program> programMap = new HashMap<>();
        for (Program fd : fdRepo.findAll())
            programMap.put(fd.getName().trim().toLowerCase(), fd);

        // HashMap: "gradeName|sectionName" (lowercase) → GradeSectionMapping entity
        // Key: grade name + pipe + section name (empty string if no section)
        Map<String, GradeSectionMapping> gradeSectionMap = new HashMap<>();
        for (GradeSectionMapping gs : gsRepo.findAll()) {
            String gradeName   = gs.getGrade()   != null ? gs.getGrade().getName().trim().toLowerCase()   : "";
            String sectionName = gs.getSection() != null ? gs.getSection().getName().trim().toLowerCase() : "";
            // Store with section: "11|a"
            gradeSectionMap.put(gradeName + "|" + sectionName, gs);
            // Also store without section for cases where section is blank: "11|"
            if (sectionName.isEmpty())
                gradeSectionMap.put(gradeName + "|", gs);
        }

        // Phase 1: Build HashSet of existing composite keys
        Set<String> existingKeys = buildExistingKeySet();

        // Phase 2: HashMap for intra-file duplicate detection
        Map<String, Integer> sessionKeys = new HashMap<>();

        try (InputStream is = file.getInputStream(); Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);

            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Read columns: Name | Address | GuardianName | Contact | Email | ProgramName | GradeName | SectionName
                String name         = getCellString(row.getCell(0));
                if (name.isBlank()) continue;

                String address      = getCellString(row.getCell(1));
                String guardianName = getCellString(row.getCell(2));
                String contact      = getCellString(row.getCell(3));
                String email        = getCellString(row.getCell(4));
                String programName  = getCellString(row.getCell(5)).toLowerCase().trim();
                String gradeName    = getCellString(row.getCell(6)).toLowerCase().trim();
                String sectionName  = getCellString(row.getCell(7)).toLowerCase().trim();

                // Resolve program name → entity using HashMap
                Program fd = null;
                if (!programName.isBlank()) {
                    fd = programMap.get(programName);
                    if (fd == null) {
                        errors.add("Row " + (i + 1) + ": Program '" + getCellString(row.getCell(5)) +
                                "' does not exist. Check Programs_Reference sheet.");
                        skippedError++;
                        continue;
                    }
                }

                // Resolve grade+section → entity using HashMap
                GradeSectionMapping gs = null;
                if (!gradeName.isBlank()) {
                    String gsKey = gradeName + "|" + sectionName;
                    gs = gradeSectionMap.get(gsKey); // O(1) HashMap lookup
                    if (gs == null) {
                        errors.add("Row " + (i + 1) + ": Grade '" + getCellString(row.getCell(6)) +
                                "' Section '" + getCellString(row.getCell(7)) +
                                "' does not exist. Check Grades_Reference sheet.");
                        skippedError++;
                        continue;
                    }
                }

                Long fdId = fd != null ? fd.getId() : null;
                Long gsId = gs != null ? gs.getId() : null;

                // Duplicate check
                String key = buildDuplicateKey(name, guardianName, contact, fdId, gsId);

                if (existingKeys.contains(key)) {
                    duplicates.add("Row " + (i + 1) + ": '" + name +
                            "' already exists in database (same name, guardian, contact, program, grade).");
                    skippedDuplicate++;
                    continue;
                }

                if (sessionKeys.containsKey(key)) {
                    duplicates.add("Row " + (i + 1) + ": '" + name +
                            "' is a duplicate of row " + sessionKeys.get(key) + " in this file.");
                    skippedDuplicate++;
                    continue;
                }

                // Save unique student
                try {
                    Student s = Student.builder()
                            .name(name.trim()).address(address)
                            .guardianName(guardianName).contact(contact).email(email)
                            .build();
                    if (fd != null) s.setProgram(fd);
                    if (gs != null) s.setGradeSection(gs);

                    repo.save(s);

                    // Add to both sets — prevents duplicates from later rows
                    existingKeys.add(key);
                    sessionKeys.put(key, i + 1);
                    created++;

                } catch (Exception e) {
                    errors.add("Row " + (i + 1) + ": " + e.getMessage());
                    skippedError++;
                }
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Failed to read Excel file: " + e.getMessage()));
        }

        return ResponseEntity.ok(Map.of(
                "message",          created > 0
                        ? created + " student(s) saved successfully. No separate save button needed — data is in DB now."
                        : "No new students added.",
                "created",          created,
                "skippedDuplicate", skippedDuplicate,
                "skippedError",     skippedError,
                "duplicates",       duplicates,
                "errors",           errors
        ));
    }




    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private Map<String, Object> toMap(Student s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("name", s.getName());
        m.put("address", s.getAddress());
        m.put("guardianName", s.getGuardianName());
        m.put("contact", s.getContact());
        m.put("email", s.getEmail());
        if (s.getProgram() != null) {
            m.put("facultyDetailsId", s.getProgram().getId());
            m.put("facultyName", s.getProgram().getName());
        }
        if (s.getGradeSection() != null) {
            m.put("gradesId", s.getGradeSection().getId());
            if (s.getGradeSection().getGrade() != null) {
                m.put("gradeId", s.getGradeSection().getGrade().getId());
                m.put("gradeName", s.getGradeSection().getGrade().getName());
            }
            if (s.getGradeSection().getSection() != null) {
                m.put("sectionId", s.getGradeSection().getSection().getId());
                m.put("sectionName", s.getGradeSection().getSection().getName());
            }
        }
        return m;
    }
}