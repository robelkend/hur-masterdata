package com.rsoft.hurmanagement.hurmasterdata.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HoraireDtUpdateDTO {
    private Long id; // null for new, existing id for update
    private Integer rowscn; // 0 for new, existing rowscn for update
    
    @NotNull(message = "Jour is required")
    private Integer jour; // 1=Lundi, 2=Mardi, etc.
    
    private String heureDebutJour; // format HH:mi
    private String heureFinJour; // format HH:mi
    private String heureDebutNuit; // format HH:mi
    private String heureFinNuit; // format HH:mi
    private String exigerPresence; // 'Y' or 'N', default 'N' if null
    private String heureFermetureAuto; // 'Y' or 'N', default 'N' if null
}
