package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "examinations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Examination {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String year;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "examinations_program",
        joinColumns = @JoinColumn(name = "examination_id"),
        inverseJoinColumns = @JoinColumn(name = "program_id")
    )
    @Builder.Default
    private List<Program> programs = new ArrayList<>();
}
