package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class PayrollEmployeDTO {
    private Long id;
    private Long payrollId;
    private Long employeId;
    private String employeCode;
    private String employeNom;
    private String employePrenom;
    private BigDecimal montantSalaireBase;
    private BigDecimal montantSupplementaire;
    private BigDecimal montantAutreRevenu;
    private BigDecimal montantBrut;
    private BigDecimal montantDeductions;
    private BigDecimal montantRecouvrements;
    private BigDecimal montantSanctions;
    private BigDecimal montantNetAPayer;
    private String modePaiement;
    private String noCheque;
    private String libelleBanque;
    private String noCompte;
    private String typeCompte;
    private String emailEnvoye;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
