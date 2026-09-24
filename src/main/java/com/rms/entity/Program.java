package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "program_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Program {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "streamId")
    private Stream stream;

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)    @Builder.Default
    private List<ProgramSubject> subjectMappings = new ArrayList<>();
}
