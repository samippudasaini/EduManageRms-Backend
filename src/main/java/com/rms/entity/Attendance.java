package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "attendance",
uniqueConstraints = @UniqueConstraint(columnNames = {"studentId", "date"}))

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studentId")
    private Student student;

    private LocalDate date;

    /**
     * Status: P = Present, A = Absent, L = Late
     * Replaces the old Boolean status field.
     * Default: P (Present)
     */
    @Builder.Default
    @Column(name = "attendance_status", length = 1)
    private String attendanceStatus = "P";

    // Kept for backward compatibility with old code
    @Builder.Default
    private Boolean status = true;
}
