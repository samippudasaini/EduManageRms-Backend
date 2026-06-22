//package com.rms.controller;
//import com.rms.repository.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import java.util.*;
//
//@RestController @RequestMapping("/api/student-profile")
//public class StudentProfileController {
//    @Autowired private StudentRepository studentRepo;
//    @Autowired private AttendanceRepository attendanceRepo;
//    @Autowired private AssignmentStudentRepository asRepo;
//    @Autowired private ResultRepository resultRepo;
//    @Autowired private RemarkRepository remarkRepo;
//
//    @GetMapping("/{id}")
//    public ResponseEntity<?> getProfile(@PathVariable Long id) {
//        return studentRepo.findById(id).map(student -> {
//            Map<String,Object> profile = new LinkedHashMap<>();
//            profile.put("id", student.getId());
//            profile.put("name", student.getName());
//            profile.put("address", student.getAddress());
//            profile.put("guardianName", student.getGuardianName());
//            profile.put("contact", student.getContact());
//            if (student.getFacultyDetail() != null)
//                profile.put("facultyName", student.getFacultyDetail().getName());
//            if (student.getGradeSection() != null) {
//                if (student.getGradeSection().getGrade() != null)
//                    profile.put("gradeName", student.getGradeSection().getGrade().getName());
//                if (student.getGradeSection().getSection() != null)
//                    profile.put("sectionName", student.getGradeSection().getSection().getName());
//            }
//
//            // Attendance summary
//            try {
//                Object[] att = attendanceRepo.getAttendanceSummary(id);
//                if (att != null && att.length >= 3) {
//                    profile.put("totalWorkingDays", att[0]);
//                    profile.put("presentDays", att[1]);
//                    profile.put("absentDays", att[2]);
//                }
//            } catch (Exception ignored) {}
//
//            // Assignment summary
//            try {
//                Object[] as = asRepo.getAssignmentSummary(id);
//                if (as != null && as.length >= 3) {
//                    profile.put("totalAssignments", as[0]);
//                    profile.put("completedAssignments", as[1]);
//                    profile.put("incompleteAssignments", as[2]);
//                }
//            } catch (Exception ignored) {}
//
//            // Results
//            var results = resultRepo.findByStudentId(id).stream().map(r -> {
//                Map<String,Object> rm = new LinkedHashMap<>();
//                rm.put("id", r.getId());
//                rm.put("examinationName", r.getExamination() != null ? r.getExamination().getName() : "");
//                rm.put("grade", r.getGrade());
//                rm.put("percentage", r.getPercentage());
//                rm.put("rank", r.getRank());
//                return rm;
//            }).toList();
//            profile.put("results", results);
//
//            // Remarks
//            var remarks = remarkRepo.findByStudentIdOrderByIdDesc(id).stream().map(r ->
//                Map.of("id", r.getId(), "description", r.getDescription() != null ? r.getDescription() : "",
//                        "date", r.getDate() != null ? r.getDate().toString() : "")).toList();
//            profile.put("remarks", remarks);
//
//            return ResponseEntity.ok(profile);
//        }).orElse(ResponseEntity.notFound().build());
//    }
//}

package com.rms.controller;

import com.rms.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/student-profile")
public class StudentProfileController {

    @Autowired private StudentRepository studentRepo;
    @Autowired private AttendanceRepository attendanceRepo;
    @Autowired private AssignmentStudentRepository asRepo;
    @Autowired private ResultRepository resultRepo;
    @Autowired private RemarkRepository remarkRepo;

    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<?> getProfile(@PathVariable Long id) {
        return studentRepo.findById(id).map(student -> {
            Map<String, Object> profile = new LinkedHashMap<>();
            profile.put("id", student.getId());
            profile.put("name", student.getName());
            profile.put("address", student.getAddress());
            profile.put("guardianName", student.getGuardianName());
            profile.put("contact", student.getContact());

            if (student.getFacultyDetail() != null)
                profile.put("facultyName", student.getFacultyDetail().getName());
            if (student.getGradeSection() != null) {
                if (student.getGradeSection().getGrade() != null)
                    profile.put("gradeName", student.getGradeSection().getGrade().getName());
                if (student.getGradeSection().getSection() != null)
                    profile.put("sectionName", student.getGradeSection().getSection().getName());
            }

            // Attendance summary
            try {
                Object[] att = attendanceRepo.getAttendanceSummary(id);
                if (att != null && att.length >= 3) {
                    profile.put("totalWorkingDays", att[0]);
                    profile.put("presentDays", att[1]);
                    profile.put("absentDays", att[2]);
                }
            } catch (Exception ignored) {}

            // Assignment summary
            try {
                Object[] as = asRepo.getAssignmentSummary(id);
                if (as != null && as.length >= 3) {
                    profile.put("totalAssignments", as[0]);
                    profile.put("completedAssignments", as[1]);
                    profile.put("incompleteAssignments", as[2]);
                }
            } catch (Exception ignored) {}

            // Results — access examination inside @Transactional so session is open
            var results = resultRepo.findByStudentId(id).stream().map(r -> {
                Map<String, Object> rm = new LinkedHashMap<>();
                rm.put("id", r.getId());
                rm.put("examinationName", r.getExamination() != null ? r.getExamination().getName() : "");
                rm.put("grade", r.getGrade());
                rm.put("percentage", r.getPercentage());
                rm.put("rank", r.getRank());
                return rm;
            }).toList();
            profile.put("results", results);

            // Remarks
            var remarks = remarkRepo.findByStudentIdOrderByIdDesc(id).stream().map(r ->
                    Map.of("id", r.getId(),
                            "description", r.getDescription() != null ? r.getDescription() : "",
                            "date", r.getDate() != null ? r.getDate().toString() : "")).toList();
            profile.put("remarks", remarks);

            return ResponseEntity.ok(profile);
        }).orElse(ResponseEntity.notFound().build());
    }
}