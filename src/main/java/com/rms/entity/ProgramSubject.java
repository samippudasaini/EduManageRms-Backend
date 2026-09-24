package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "program_subjects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    /** true = optional subject for this program, false = compulsory */
    @Column(name = "is_optional", nullable = false)
    @Builder.Default
    private Boolean optional = false;
}
