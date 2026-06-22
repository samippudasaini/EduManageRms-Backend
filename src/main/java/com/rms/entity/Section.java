package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "section") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Section {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
}
