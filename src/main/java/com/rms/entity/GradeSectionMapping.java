package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "grades_section_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeSectionMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gradeId")
    private Grade grade;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sectionId")
    private Section section;
}
