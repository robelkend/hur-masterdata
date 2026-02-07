package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertConfigCreateDTO {
    @NotBlank(message = "Code message is required")
    private String codeMessage;
    
    @NotNull(message = "Email is required")
    private String email; // 'Y' or 'N'
    
    @NotNull(message = "Push alert is required")
    private String pushAlert; // 'Y' or 'N'
}
