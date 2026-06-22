package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "grades") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Grade {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
}
