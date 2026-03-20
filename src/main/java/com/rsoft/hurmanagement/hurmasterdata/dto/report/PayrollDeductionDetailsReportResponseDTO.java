package com.rsoft.hurmanagement.hurmasterdata.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollDeductionDetailsReportResponseDTO {
    private PresenceReportHeaderDTO header;
    private List<String> payrollDates;
    private List<String> deductionEmployeHeaders;
    private List<String> deductionEmployeurHeaders;
    private PayrollDeductionDetailsTotalDTO grandTotalTop;
    private List<PayrollDeductionDetailsGroupDTO> groupes;
    private PayrollDeductionDetailsTotalDTO grandTotalBottom;
}
