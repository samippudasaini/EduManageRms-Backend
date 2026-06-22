package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "marks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Marks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subjectId")
    private Subject subject;

    @Builder.Default private Float practical = 0f;
    @Builder.Default private Float theory = 0f;
    @Builder.Default private String grade = "";
    @Builder.Default private Double gradePoint = 0.0;
}
