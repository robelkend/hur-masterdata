package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.AccountChangePasswordRequest;
import com.rsoft.hurmanagement.hurmasterdata.dto.AccountUpdateLanguageRequest;
import com.rsoft.hurmanagement.hurmasterdata.entity.Utilisateur;
import com.rsoft.hurmanagement.hurmasterdata.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePassword(String identifiant, AccountChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "auth.error.passwordConfirmMismatch");
        }
        Utilisateur utilisateur = utilisateurRepository.findByIdentifiant(identifiant)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "auth.error.userNotFound"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), utilisateur.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "auth.error.currentPasswordInvalid");
        }

        utilisateur.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        utilisateur.setDateExpPassword(null);
        utilisateur.setUpdatedBy(identifiant);
        utilisateur.setUpdatedOn(OffsetDateTime.now());
        utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public Utilisateur.Langue updateLanguage(String identifiant, AccountUpdateLanguageRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByIdentifiant(identifiant)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "auth.error.userNotFound"));

        utilisateur.setLangue(request.getLangue());
        utilisateur.setUpdatedBy(identifiant);
        utilisateur.setUpdatedOn(OffsetDateTime.now());
        utilisateurRepository.save(utilisateur);
        return utilisateur.getLangue();
    }
}
