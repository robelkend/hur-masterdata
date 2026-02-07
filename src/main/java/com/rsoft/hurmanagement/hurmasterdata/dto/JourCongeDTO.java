package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.JourConge;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JourCongeDTO {
    private Long id;
    private JourConge.Type type;
    private LocalDate dateConge;
    private String description;
    private JourConge.MiJournee miJournee;
    private JourConge.Actif actif;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
