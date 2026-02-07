package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class EmployeContactDTO {
    private Long id;
    private Long employeId;
    private String nom;
    private String prenom;
    private String lien;
    private String telephone1;
    private String telephone2;
    private String courriel;
    private Integer priorite;
    private String actif;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
