package com.rms.dto;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String username;
    private String answer;
    private String newPassword;
}