package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "subjects") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Subject {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private Float theory;
    private Float practical;
    private Float passMarks;
    private Float fullMarks;
    private Integer creditHour;
}
