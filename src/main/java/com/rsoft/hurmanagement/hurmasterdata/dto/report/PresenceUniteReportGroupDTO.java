package com.rsoft.hurmanagement.hurmasterdata.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenceUniteReportGroupDTO {
    private Long uniteId;
    private String uniteCode;
    private String uniteNom;
    private List<PresenceUniteReportRowDTO> rows;
}
