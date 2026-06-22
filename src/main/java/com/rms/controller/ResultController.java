package com.rms.controller;
import com.rms.service.ResultService;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@RestController @RequestMapping("/api/results")
public class ResultController {
    @Autowired private ResultService resultService;

    @PostMapping("/update") public ResponseEntity<?> updateResult(@RequestBody Map<String,Object> body) {
        Long resultId = Long.parseLong(body.get("resultId").toString());
        List<Map<String,Object>> marks = (List<Map<String,Object>>) body.get("marks");
        resultService.updateMarks(resultId, marks);
        return ResponseEntity.ok(Map.of("message", "Result updated"));
    }

    /**
     * Bulk update marks for all students in an exam+program via JSON.
     * Body: { "examId": 1, "fdId": 2, "results": [ { "resultId": 10, "marks": [...] } ] }
     */
    @PostMapping("/bulk-update")
    @Transactional
    public ResponseEntity<?> bulkUpdate(@RequestBody Map<String, Object> body) {
        List<Map<String, Object>> results = (List<Map<String, Object>>) body.get("results");
        for (Map<String, Object> r : results) {
            Long resultId = Long.parseLong(r.get("resultId").toString());
            List<Map<String, Object>> marks = (List<Map<String, Object>>) r.get("marks");
            resultService.updateMarks(resultId, marks);
        }
        return ResponseEntity.ok(Map.of("message", "Bulk update complete", "count", results.size()));
    }

    /**
     * Excel bulk upload.
     * Expected sheet columns: StudentName | Subject1_Theory | Subject1_Practical | Subject2_Theory | ...
     * First row = headers matching subject names exactly.
     * The examId+fdId are used to resolve which result/marks rows to update.
     */
    @PostMapping("/bulk-upload-excel")
    @Transactional
    public ResponseEntity<?> bulkUploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("examId") Long examId,
            @RequestParam("fdId") Long fdId) {
        try (InputStream is = file.getInputStream();
             Workbook wb = new XSSFWorkbook(is)) {

            Sheet sheet = wb.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return ResponseEntity.badRequest().body(Map.of("message", "Empty file"));

            // Build column index map from header: "SubjectName_Theory" or "SubjectName_Practical"
            Map<Integer, String[]> colMap = new LinkedHashMap<>();
            for (int c = 1; c < headerRow.getLastCellNum(); c++) {
                Cell cell = headerRow.getCell(c);
                if (cell == null) continue;
                String header = cell.getStringCellValue().trim();
                // Expected format: "MATH_Theory", "MATH_Practical"
                int lastUs = header.lastIndexOf('_');
                if (lastUs > 0) {
                    colMap.put(c, new String[]{header.substring(0, lastUs), header.substring(lastUs + 1)});
                }
            }

            // Load current results for this exam+fd
            List<Map<String, Object>> currentResults = resultService.getResultsWithMarks(examId, fdId);
            // Index by studentName for matching
            Map<String, Map<String, Object>> byName = new LinkedHashMap<>();
            for (Map<String, Object> r : currentResults)
                byName.put(r.get("studentName").toString().trim().toUpperCase(), r);

            int updated = 0;
            for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) continue;
                Cell nameCell = row.getCell(0);
                if (nameCell == null) continue;
                String studentName = nameCell.getStringCellValue().trim().toUpperCase();
                Map<String, Object> resultData = byName.get(studentName);
                if (resultData == null) continue;

                Long resultId = Long.parseLong(resultData.get("resultId").toString());
                List<Map<String, Object>> marksList = (List<Map<String, Object>>) resultData.get("marks");

                // Build marks map by subjectName for quick lookup
                Map<String, Map<String, Object>> marksBySubject = new LinkedHashMap<>();
                for (Map<String, Object> m : marksList)
                    marksBySubject.put(m.get("subjectName").toString().trim().toUpperCase(), m);

                // Apply values from Excel row
                for (Map.Entry<Integer, String[]> entry : colMap.entrySet()) {
                    int col = entry.getKey();
                    String subjectName = entry.getValue()[0].trim().toUpperCase();
                    String type = entry.getValue()[1].trim().toUpperCase(); // THEORY or PRACTICAL
                    Cell valueCell = row.getCell(col);
                    if (valueCell == null) continue;
                    float value = (float) (valueCell.getCellType() == CellType.NUMERIC
                            ? valueCell.getNumericCellValue() : 0);
                    Map<String, Object> mark = marksBySubject.get(subjectName);
                    if (mark == null) continue;
                    if (type.equals("THEORY")) mark.put("theory", value);
                    else if (type.equals("PRACTICAL")) mark.put("practical", value);
                }

                resultService.updateMarks(resultId, marksList);
                updated++;
            }
            return ResponseEntity.ok(Map.of("message", "Excel upload complete", "studentsUpdated", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed: " + e.getMessage()));
        }
    }

    /** Download an Excel template for a given exam+program to fill marks */
    @GetMapping("/excel-template")
    public void downloadTemplate(
            @RequestParam Long examId,
            @RequestParam Long fdId,
            jakarta.servlet.http.HttpServletResponse response) throws Exception {

        List<Map<String, Object>> results = resultService.getResultsWithMarks(examId, fdId);
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Marks");

            // Build headers
            Row header = sheet.createRow(0);
            CellStyle bold = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            bold.setFont(font);

            header.createCell(0).setCellValue("StudentName");
            header.getCell(0).setCellStyle(bold);
            int col = 1;

            List<String> subjectHeaders = new ArrayList<>();
            if (!results.isEmpty()) {
                List<Map<String, Object>> marks = (List<Map<String, Object>>) results.get(0).get("marks");
                for (Map<String, Object> m : marks) {
                    String sn = m.get("subjectName").toString();
                    float theoryMax = m.get("theoryMax") != null ? Float.parseFloat(m.get("theoryMax").toString()) : 0f;
                    float practicalMax = m.get("practicalMax") != null ? Float.parseFloat(m.get("practicalMax").toString()) : 0f;
                    if (theoryMax > 0) {
                        subjectHeaders.add(sn + "_Theory");
                        Cell c = header.createCell(col++);
                        c.setCellValue(sn + "_Theory"); c.setCellStyle(bold);
                    }
                    if (practicalMax > 0) {
                        subjectHeaders.add(sn + "_Practical");
                        Cell c = header.createCell(col++);
                        c.setCellValue(sn + "_Practical"); c.setCellStyle(bold);
                    }
                }
            }

            // Fill student rows
            int rowIdx = 1;
            for (Map<String, Object> r : results) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.get("studentName").toString());
                List<Map<String, Object>> marks = (List<Map<String, Object>>) r.get("marks");
                int c2 = 1;
                for (Map<String, Object> m : marks) {
                    float theoryMax = m.get("theoryMax") != null ? Float.parseFloat(m.get("theoryMax").toString()) : 0f;
                    float practicalMax = m.get("practicalMax") != null ? Float.parseFloat(m.get("practicalMax").toString()) : 0f;
                    if (theoryMax > 0) row.createCell(c2++).setCellValue(Float.parseFloat(m.get("theory").toString()));
                    if (practicalMax > 0) row.createCell(c2++).setCellValue(Float.parseFloat(m.get("practical").toString()));
                }
            }
            for (int i = 0; i < col; i++) sheet.autoSizeColumn(i);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=marks_template.xlsx");
            wb.write(response.getOutputStream());
        }
    }

    /** Get marksheet data for a single student result (used for print/download) */
    @GetMapping("/marksheet/{resultId}")
    public ResponseEntity<?> getMarksheet(@PathVariable Long resultId) {
        return ResponseEntity.ok(resultService.getMarksheetData(resultId));
    }

    /** Get all marksheets for an exam+program (for bulk print/download) */
    @GetMapping("/marksheets")
    public ResponseEntity<?> getAllMarksheets(
            @RequestParam Long examId,
            @RequestParam Long fdId) {
        return ResponseEntity.ok(resultService.getAllMarksheetData(examId, fdId));
    }

}
