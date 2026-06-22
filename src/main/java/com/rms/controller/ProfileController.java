package com.rms.controller;
import com.rms.entity.CollegeProfile;
import com.rms.repository.CollegeProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.*;
import java.util.Map;

@RestController @RequestMapping("/api/profile")
public class ProfileController {
    @Autowired private CollegeProfileRepository repo;
    @Value("${app.upload.dir}") private String uploadDir;

    @GetMapping public ResponseEntity<?> get() {
        return repo.findById(1L).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping public ResponseEntity<?> create(@RequestBody CollegeProfile p) { return ResponseEntity.ok(repo.save(p)); }
    @PutMapping("/{id}") public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CollegeProfile p) {
        p.setId(id); return ResponseEntity.ok(repo.save(p));
    }
    @PostMapping("/upload-logo") public ResponseEntity<?> uploadLogo(@RequestParam("file") MultipartFile file) throws IOException {
        String dir = uploadDir + "profilePicture/";
        Files.createDirectories(Paths.get(dir));
        String path = dir + file.getOriginalFilename();
        file.transferTo(new File(path));
        return ResponseEntity.ok(Map.of("path", path));
    }
}
