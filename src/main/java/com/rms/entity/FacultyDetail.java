package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "faculty_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacultyDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "streamId")
    private Stream stream;

    @OneToMany(mappedBy = "facultyDetail", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<FacultyDetailSubject> subjectMappings = new ArrayList<>();
}
