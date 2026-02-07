package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class EmployeIdentiteDTO {
    private Long id;
    private Long employeId;
    private String typePiece;
    private String numeroPiece;
    private LocalDate dateEmission;
    private LocalDate dateExpiration;
    private String paysEmission;
    private String actif;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
