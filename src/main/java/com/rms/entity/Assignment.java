package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "assignment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    private String date;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gradesId")
    private GradeSectionMapping gradeSection;
}
