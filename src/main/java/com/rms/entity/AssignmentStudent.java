package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    // PENDING | DONE | LATE | NOT_DONE
    @Builder.Default
    @Column(name = "submission_status")
    private String submissionStatus = "PENDING";

    // Kept for backward compatibility
    @Builder.Default
    private Boolean status = false;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gradesId")
    private GradeSectionMapping gradeSection;

    private LocalDate date;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
