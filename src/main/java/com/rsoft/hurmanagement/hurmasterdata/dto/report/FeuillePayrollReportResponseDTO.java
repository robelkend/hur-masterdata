package com.rsoft.hurmanagement.hurmasterdata.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeuillePayrollReportResponseDTO {
    private PresenceReportHeaderDTO header;
    private List<FeuillePayrollReportRowDTO> rows;
}
