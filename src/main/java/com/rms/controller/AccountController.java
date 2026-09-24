package com.rms.controller;

import com.rms.dto.ChangePasswordRequest;
import com.rms.dto.SecurityQuestionRequest;
import com.rms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder encoder;

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req, Authentication auth) {
        var user = userRepository.findByName(auth.getName()).orElseThrow();
        if (!encoder.matches(req.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.status(400).body("Current password is incorrect");
        }
        if (req.getNewPassword() == null || req.getNewPassword().isBlank()) {
            return ResponseEntity.status(400).body("New password cannot be empty");
        }
        user.setPassword(encoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/security-question")
    public ResponseEntity<?> setSecurityQuestion(@RequestBody SecurityQuestionRequest req, Authentication auth) {
        var user = userRepository.findByName(auth.getName()).orElseThrow();
        if (req.getQuestion() == null || req.getQuestion().isBlank() || req.getAnswer() == null || req.getAnswer().isBlank()) {
            return ResponseEntity.status(400).body("Question and answer are required");
        }
        user.setSecurityQuestion(req.getQuestion());
        user.setSecurityAnswer(encoder.encode(req.getAnswer().trim().toLowerCase()));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/security-question/me")
    public ResponseEntity<?> getOwnSecurityQuestion(Authentication auth) {
        var user = userRepository.findByName(auth.getName()).orElseThrow();
        return ResponseEntity.ok(java.util.Map.of("question", user.getSecurityQuestion() == null ? "" : user.getSecurityQuestion()));
    }
}