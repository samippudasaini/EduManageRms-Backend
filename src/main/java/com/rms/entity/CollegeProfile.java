package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "collegeprofile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollegeProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String address;
    private String email;
    private String phone;
    @Column(name = "principalName")
    private String principalName;
    @Column(name = "logoPath")
    private String logoPath;
    @Column(columnDefinition = "TEXT", name = "importantInfo")
    private String importantInfo;
    @Column(columnDefinition = "TEXT")
    private String slogan;
}
