package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class PrestationDepartDTO {
    private Long id;
    private Long employeId;
    private String employeCode;
    private String employeNom;
    private String employePrenom;
    private Long regimePaieId;
    private String regimePaieCode;
    private String regimePaieDescription;
    private Long mutationEmployeId;
    private String typeDepart;
    private LocalDate dateDepart;
    private LocalDateTime dateCalcul;
    private BigDecimal totalGains;
    private BigDecimal totalDeductions;
    private BigDecimal montantNet;
    private String statut;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
    private List<PrestationDepartDetailDTO> details = new ArrayList<>();
    private List<PrestationDepartDeductionDTO> deductions = new ArrayList<>();
}
