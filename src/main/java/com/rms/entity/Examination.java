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
        name = "examinations_faculty_details",
        joinColumns = @JoinColumn(name = "examinationId"),
        inverseJoinColumns = @JoinColumn(name = "facultyDetailId")
    )
    @Builder.Default
    private List<FacultyDetail> facultyDetails = new ArrayList<>();
}
