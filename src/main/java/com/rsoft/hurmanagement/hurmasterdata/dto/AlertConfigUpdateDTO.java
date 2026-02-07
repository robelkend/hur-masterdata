package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertConfigUpdateDTO {
    @NotNull(message = "Id is required")
    private Long id;
    
    @NotNull(message = "Email is required")
    private String email; // 'Y' or 'N'
    
    @NotNull(message = "Push alert is required")
    private String pushAlert; // 'Y' or 'N'
    
    @NotNull(message = "Rowscn is required")
    private Integer rowscn;
}
