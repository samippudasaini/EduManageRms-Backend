package com.rms.dto;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor
public class LoginRequest {
    private String name;
    private String password;
}
