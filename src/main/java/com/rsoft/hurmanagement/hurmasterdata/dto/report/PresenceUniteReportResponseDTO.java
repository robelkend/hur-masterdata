package com.rsoft.hurmanagement.hurmasterdata.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenceUniteReportResponseDTO {
    private PresenceReportHeaderDTO header;
    private List<String> dates;
    private List<PresenceUniteReportGroupDTO> groupes;
}
