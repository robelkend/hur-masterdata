package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRefreshRequest {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
