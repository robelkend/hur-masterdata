package com.rsoft.hurmanagement.hurmasterdata.dto;

import com.rsoft.hurmanagement.hurmasterdata.entity.Utilisateur;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountUpdateLanguageRequest {
    @NotNull(message = "Langue is required")
    private Utilisateur.Langue langue;
}
