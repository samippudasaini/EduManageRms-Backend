package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    private String address;
    private String guardianName;
    private String contact;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "facultyDetailsId")
    private FacultyDetail facultyDetail;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gradesId")
    private GradeSectionMapping gradeSection;
}
