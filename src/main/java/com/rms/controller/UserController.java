package com.rms.controller;

import com.rms.entity.User;
import com.rms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/api/users")
public class UserController {
    @Autowired private UserRepository repo;
    @Autowired private PasswordEncoder encoder;

    @GetMapping public List<User> getAll() { return repo.findAll(); }
    @GetMapping("/{id}") public ResponseEntity<?> getById(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        var u = User.builder().name(body.get("name"))
                .password(encoder.encode(body.get("password"))).type(body.get("type")).build();
        return ResponseEntity.ok(repo.save(u));
    }
    @PutMapping("/{id}") public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return repo.findById(id).map(u -> {
            if (body.containsKey("password")) u.setPassword(encoder.encode(body.get("password")));
            if (body.containsKey("type")) u.setType(body.get("type"));
            return ResponseEntity.ok(repo.save(u));
        }).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}") public ResponseEntity<?> delete(@PathVariable Long id) {
        repo.deleteById(id); return ResponseEntity.ok().build();
    }
}
