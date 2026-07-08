////package com.rms.service;
////
////import com.rms.entity.*;
////import com.rms.repository.*;
////import jakarta.transaction.Transactional;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.stereotype.Service;
////import java.util.*;
////
////@Service
////public class ResultService {
////    @Autowired private ResultRepository resultRepo;
////    @Autowired private MarksRepository marksRepo;
////    @Autowired private StudentRepository studentRepo;
////    @Autowired private FacultyDetailRepository fdRepo;
////
////    @Transactional
////    public void createResultsForExamination(Examination exam, FacultyDetail fd) {
////        List<Student> students = studentRepo.findByFacultyDetailId(fd.getId());
////        for (Student student : students) {
////            boolean exists = resultRepo.findByExaminationIdAndFacultyDetailIdAndStudentId(
////                    exam.getId(), fd.getId(), student.getId()).isPresent();
////            if (!exists) {
////                Result result = Result.builder()
////                        .examination(exam)
////                        .facultyDetail(fd)
////                        .student(student)
////                        .grade("")
////                        .percentage(0.0)
////                        .rank("-")
////                        .build();
////                Result savedResult = resultRepo.save(result);
////                for (FacultyDetailSubject fds : fd.getSubjectMappings()) {
////                    Subject subject = fds.getSubject();
////                    Marks marks = marksRepo.save(Marks.builder()
////                            .subject(subject).practical(0f).theory(0f).grade("").gradePoint(0.0).build());
////                    savedResult.getMarks().add(marks);
////                }
////                resultRepo.save(savedResult);
////            }
////        }
////    }
////
////    @Transactional
////    public void updateMarks(Long resultId, List<Map<String, Object>> marksData) {
////        Result result = resultRepo.findById(resultId).orElseThrow(
////                () -> new RuntimeException("Result not found: " + resultId));
////
////        float totalObtained = 0f;
////        float totalMax = 0f;
////        boolean hasFail = false;
////
////        for (Map<String, Object> markData : marksData) {
////            Long marksId = Long.parseLong(markData.get("id").toString());
////            float practical = Float.parseFloat(markData.get("practical").toString());
////            float theory = Float.parseFloat(markData.get("theory").toString());
////
////            Marks marks = marksRepo.findById(marksId).orElseThrow();
////            Subject subject = marks.getSubject();
////
////            float obtained = practical + theory;
////            float total = subject.getFullMarks() != null ? subject.getFullMarks() : 100f;
////            float passMarks = subject.getPassMarks() != null ? subject.getPassMarks() : 40f;
////            float pct = total > 0 ? (obtained / total) * 100f : 0f;
////
////            marks.setPractical(practical);
////            marks.setTheory(theory);
////            marks.setGrade(calculateGrade(pct));
////            marks.setGradePoint((double) Math.round(pct * 4.0 / 100.0 * 100) / 100.0);
////            marksRepo.save(marks);
////
////            totalObtained += obtained;
////            totalMax += total;
////            if (obtained < passMarks) hasFail = true;
////        }
////
////        double percentage = totalMax > 0 ? (totalObtained / totalMax) * 100.0 : 0.0;
////        result.setPercentage(Math.round(percentage * 100.0) / 100.0);
////
////        if (hasFail) {
////            result.setGrade("NG");
////            result.setRank("FAIL");
////        } else {
////            result.setGrade(calculateGrade((float) percentage));
////        }
////        resultRepo.save(result);
////
////        if (!hasFail) {
////            updateRankings(result.getExamination().getId());
////        }
////    }
////
////    private void updateRankings(Long examId) {
////        List<Result> results = resultRepo.findRankableResults(examId);
////        results.sort((a, b) -> Double.compare(
////                b.getPercentage() != null ? b.getPercentage() : 0.0,
////                a.getPercentage() != null ? a.getPercentage() : 0.0));
////
////        int rank = 1;
////        Double prevPct = null;
////        int prevRank = 1;
////        for (Result r : results) {
////            if (prevPct != null && r.getPercentage() != null && r.getPercentage().equals(prevPct)) {
////                r.setRank(String.valueOf(prevRank));
////            } else {
////                prevRank = rank;
////                r.setRank(String.valueOf(rank));
////                prevPct = r.getPercentage();
////            }
////            resultRepo.save(r);
////            rank++;
////        }
////    }
////
////    private String calculateGrade(float pct) {
////        if (pct >= 90) return "A+";
////        else if (pct >= 80) return "A";
////        else if (pct >= 70) return "B+";
////        else if (pct >= 60) return "B";
////        else if (pct >= 50) return "C";
////        else if (pct >= 40) return "D";
////        else return "F";
////    }
////
////    public List<Map<String, Object>> getResultsWithMarks(Long examId, Long fdId) {
////        List<Result> results = resultRepo.findByExaminationIdAndFacultyDetailId(examId, fdId);
////        List<Map<String, Object>> data = new ArrayList<>();
////        for (Result result : results) {
////            Map<String, Object> rd = new LinkedHashMap<>();
////            Student student = result.getStudent();
////            rd.put("resultId", result.getId());
////            rd.put("grade", result.getGrade());
////            rd.put("percentage", result.getPercentage());
////            rd.put("rank", result.getRank());
////            rd.put("studentId", student.getId());
////            rd.put("studentName", student.getName());
////            rd.put("studentAddress", student.getAddress());
////            rd.put("guardianName", student.getGuardianName());
////
////            List<Map<String, Object>> marksList = new ArrayList<>();
////            for (Marks marks : result.getMarks()) {
////                Map<String, Object> mm = new LinkedHashMap<>();
////                mm.put("id", marks.getId());
////                mm.put("subjectId", marks.getSubject().getId());
////                mm.put("subjectName", marks.getSubject().getName());
////                mm.put("theory", marks.getTheory());
////                mm.put("practical", marks.getPractical());
////                mm.put("grade", marks.getGrade());
////                mm.put("gradePoint", marks.getGradePoint());
////                mm.put("fullMarks", marks.getSubject().getFullMarks());
////                mm.put("passMarks", marks.getSubject().getPassMarks());
////                mm.put("theoryMax", marks.getSubject().getTheory());
////                mm.put("practicalMax", marks.getSubject().getPractical());
////                marksList.add(mm);
////            }
////            rd.put("marks", marksList);
////            data.add(rd);
////        }
////        return data;
////    }
////
////    public Map<String, Object> getMarksheetData(Long resultId) {
////        Result result = resultRepo.findById(resultId)
////                .orElseThrow(() -> new RuntimeException("Result not found"));
////        return buildMarksheet(result);
////    }
////
////    public List<Map<String, Object>> getAllMarksheetData(Long examId, Long fdId) {
////        return resultRepo.findByExaminationIdAndFacultyDetailId(examId, fdId)
////                .stream().map(this::buildMarksheet).toList();
////    }
////
////    private Map<String, Object> buildMarksheet(Result result) {
////        Map<String, Object> ms = new LinkedHashMap<>();
////        Student s = result.getStudent();
////        Examination exam = result.getExamination();
////        FacultyDetail fd = result.getFacultyDetail();
////
////        ms.put("resultId", result.getId());
////        ms.put("grade", result.getGrade());
////        ms.put("percentage", result.getPercentage());
////        ms.put("rank", result.getRank());
////
////        // Student info
////        Map<String, Object> student = new LinkedHashMap<>();
////        student.put("id", s.getId()); student.put("name", s.getName());
////        student.put("address", s.getAddress()); student.put("guardianName", s.getGuardianName());
////        student.put("contact", s.getContact());
////        ms.put("student", student);
////
////        // Exam info
////        Map<String, Object> examination = new LinkedHashMap<>();
////        examination.put("id", exam.getId()); examination.put("name", exam.getName());
////        examination.put("year", exam.getYear());
////        ms.put("examination", examination);
////
////        // Program info
////        Map<String, Object> program = new LinkedHashMap<>();
////        program.put("id", fd.getId()); program.put("name", fd.getName());
////        ms.put("program", program);
////
////        // Marks
////        float totalObtained = 0f, totalMax = 0f;
////        List<Map<String, Object>> marksList = new ArrayList<>();
////        for (Marks m : result.getMarks()) {
////            Subject sub = m.getSubject();
////            float obtained = (m.getTheory() != null ? m.getTheory() : 0f)
////                    + (m.getPractical() != null ? m.getPractical() : 0f);
////            float full = sub.getFullMarks() != null ? sub.getFullMarks() : 100f;
////            totalObtained += obtained; totalMax += full;
////            Map<String, Object> mm = new LinkedHashMap<>();
////            mm.put("subjectName", sub.getName());
////            mm.put("theory", m.getTheory()); mm.put("practical", m.getPractical());
////            mm.put("obtained", obtained);
////            mm.put("fullMarks", full); mm.put("passMarks", sub.getPassMarks());
////            mm.put("theoryMax", sub.getTheory()); mm.put("practicalMax", sub.getPractical());
////            mm.put("grade", m.getGrade()); mm.put("gradePoint", m.getGradePoint());
////            mm.put("pass", sub.getPassMarks() == null || obtained >= sub.getPassMarks());
////            marksList.add(mm);
////        }
////        ms.put("marks", marksList);
////        ms.put("totalObtained", totalObtained);
////        ms.put("totalMax", totalMax);
////        ms.put("gpa", result.getGrade());
////        return ms;
////    }
////}
//
//
//package com.rms.service;
//
//import com.rms.entity.*;
//import com.rms.repository.*;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//
//@Service
//public class ResultService {
//
//    @Autowired private ResultRepository resultRepo;
//    @Autowired private MarksRepository marksRepo;
//    @Autowired private StudentRepository studentRepo;
//    @Autowired private FacultyDetailRepository fdRepo;
//    @Autowired private FacultyDetailSubjectRepository fdsRepo;
//
//
//    // ── Create result rows for all students in a program ──────────────────────
//
////    @Transactional
////    public void createResultsForExamination(Examination exam, FacultyDetail fd) {
////        List<Student> students = studentRepo.findByFacultyDetailId(fd.getId());
////        for (Student student : students) {
////            boolean exists = resultRepo
////                    .findByExaminationIdAndFacultyDetailIdAndStudentId(exam.getId(), fd.getId(), student.getId())
////                    .isPresent();
////            if (!exists) {
////                Result result = Result.builder()
////                        .examination(exam).facultyDetail(fd).student(student)
////                        .grade("").percentage(0.0).rank("-")
////                        .build();
////                Result saved = resultRepo.save(result);
////                for (FacultyDetailSubject fds : fd.getSubjectMappings()) {
////                    Marks marks = marksRepo.save(Marks.builder()
////                            .subject(fds.getSubject())
////                            .theory(0f).practical(0f)
////                            .grade("").gradePoint(0.0)
////                            .build());
////                    saved.getMarks().add(marks);
////                }
////                resultRepo.save(saved);
////            }
////        }
////    }
//
//
//    @Transactional
//    public void createResultsForExamination(Examination exam, FacultyDetail fd) {
//        List<Student> students = studentRepo.findByFacultyDetailId(fd.getId());
//        for (Student student : students) {
//            boolean exists = resultRepo
//                    .findByExaminationIdAndFacultyDetailIdAndStudentId(
//                            exam.getId(), fd.getId(), student.getId())
//                    .isPresent();
//            if (!exists) {
//                // Save all Marks first to get their IDs
//                List<Marks> marksList = new ArrayList<>();
//                for (FacultyDetailSubject fds : fd.getSubjectMappings()) {
//                    Marks marks = marksRepo.save(Marks.builder()
//                            .subject(fds.getSubject())
//                            .theory(0f).practical(0f)
//                            .grade("").gradePoint(0.0)
//                            .build());
//                    marksList.add(marks);
//                }
//                // Build Result with marks list already populated — saves join table in one shot
//                Result result = Result.builder()
//                        .examination(exam)
//                        .facultyDetail(fd)
//                        .student(student)
//                        .grade("").percentage(0.0).rank("-")
//                        .marks(marksList)
//                        .build();
//                resultRepo.save(result);
//            }
//        }
//    }
//
//    // ── Update marks for one student result ───────────────────────────────────
//
//    @Transactional
//    public void updateMarks(Long resultId, List<Map<String, Object>> marksData) {
//        Result result = resultRepo.findById(resultId)
//                .orElseThrow(() -> new RuntimeException("Result not found: " + resultId));
//
//        float totalObtained = 0f;
//        float totalMax = 0f;
//        boolean hasFail = false;
//
//        for (Map<String, Object> markData : marksData) {
//            // id may come as Integer from JSON — handle both
//            Object idObj = markData.get("id");
//            if (idObj == null) continue;
//            Long marksId = Long.parseLong(idObj.toString());
//
//            float theory = parseFloat(markData.get("theory"));
//            float practical = parseFloat(markData.get("practical"));
//
//            Marks marks = marksRepo.findById(marksId)
//                    .orElseThrow(() -> new RuntimeException("Marks not found: " + marksId));
//            Subject subject = marks.getSubject();
//
//            float obtained = theory + practical;
//            float full = subject.getFullMarks() != null ? subject.getFullMarks() : 100f;
//            float pass = subject.getPassMarks() != null ? subject.getPassMarks() : 40f;
//            float pct = full > 0 ? (obtained / full) * 100f : 0f;
//
//            marks.setTheory(theory);
//            marks.setPractical(practical);
//            marks.setGrade(calcGrade(pct));
//            marks.setGradePoint(Math.round(pct * 4.0 / 100.0 * 100) / 100.0);
//            marksRepo.save(marks);
//
//            totalObtained += obtained;
//            totalMax += full;
//            if (obtained < pass) hasFail = true;
//        }
//
//        double pct = totalMax > 0 ? (totalObtained / totalMax) * 100.0 : 0.0;
//        result.setPercentage(Math.round(pct * 100.0) / 100.0);
//        result.setGrade(hasFail ? "NG" : calcGrade((float) pct));
//        result.setRank(hasFail ? "FAIL" : "-");
//        resultRepo.save(result);
//
//        if (!hasFail) updateRankings(result.getExamination().getId());
//    }
//
//    // ── Get results with marks (for bulk table view) ───────────────────────────
//
//    @Transactional
//    public List<Map<String, Object>> getResultsWithMarks(Long examId, Long fdId) {
//        List<Result> results = resultRepo.findByExaminationIdAndFacultyDetailId(examId, fdId);
//        List<Map<String, Object>> data = new ArrayList<>();
//        for (Result result : results) {
//            Map<String, Object> rd = new LinkedHashMap<>();
//            Student s = result.getStudent();
//            rd.put("resultId", result.getId());
//            rd.put("studentId", s.getId());
//            rd.put("studentName", s.getName());
//            rd.put("grade", result.getGrade());
//            rd.put("percentage", result.getPercentage());
//            rd.put("rank", result.getRank());
//
//            List<Map<String, Object>> marksList = new ArrayList<>();
//            for (Marks m : result.getMarks()) {
//                Subject sub = m.getSubject();
//                Map<String, Object> mm = new LinkedHashMap<>();
//                mm.put("id", m.getId());
//                mm.put("subjectId", sub.getId());
//                mm.put("subjectName", sub.getName());
//                mm.put("theory", m.getTheory() != null ? m.getTheory() : 0f);
//                mm.put("practical", m.getPractical() != null ? m.getPractical() : 0f);
//                mm.put("grade", m.getGrade());
//                mm.put("gradePoint", m.getGradePoint());
//                mm.put("fullMarks", sub.getFullMarks() != null ? sub.getFullMarks() : 100f);
//                mm.put("passMarks", sub.getPassMarks() != null ? sub.getPassMarks() : 40f);
//                mm.put("theoryMax", sub.getTheory() != null ? sub.getTheory() : 0f);
//                mm.put("practicalMax", sub.getPractical() != null ? sub.getPractical() : 0f);
//                marksList.add(mm);
//            }
//            rd.put("marks", marksList);
//            data.add(rd);
//        }
//        return data;
//    }
//
//    // ── Single marksheet data (for print) ─────────────────────────────────────
//
//    @Transactional
//    public Map<String, Object> getMarksheetData(Long resultId) {
//        Result result = resultRepo.findById(resultId)
//                .orElseThrow(() -> new RuntimeException("Result not found: " + resultId));
//        return buildMarksheet(result);
//    }
//
//    // ── All marksheets for exam + program ─────────────────────────────────────
//
//    @Transactional
//    public List<Map<String, Object>> getAllMarksheetData(Long examId, Long fdId) {
//        return resultRepo.findByExaminationIdAndFacultyDetailId(examId, fdId)
//                .stream().map(this::buildMarksheet).toList();
//    }
//
//    // ── Private helpers ───────────────────────────────────────────────────────
//
//    private Map<String, Object> buildMarksheet(Result result) {
//        Map<String, Object> ms = new LinkedHashMap<>();
//        Student s = result.getStudent();
//        Examination exam = result.getExamination();
//        FacultyDetail fd = result.getFacultyDetail();
//
//        ms.put("resultId", result.getId());
//        ms.put("grade", result.getGrade());
//        ms.put("percentage", result.getPercentage());
//        ms.put("gpa", result.getGrade());
//        ms.put("rank", result.getRank());
//
//        // Student
//        Map<String, Object> student = new LinkedHashMap<>();
//        student.put("id", s.getId());
//        student.put("name", s.getName());
//        student.put("address", s.getAddress());
//        student.put("guardianName", s.getGuardianName());
//        student.put("contact", s.getContact());
//        ms.put("student", student);
//
//        // Examination
//        Map<String, Object> examination = new LinkedHashMap<>();
//        examination.put("id", exam.getId());
//        examination.put("name", exam.getName());
//        examination.put("year", exam.getYear());
//        ms.put("examination", examination);
//
//        // Program
//        Map<String, Object> program = new LinkedHashMap<>();
//        program.put("id", fd.getId());
//        program.put("name", fd.getName());
//        ms.put("program", program);
//
//        // Marks
//        float totalObtained = 0f, totalMax = 0f;
//        List<Map<String, Object>> marksList = new ArrayList<>();
//        for (Marks m : result.getMarks()) {
//            Subject sub = m.getSubject();
//            float theory = m.getTheory() != null ? m.getTheory() : 0f;
//            float practical = m.getPractical() != null ? m.getPractical() : 0f;
//            float obtained = theory + practical;
//            float full = sub.getFullMarks() != null ? sub.getFullMarks() : 100f;
//            float pass = sub.getPassMarks() != null ? sub.getPassMarks() : 40f;
//
//            totalObtained += obtained;
//            totalMax += full;
//
//            Map<String, Object> mm = new LinkedHashMap<>();
//            mm.put("subjectName", sub.getName());
//            mm.put("theory", theory);
//            mm.put("practical", practical);
//            mm.put("obtained", obtained);
//            mm.put("fullMarks", full);
//            mm.put("passMarks", pass);
//            mm.put("theoryMax", sub.getTheory() != null ? sub.getTheory() : 0f);
//            mm.put("practicalMax", sub.getPractical() != null ? sub.getPractical() : 0f);
//            mm.put("grade", m.getGrade());
//            mm.put("gradePoint", m.getGradePoint());
//            mm.put("pass", obtained >= pass);
//            marksList.add(mm);
//        }
//        ms.put("marks", marksList);
//        ms.put("totalObtained", totalObtained);
//        ms.put("totalMax", totalMax);
//        return ms;
//    }
//
//    private void updateRankings(Long examId) {
//        List<Result> results = resultRepo.findRankableResults(examId);
//        results.sort((a, b) -> Double.compare(
//                b.getPercentage() != null ? b.getPercentage() : 0.0,
//                a.getPercentage() != null ? a.getPercentage() : 0.0));
//        int rank = 1;
//        Double prevPct = null;
//        int prevRank = 1;
//        for (Result r : results) {
//            if (prevPct != null && Objects.equals(r.getPercentage(), prevPct)) {
//                r.setRank(String.valueOf(prevRank));
//            } else {
//                prevRank = rank;
//                r.setRank(String.valueOf(rank));
//                prevPct = r.getPercentage();
//            }
//            resultRepo.save(r);
//            rank++;
//        }
//    }
//
//    private String calcGrade(float pct) {
//        if (pct >= 90) return "A+";
//        if (pct >= 80) return "A";
//        if (pct >= 70) return "B+";
//        if (pct >= 60) return "B";
//        if (pct >= 50) return "C";
//        if (pct >= 40) return "D";
//        return "F";
//    }
//
//    private float parseFloat(Object val) {
//        if (val == null) return 0f;
//        try { return Float.parseFloat(val.toString()); }
//        catch (Exception e) { return 0f; }
//    }
//}



package com.rms.service;

import com.rms.entity.*;
import com.rms.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ResultService {

    @Autowired private ResultRepository resultRepo;
    @Autowired private MarksRepository marksRepo;
    @Autowired private StudentRepository studentRepo;
    @Autowired private FacultyDetailRepository fdRepo;
    @Autowired private FacultyDetailSubjectRepository fdsRepo;

    @Transactional
    public void createResultsForExamination(Examination exam, FacultyDetail fd) {
        // Get ALL students in this program
        List<Student> students = studentRepo.findByFacultyDetailId(fd.getId());
        System.out.println("[ResultService] Found " + students.size() +
                " students for faculty_detail_id=" + fd.getId());

        // Get ALL subjects for this program
        List<FacultyDetailSubject> subjectMappings = fdsRepo.findByFacultyDetailId(fd.getId());
        System.out.println("[ResultService] Found " + subjectMappings.size() +
                " subjects for faculty_detail_id=" + fd.getId());

        for (Student student : students) {
            // Check if result already exists
            Optional<Result> existing = resultRepo
                    .findByExaminationIdAndFacultyDetailIdAndStudentId(
                            exam.getId(), fd.getId(), student.getId());
            if (existing.isPresent()) {
                System.out.println("[ResultService] Result already exists for student=" + student.getName());
                continue;
            }

            // Create result row
            Result result = new Result();
            result.setExamination(exam);
            result.setFacultyDetail(fd);
            result.setStudent(student);
            result.setGrade("F");
            result.setPercentage(0.0);
            result.setRank("-");
            result.setMarks(new ArrayList<>());
            Result saved = resultRepo.save(result);

            // Create one Marks row per subject and link to result
            for (FacultyDetailSubject fds : subjectMappings) {
                Subject subject = fds.getSubject();
                Marks marks = new Marks();
                marks.setSubject(subject);
                marks.setTheory(0f);
                marks.setPractical(0f);
                marks.setGrade("F");
                marks.setGradePoint(0.0);
                Marks savedMarks = marksRepo.save(marks);
                saved.getMarks().add(savedMarks);
            }
            resultRepo.save(saved);
            System.out.println("[ResultService] Created result with " +
                    subjectMappings.size() + " marks for student=" + student.getName());
        }
    }

    @Transactional
    public void updateMarks(Long resultId, List<Map<String, Object>> marksData) {
        Result result = resultRepo.findById(resultId)
                .orElseThrow(() -> new RuntimeException("Result not found: " + resultId));

        float totalObtained = 0f;
        float totalMax = 0f;
        boolean hasFail = false;

        for (Map<String, Object> markData : marksData) {
            Object idObj = markData.get("id");
            if (idObj == null) continue;
            Long marksId = Long.parseLong(idObj.toString());

            float theory = parseFloat(markData.get("theory"));
            float practical = parseFloat(markData.get("practical"));

            Marks marks = marksRepo.findById(marksId)
                    .orElseThrow(() -> new RuntimeException("Marks not found: " + marksId));
            Subject subject = marks.getSubject();

            float obtained = theory + practical;
            float full = subject.getFullMarks() != null ? subject.getFullMarks() : 100f;
            float pass = subject.getPassMarks() != null ? subject.getPassMarks() : 40f;
            float pct = full > 0 ? (obtained / full) * 100f : 0f;

            marks.setTheory(theory);
            marks.setPractical(practical);
            marks.setGrade(calcGrade(pct));
            marks.setGradePoint(Math.round(pct * 4.0 / 100.0 * 100.0) / 100.0);
            marksRepo.save(marks);

            totalObtained += obtained;
            totalMax += full;
            if (obtained < pass) hasFail = true;
        }

        double pct = totalMax > 0 ? (totalObtained / totalMax) * 100.0 : 0.0;
        result.setPercentage(Math.round(pct * 100.0) / 100.0);
        result.setGrade(hasFail ? "NG" : calcGrade((float) pct));
        result.setRank("-");
        resultRepo.save(result);

        if (!hasFail) updateRankings(result.getExamination().getId());
    }

    @Transactional
    public List<Map<String, Object>> getResultsWithMarks(Long examId, Long fdId) {
        List<Result> results = resultRepo.findByExaminationIdAndFacultyDetailId(examId, fdId);
        System.out.println("[ResultService] getResultsWithMarks: found " + results.size() + " results");
        List<Map<String, Object>> data = new ArrayList<>();
        for (Result result : results) {
            Map<String, Object> rd = new LinkedHashMap<>();
            Student s = result.getStudent();
            rd.put("resultId", result.getId());
            rd.put("studentId", s.getId());
            rd.put("studentName", s.getName());
            rd.put("grade", result.getGrade());
            rd.put("percentage", result.getPercentage());
            rd.put("rank", result.getRank());

            List<Map<String, Object>> marksList = new ArrayList<>();
            for (Marks m : result.getMarks()) {
                Subject sub = m.getSubject();
                Map<String, Object> mm = new LinkedHashMap<>();
                mm.put("id", m.getId());
                mm.put("subjectId", sub.getId());
                mm.put("subjectName", sub.getName());
                mm.put("theory", m.getTheory() != null ? m.getTheory() : 0f);
                mm.put("practical", m.getPractical() != null ? m.getPractical() : 0f);
                mm.put("grade", m.getGrade());
                mm.put("gradePoint", m.getGradePoint());
                mm.put("fullMarks", sub.getFullMarks() != null ? sub.getFullMarks() : 100f);
                mm.put("passMarks", sub.getPassMarks() != null ? sub.getPassMarks() : 40f);
                mm.put("theoryMax", sub.getTheory() != null ? sub.getTheory() : 0f);
                mm.put("practicalMax", sub.getPractical() != null ? sub.getPractical() : 0f);
                marksList.add(mm);
            }
            rd.put("marks", marksList);
            data.add(rd);
        }
        return data;
    }

    @Transactional
    public Map<String, Object> getMarksheetData(Long resultId) {
        Result result = resultRepo.findById(resultId)
                .orElseThrow(() -> new RuntimeException("Result not found: " + resultId));
        return buildMarksheet(result);
    }

    @Transactional
    public List<Map<String, Object>> getAllMarksheetData(Long examId, Long fdId) {
        return resultRepo.findByExaminationIdAndFacultyDetailId(examId, fdId)
                .stream().map(this::buildMarksheet).toList();
    }

    private Map<String, Object> buildMarksheet(Result result) {
        Map<String, Object> ms = new LinkedHashMap<>();
        Student s = result.getStudent();
        Examination exam = result.getExamination();
        FacultyDetail fd = result.getFacultyDetail();

        ms.put("resultId", result.getId());
        ms.put("grade", result.getGrade());
        ms.put("percentage", result.getPercentage());
        ms.put("gpa", result.getGrade());
        ms.put("rank", result.getRank());

        Map<String, Object> student = new LinkedHashMap<>();
        student.put("id", s.getId()); student.put("name", s.getName());
        student.put("address", s.getAddress()); student.put("guardianName", s.getGuardianName());
        ms.put("student", student);

        Map<String, Object> examination = new LinkedHashMap<>();
        examination.put("id", exam.getId()); examination.put("name", exam.getName());
        examination.put("year", exam.getYear());
        ms.put("examination", examination);

        Map<String, Object> program = new LinkedHashMap<>();
        program.put("id", fd.getId()); program.put("name", fd.getName());
        ms.put("program", program);

        float totalObtained = 0f, totalMax = 0f;
        List<Map<String, Object>> marksList = new ArrayList<>();
        for (Marks m : result.getMarks()) {
            Subject sub = m.getSubject();
            float theory = m.getTheory() != null ? m.getTheory() : 0f;
            float practical = m.getPractical() != null ? m.getPractical() : 0f;
            float obtained = theory + practical;
            float full = sub.getFullMarks() != null ? sub.getFullMarks() : 100f;
            float pass = sub.getPassMarks() != null ? sub.getPassMarks() : 40f;
            totalObtained += obtained; totalMax += full;
            Map<String, Object> mm = new LinkedHashMap<>();
            mm.put("subjectName", sub.getName());
            mm.put("theory", theory); mm.put("practical", practical);
            mm.put("obtained", obtained); mm.put("fullMarks", full);
            mm.put("passMarks", pass);
            mm.put("theoryMax", sub.getTheory() != null ? sub.getTheory() : 0f);
            mm.put("practicalMax", sub.getPractical() != null ? sub.getPractical() : 0f);
            mm.put("grade", m.getGrade()); mm.put("gradePoint", m.getGradePoint());
            mm.put("pass", obtained >= pass);
            marksList.add(mm);
        }
        ms.put("marks", marksList);
        ms.put("totalObtained", totalObtained);
        ms.put("totalMax", totalMax);
        return ms;
    }

    private void updateRankings(Long examId) {
        List<Result> results = resultRepo.findRankableResults(examId);
        results.sort((a, b) -> Double.compare(
                b.getPercentage() != null ? b.getPercentage() : 0.0,
                a.getPercentage() != null ? a.getPercentage() : 0.0));
        int rank = 1; Double prevPct = null; int prevRank = 1;
        for (Result r : results) {
            if (prevPct != null && Objects.equals(r.getPercentage(), prevPct)) {
                r.setRank(String.valueOf(prevRank));
            } else {
                prevRank = rank; r.setRank(String.valueOf(rank)); prevPct = r.getPercentage();
            }
            resultRepo.save(r); rank++;
        }
    }

    private String calcGrade(float pct) {
        if (pct >= 90) return "A+";
        if (pct >= 80) return "A";
        if (pct >= 70) return "B+";
        if (pct >= 60) return "B";
        if (pct >= 50) return "C";
        if (pct >= 40) return "D";
        return "F";
    }

    private float parseFloat(Object val) {
        if (val == null) return 0f;
        try { return Float.parseFloat(val.toString()); } catch (Exception e) { return 0f; }
    }
}