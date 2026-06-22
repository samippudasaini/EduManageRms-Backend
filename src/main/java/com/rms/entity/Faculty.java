package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "faculty") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Faculty {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
}
