package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayrollEmployeBoniDTO {
    private Long id;
    private String statut;
    private Long rubriquePaieId;
    private String rubriquePaieCode;
    private Long regimePaieId;
    private String regimePaieCode;
    private Long periodeBoniId;
    private String periodeBoniCode;
    private Long employeId;
    private String employeCode;
    private String employeNom;
    private String employePrenom;
    private BigDecimal montantReference;
    private BigDecimal diviseur;
    private BigDecimal montantBoniBrut;
    private BigDecimal montantDeductions;
    private BigDecimal montantBoniNet;
}
