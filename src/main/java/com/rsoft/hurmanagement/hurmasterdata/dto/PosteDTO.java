package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.Poste;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PosteDTO {
    private Long id;
    private String codePoste;
    private Poste.TypeSalaire typeSalaire;
    private String description;
    private String codeDevise;
    private String deviseDescription;
    private BigDecimal salaireMin;
    private BigDecimal salaireMax;
    private Integer nbJourSemaine;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String updatedBy;
    private OffsetDateTime updatedOn;
    private Integer rowscn;
}
