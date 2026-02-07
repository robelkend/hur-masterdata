package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.AccountChangePasswordRequest;
import com.rsoft.hurmanagement.hurmasterdata.dto.AccountUpdateLanguageRequest;
import com.rsoft.hurmanagement.hurmasterdata.entity.Utilisateur;
import com.rsoft.hurmanagement.hurmasterdata.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody AccountChangePasswordRequest request) {
        accountService.changePassword(getCurrentUsername(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/language")
    public ResponseEntity<Utilisateur.Langue> updateLanguage(@Valid @RequestBody AccountUpdateLanguageRequest request) {
        return ResponseEntity.ok(accountService.updateLanguage(getCurrentUsername(), request));
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "system";
        }
        return authentication.getName();
    }
}
