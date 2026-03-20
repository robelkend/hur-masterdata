package com.rsoft.hurmanagement.hurmasterdata.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollDeductionDetailsGroupDTO {
    private Long uniteId;
    private String uniteCode;
    private String uniteNom;
    private List<PayrollDeductionDetailsRowDTO> rows;
    private PayrollDeductionDetailsTotalDTO total;
}
