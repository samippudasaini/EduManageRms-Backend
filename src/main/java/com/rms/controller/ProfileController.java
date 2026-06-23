//package com.rms.controller;
//import com.rms.entity.CollegeProfile;
//import com.rms.repository.CollegeProfileRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import java.io.*;
//import java.nio.file.*;
//import java.util.Map;
//
//@RestController @RequestMapping("/api/profile")
//public class ProfileController {
//    @Autowired private CollegeProfileRepository repo;
//    @Value("${app.upload.dir}") private String uploadDir;
//
//    @GetMapping public ResponseEntity<?> get() {
//        return repo.findById(1L).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
//    }
//    @PostMapping public ResponseEntity<?> create(@RequestBody CollegeProfile p) { return ResponseEntity.ok(repo.save(p)); }
//    @PutMapping("/{id}") public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CollegeProfile p) {
//        p.setId(id); return ResponseEntity.ok(repo.save(p));
//    }
//    @PostMapping("/upload-logo") public ResponseEntity<?> uploadLogo(@RequestParam("file") MultipartFile file) throws IOException {
//        String dir = uploadDir + "profilePicture/";
//        Files.createDirectories(Paths.get(dir));
//        String path = dir + file.getOriginalFilename();
//        file.transferTo(new File(path));
//        return ResponseEntity.ok(Map.of("path", path));
//    }
//}



package com.rms.controller;

import com.rms.entity.CollegeProfile;
import com.rms.repository.CollegeProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired private CollegeProfileRepository repo;

    @GetMapping
    public ResponseEntity<?> get() {
//        return repo.findById(1L)
//                .map(p -> ResponseEntity.ok(toMap(p)))
//                .orElse(ResponseEntity.notFound().build());
        return repo.findAll().stream().findFirst()
                .map(p -> ResponseEntity.ok(toMap(p)))
                .orElse(ResponseEntity.ok(new java.util.LinkedHashMap<>()));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        CollegeProfile p = fromMap(new CollegeProfile(), body);
        p.setId(null);
        return ResponseEntity.ok(toMap(repo.save(p)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        CollegeProfile p = repo.findById(id).orElse(new CollegeProfile());
        p.setId(id);
        fromMap(p, body);
        return ResponseEntity.ok(toMap(repo.save(p)));
    }

    private Map<String, Object> toMap(CollegeProfile p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("name", p.getName());
        m.put("address", p.getAddress());
        m.put("email", p.getEmail());
        m.put("phone", p.getPhone());
        m.put("principalName", p.getPrincipalName());
        m.put("slogan", p.getSlogan());
        m.put("importantInfo", p.getImportantInfo());
        // logoPath stores the base64 string (saved from frontend)
        m.put("logoUrl", p.getLogoPath());
        return m;
    }

    private CollegeProfile fromMap(CollegeProfile p, Map<String, Object> body) {
        if (body.containsKey("name")) p.setName((String) body.get("name"));
        if (body.containsKey("address")) p.setAddress((String) body.get("address"));
        if (body.containsKey("email")) p.setEmail((String) body.get("email"));
        if (body.containsKey("phone")) p.setPhone((String) body.get("phone"));
        if (body.containsKey("principalName")) p.setPrincipalName((String) body.get("principalName"));
        if (body.containsKey("slogan")) p.setSlogan((String) body.get("slogan"));
        if (body.containsKey("importantInfo")) p.setImportantInfo((String) body.get("importantInfo"));
        // Frontend sends logoUrl (base64), store in logoPath column
        if (body.containsKey("logoUrl")) p.setLogoPath((String) body.get("logoUrl"));
        return p;
    }
}