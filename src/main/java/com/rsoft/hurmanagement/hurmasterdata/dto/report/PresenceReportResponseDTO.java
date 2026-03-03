package com.rsoft.hurmanagement.hurmasterdata.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenceReportResponseDTO {
    private PresenceReportHeaderDTO header;
    private List<PresenceReportRowDTO> rows;
    private PresenceReportStatsDTO stats;
}
