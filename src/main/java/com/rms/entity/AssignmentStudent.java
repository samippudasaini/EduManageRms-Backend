package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "assignment_student")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentStudent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignmentId")
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "studentId")
    private Student student;

    @Builder.Default
    private Boolean status = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gradesId")
    private GradeSectionMapping gradeSection;

    private LocalDate date;
}
