package com.rms.dto;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String name;
    private String type;
}
