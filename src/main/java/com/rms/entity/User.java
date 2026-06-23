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
    private String facultyDetailId;       // which program/faculty this teacher manages
    private Boolean canAttendance = false;
    private Boolean canAssignment = false;
    private Boolean canExam = false;
    private Boolean canResult = false;
}
