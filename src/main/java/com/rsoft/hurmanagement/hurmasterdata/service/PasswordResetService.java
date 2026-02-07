package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.ForgotPasswordRequest;
import com.rsoft.hurmanagement.hurmasterdata.dto.ResetPasswordRequest;
import com.rsoft.hurmanagement.hurmasterdata.entity.PasswordResetToken;
import com.rsoft.hurmanagement.hurmasterdata.entity.Utilisateur;
import com.rsoft.hurmanagement.hurmasterdata.repository.PasswordResetTokenRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    private static final int RESET_TOKEN_TTL_MINUTES = 60;

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void requestReset(ForgotPasswordRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail()).orElse(null);
        if (utilisateur == null) {
            return;
        }
        PasswordResetToken token = new PasswordResetToken();
        token.setUtilisateur(utilisateur);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(OffsetDateTime.now().plusMinutes(RESET_TOKEN_TTL_MINUTES));
        token.setUsed("N");
        token.setCreatedOn(OffsetDateTime.now());
        passwordResetTokenRepository.save(token);

        log.info("Password reset token created for {}: {}", utilisateur.getEmail(), token.getToken());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "auth.error.passwordConfirmMismatch");
        }
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "auth.error.resetTokenInvalid"));

        if ("Y".equalsIgnoreCase(token.getUsed()) || token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "auth.error.resetTokenInvalid");
        }

        Utilisateur utilisateur = token.getUtilisateur();
        utilisateur.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        utilisateur.setDateExpPassword(null);
        utilisateur.setUpdatedBy("SYSTEM");
        utilisateur.setUpdatedOn(OffsetDateTime.now());
        utilisateurRepository.save(utilisateur);

        token.setUsed("Y");
        token.setUsedOn(OffsetDateTime.now());
        passwordResetTokenRepository.save(token);
    }
}
