package com.rms.dto;
import lombok.*;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String name;
    private String type;
    private Boolean canAttendance;
    private Boolean canAssignment;
    private Boolean canExam;
    private Boolean canResult;
    private Boolean canStudent;
}