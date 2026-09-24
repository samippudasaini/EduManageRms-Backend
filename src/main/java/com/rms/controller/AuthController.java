//package com.rms.controller;
//
//import com.rms.dto.LoginRequest;
//import com.rms.dto.LoginResponse;
//import com.rms.repository.UserRepository;
//import com.rms.security.JwtUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.*;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/auth")
//public class AuthController {
//    @Autowired private AuthenticationManager authManager;
//    @Autowired private JwtUtil jwtUtil;
//    @Autowired private UserDetailsService userDetailsService;
//    @Autowired private UserRepository userRepository;
//
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
//        try {
//            authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getName(), req.getPassword()));
//            var userDetails = userDetailsService.loadUserByUsername(req.getName());
//            var user = userRepository.findByName(req.getName()).orElseThrow();
//            String token = jwtUtil.generateToken(userDetails);
//            return ResponseEntity.ok(new LoginResponse(token, user.getId(), user.getName(), user.getType()));
//        } catch (BadCredentialsException e) {
//            return ResponseEntity.status(401).body("Invalid credentials");
//        }
//    }
//}


package com.rms.controller;

import com.rms.dto.LoginRequest;
import com.rms.dto.LoginResponse;
import com.rms.dto.ResetPasswordRequest;
import com.rms.repository.UserRepository;
import com.rms.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired private AuthenticationManager authManager;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserDetailsService userDetailsService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder encoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getName(), req.getPassword()));
            var userDetails = userDetailsService.loadUserByUsername(req.getName());
            var user = userRepository.findByName(req.getName()).orElseThrow();
            String token = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(new LoginResponse(
                    token, user.getId(), user.getName(), user.getType(),
                    user.getCanAttendance(), user.getCanAssignment(),
                    user.getCanExam(), user.getCanResult(), user.getCanStudent()
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    //  Forgot password flow (no login required)

    @GetMapping("/security-question")
    public ResponseEntity<?> getSecurityQuestion(@RequestParam String username) {
        var userOpt = userRepository.findByName(username);
        if (userOpt.isEmpty() || userOpt.get().getSecurityQuestion() == null) {
            // Same generic message whether the user exists or not, and whether a
            // question was set or not — avoids leaking which usernames are valid.
            return ResponseEntity.status(404).body("No recovery question set up for this account");
        }
        return ResponseEntity.ok(Map.of("question", userOpt.get().getSecurityQuestion()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        var userOpt = userRepository.findByName(req.getUsername());
        if (userOpt.isEmpty() || userOpt.get().getSecurityAnswer() == null) {
            return ResponseEntity.status(404).body("No recovery question set up for this account");
        }
        var user = userOpt.get();
        String normalizedAnswer = req.getAnswer() == null ? "" : req.getAnswer().trim().toLowerCase();
        if (!encoder.matches(normalizedAnswer, user.getSecurityAnswer())) {
            return ResponseEntity.status(400).body("Answer is incorrect");
        }
        if (req.getNewPassword() == null || req.getNewPassword().isBlank()) {
            return ResponseEntity.status(400).body("New password cannot be empty");
        }
        user.setPassword(encoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }
}