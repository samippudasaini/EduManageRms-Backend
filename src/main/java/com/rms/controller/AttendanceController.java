package com.rms.controller;

import com.rms.entity.*;
import com.rms.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired private AttendanceRepository repo;
    @Autowired private StudentRepository studentRepo;
    @Autowired private GradeSectionMappingRepository gsRepo;


    @GetMapping("/sections")
    public List<Map<String, Object>> getSections() {
        return gsRepo.findAll().stream().map(gs -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", gs.getId());
            m.put("gradeName",   gs.getGrade()   != null ? gs.getGrade().getName()   : "");
            m.put("sectionName", gs.getSection() != null ? gs.getSection().getName() : "");
            m.put("studentCount", studentRepo.findByGradeSectionId(gs.getId()).size());
            return m;
        }).toList();
    }

    // Daily Grid: get full month grid


    @GetMapping("/monthly/{gsId}")
    @Transactional
    public ResponseEntity<?> getMonthlyGrid(
            @PathVariable Long gsId,
            @RequestParam int year,
            @RequestParam int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end   = start.withDayOfMonth(start.lengthOfMonth());
        int daysInMonth = start.lengthOfMonth();

        List<Student> students = studentRepo.findByGradeSectionId(gsId);

        // Load all attendance in one query
        List<Attendance> records = repo.findByGradeSectionAndRange(gsId, start, end);

        // Index: studentId → {dayOfMonth → status}
        Map<Long, Map<Integer, String>> index = new HashMap<>();
        for (Attendance a : records) {
            Long sid = a.getStudent().getId();
            index.computeIfAbsent(sid, k -> new HashMap<>())
                    .put(a.getDate().getDayOfMonth(), a.getAttendanceStatus());
        }

        // ── TOTAL WORKING DAYS = unique dates that have ANY attendance record ──
        // All students share the same total working days for the class.
        // A "working day" is any date where at least one student was marked.
        Set<Integer> workingDays = new HashSet<>();
        for (Attendance a : records) {
            if (a.getAttendanceStatus() != null && !a.getAttendanceStatus().isBlank()) {
                workingDays.add(a.getDate().getDayOfMonth());
            }
        }
        int totalWorkingDays = workingDays.size(); // same for ALL students

        // Per-student rows
        List<Map<String, Object>> studentRows = new ArrayList<>();
        for (Student s : students) {
            Map<Integer, String> dayMap = index.getOrDefault(s.getId(), new HashMap<>());

            // Present = P + L (late counts as present)
            long present = dayMap.values().stream().filter(v -> "P".equals(v) || "L".equals(v)).count();
            long absent  = dayMap.values().stream().filter("A"::equals).count();
            long late    = dayMap.values().stream().filter("L"::equals).count();

            // Percentage uses CLASS total working days, not individual student's marked count
            double pct = totalWorkingDays > 0
                    ? Math.round((present * 100.0 / totalWorkingDays) * 10) / 10.0 : 0.0;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("studentId",        s.getId());
            row.put("studentName",      s.getName());
            row.put("days",             dayMap);
            row.put("present",          present);
            row.put("absent",           absent);
            row.put("late",             late);
            row.put("totalMarked", present + absent);
            row.put("totalWorkingDays", totalWorkingDays); // same for all students
            row.put("pct",              pct);
            studentRows.add(row);
        }

        // Daily P/A/L summary counts per day
        Map<Integer, Map<String, Long>> dailySummary = new LinkedHashMap<>();
        for (int d = 1; d <= daysInMonth; d++) {
            final int day = d;
            long p = 0, a = 0, l = 0;
            for (Student s : students) {
                String st = index.getOrDefault(s.getId(), Map.of()).getOrDefault(day, "");
                if ("P".equals(st)) p++;
                else if ("A".equals(st)) a++;
                else if ("L".equals(st)) l++;
            }
            Map<String, Long> ds = new LinkedHashMap<>();
            ds.put("P", p); ds.put("A", a); ds.put("L", l);
            dailySummary.put(day, ds);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("year", year); resp.put("month", month);
        resp.put("monthName", YearMonth.of(year, month)
                .format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        resp.put("daysInMonth",      daysInMonth);
        resp.put("totalWorkingDays", totalWorkingDays); // class-level, same for all
        resp.put("students",         studentRows);
        resp.put("dailySummary",     dailySummary);
        resp.put("gradeSectionId",   gsId);

        GradeSectionMapping gs = gsRepo.findById(gsId).orElse(null);
        if (gs != null) {
            resp.put("gradeName",   gs.getGrade()   != null ? gs.getGrade().getName()   : "");
            resp.put("sectionName", gs.getSection() != null ? gs.getSection().getName() : "");
        }
        return ResponseEntity.ok(resp);
    }
    // Monthly Summary: per student, all months with totals

    @GetMapping("/summary/{gsId}")
    @Transactional
    public ResponseEntity<?> getMonthlySummary(
            @PathVariable Long gsId,
            @RequestParam int year) {

        List<Student> students = studentRepo.findByGradeSectionId(gsId);
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end   = LocalDate.of(year, 12, 31);

        List<Attendance> all = repo.findByGradeSectionAndRange(gsId, start, end);

        // ── WORKING DAYS PER MONTH = unique dates with any record in that month ──
        // Key: month → Set of days that had attendance taken (class-level)
        Map<Integer, Set<Integer>> workingDaysByMonth = new HashMap<>();
        for (Attendance a : all) {
            if (a.getAttendanceStatus() == null || a.getAttendanceStatus().isBlank()) continue;
            int mon = a.getDate().getMonthValue();
            int day = a.getDate().getDayOfMonth();
            workingDaysByMonth.computeIfAbsent(mon, k -> new HashSet<>()).add(day);
        }
        // Convert to count per month — same for ALL students in this class
        Map<Integer, Integer> totalWorkingDaysByMonth = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> e : workingDaysByMonth.entrySet())
            totalWorkingDaysByMonth.put(e.getKey(), e.getValue().size());

        // Index: studentId → month → [present, absent, late]
        Map<Long, Map<Integer, int[]>> idx = new HashMap<>();
        for (Attendance a : all) {
            Long sid   = a.getStudent().getId();
            int  mon   = a.getDate().getMonthValue();
            String st  = a.getAttendanceStatus() != null ? a.getAttendanceStatus() : "";
            int[] counts = idx.computeIfAbsent(sid, k -> new HashMap<>())
                    .computeIfAbsent(mon, k -> new int[3]);
            if ("P".equals(st)) counts[0]++;
            else if ("A".equals(st)) counts[1]++;
            else if ("L".equals(st)) counts[2]++;
        }

        Set<Integer> monthsWithData = new HashSet<>();
        for (Attendance a : all) monthsWithData.add(a.getDate().getMonthValue());
        List<Integer> months = new ArrayList<>(monthsWithData);
        Collections.sort(months);


        List<Map<String, Object>> studentSummaries = new ArrayList<>();
        for (Student s : students) {
            Map<Integer, int[]> sData = idx.getOrDefault(s.getId(), new HashMap<>());

            int totalPresent = 0, totalAbsent = 0, grandTotal = 0;
            Map<Integer, Map<String, Object>> monthlyBreakdown = new LinkedHashMap<>();

            for (int mon : months) {
                int[] c = sData.getOrDefault(mon, new int[3]);
                int p = c[0] + c[2]; // present + late
                int a = c[1];
                int total = p + a;   // THIS student's own marked days that month

                double pct = total > 0
                        ? Math.round((p * 100.0 / total) * 10) / 10.0 : 0.0;

                Map<String, Object> mb = new LinkedHashMap<>();
                mb.put("present", p);
                mb.put("absent",  a);
                mb.put("total",   total); // per-student, not class-wide
                mb.put("pct",     pct);
                monthlyBreakdown.put(mon, mb);

                totalPresent += p;
                totalAbsent  += a;
                grandTotal   += total;
            }

            double cumPct = grandTotal > 0
                    ? Math.round((totalPresent * 100.0 / grandTotal) * 10) / 10.0 : 0.0;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("studentId",   s.getId());
            row.put("studentName", s.getName());
            row.put("months",      monthlyBreakdown);
            row.put("totalPresent", totalPresent);
            row.put("totalAbsent",  totalAbsent);
            row.put("grandTotal",   grandTotal); // matches what the frontend already reads
            row.put("cumPct",       cumPct);
            studentSummaries.add(row);
        }

        Map<String, String> monthNames = new LinkedHashMap<>();
        for (int m : months)
            monthNames.put(String.valueOf(m), YearMonth.of(year, m)
                    .format(DateTimeFormatter.ofPattern("MMM")));

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("year",                  year);
        resp.put("months",                months);
        resp.put("monthNames",            monthNames);
        resp.put("totalWorkingDaysByMonth", totalWorkingDaysByMonth); // class-level per month
        resp.put("students",              studentSummaries);
        return ResponseEntity.ok(resp);
    }

    // Auto-save single cell (PATCH)

    @PatchMapping("/mark")
    @Transactional
    public ResponseEntity<?> mark(@RequestBody Map<String, Object> body) {
        Long   studentId = Long.parseLong(body.get("studentId").toString());
        String dateStr   = (String) body.get("date");
        String status    = (String) body.get("status"); // P, A, L, or null/""

        LocalDate date = LocalDate.parse(dateStr);
        Optional<Attendance> existing = repo.findByStudentIdAndDate(studentId, date);

        if (status == null || status.isBlank()) {
            // null = remove the record (not marked)
            existing.ifPresent(repo::delete);
            return ResponseEntity.ok(Map.of("studentId", studentId, "date", dateStr, "status", ""));
        }

        if (!List.of("P", "A", "L").contains(status))
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid status"));

        Attendance att = existing.orElseGet(() ->
                studentRepo.findById(studentId).map(s ->
                        Attendance.builder().student(s).date(date).build()
                ).orElseThrow(() -> new RuntimeException("Student not found"))
        );

        att.setAttendanceStatus(status);
        att.setStatus("P".equals(status) || "L".equals(status));
        repo.save(att);

        return ResponseEntity.ok(Map.of("studentId", studentId, "date", dateStr, "status", status));
    }

    // ── Bulk mark all for a date
    @PostMapping("/mark-all")
    @Transactional
    public ResponseEntity<?> markAll(@RequestBody Map<String, Object> body) {
        Long   gsId   = Long.parseLong(body.get("gsId").toString());
        String dateStr = (String) body.get("date");
        String status  = (String) body.get("status");
        LocalDate date = LocalDate.parse(dateStr);

        for (Student s : studentRepo.findByGradeSectionId(gsId)) {
            Optional<Attendance> ex = repo.findByStudentIdAndDate(s.getId(), date);
            Attendance att = ex.orElse(Attendance.builder().student(s).date(date).build());
            att.setAttendanceStatus(status);
            att.setStatus("P".equals(status) || "L".equals(status));
            repo.save(att);
        }
        return ResponseEntity.ok(Map.of("message", "Done"));
    }

    //  Legacy endpoints (kept for backward compat)

    @GetMapping("/conduct/{gradeSectionId}")
    @Transactional
    public ResponseEntity<?> conductAttendance(@PathVariable Long gradeSectionId) {
        LocalDate today = LocalDate.now();
        List<Attendance> existing = repo.findByGradeSectionIdAndDate(gradeSectionId, today);
        if (existing.isEmpty()) {
            for (Student s : studentRepo.findByGradeSectionId(gradeSectionId))
                repo.save(Attendance.builder().student(s).date(today)
                        .attendanceStatus("P").status(true).build());
            existing = repo.findByGradeSectionIdAndDate(gradeSectionId, today);
        }
        return ResponseEntity.ok(toList(existing));
    }

    @PostMapping("/process/{gradeSectionId}")
    public ResponseEntity<?> process(@PathVariable Long gradeSectionId,
                                     @RequestBody List<Map<String, Object>> list) {
        for (Map<String, Object> item : list) {
            Long id = Long.parseLong(item.get("id").toString());
            boolean st = Boolean.parseBoolean(item.get("status").toString());
            repo.findById(id).ifPresent(a -> {
                a.setStatus(st);
                a.setAttendanceStatus(st ? "P" : "A");
                repo.save(a);
            });
        }
        return ResponseEntity.ok(Map.of("message", "Saved"));
    }

    private List<Map<String, Object>> toList(List<Attendance> list) {
        return list.stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("date", a.getDate() != null ? a.getDate().toString() : "");
            m.put("status", a.getStatus() != null ? a.getStatus() : true);
            m.put("attendanceStatus", a.getAttendanceStatus() != null ? a.getAttendanceStatus() : "P");
            if (a.getStudent() != null) {
                m.put("studentId",   a.getStudent().getId());
                m.put("studentName", a.getStudent().getName());
            }
            return m;
        }).toList();
    }
}
