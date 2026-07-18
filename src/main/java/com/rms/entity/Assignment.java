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

    @Column(columnDefinition = "TEXT")
    private String description;

    private String date;
    private String dueDate;

    // File attachment fields
    private String fileName;       // original file name shown to user
    private String filePath;       // stored path on server
    private String fileType;       // e.g. application/pdf, image/png, text/plain
    private Long   fileSize;       // bytes

    @Column(name = "status")
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, DRAFT, COMPLETED

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gradesId")
    private GradeSectionMapping gradeSection;;
}
