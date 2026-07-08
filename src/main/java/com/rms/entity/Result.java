//package com.rms.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//@Table(name = "results")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Result {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "examinationId")
//    private Examination examination;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "facultyDetailId")
//    private FacultyDetail facultyDetail;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "studentId")
//    private Student student;
//
//    private String grade;
//    private Double percentage;
//
//    @Column(name = "result_rank")
//    private String rank;
//
//    @ManyToMany(fetch = FetchType.EAGER)
//    @JoinTable(
//        name = "result_marks",
//        joinColumns = @JoinColumn(name = "resultId"),
//        inverseJoinColumns = @JoinColumn(name = "marksId")
//    )
//    @Builder.Default
//    private List<Marks> marks = new ArrayList<>();
//}

package com.rms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examination_id")
    private Examination examination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_detail_id")
    private FacultyDetail facultyDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    private String grade;
    private Double percentage;

    @Column(name = "result_rank")
    private String rank;

    @ManyToMany(fetch = FetchType.EAGER
//            , cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinTable(
            name = "result_marks",
            joinColumns = @JoinColumn(name = "result_id"),
            inverseJoinColumns = @JoinColumn(name = "marks_id")
    )
    @Builder.Default
    private List<Marks> marks = new ArrayList<>();
}