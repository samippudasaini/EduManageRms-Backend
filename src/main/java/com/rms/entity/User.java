package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "users") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String type; // admin / staff
    private String facultyDetailId;
    private Boolean canAttendance = false;
    private Boolean canAssignment = false;
    private Boolean canExam = false;
    private Boolean canResult = false;
    private Boolean canStudent = false;
    private String securityQuestion;
    private String securityAnswer; // stored bcrypt-hashed, same as password
}
