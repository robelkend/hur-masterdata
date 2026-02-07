package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.config.JwtProperties;
import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.entity.AuditAcces;
import com.rsoft.hurmanagement.hurmasterdata.entity.RefreshToken;
import com.rsoft.hurmanagement.hurmasterdata.entity.Utilisateur;
import com.rsoft.hurmanagement.hurmasterdata.repository.RefreshTokenRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.UtilisateurRepository;
import com.rsoft.hurmanagement.hurmasterdata.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UtilisateurRepository utilisateurRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final AuditAccesService auditAccesService;

    @Transactional
    public AuthLoginResponse login(AuthLoginRequest request, HttpServletRequest httpRequest) {
        Utilisateur utilisateur = utilisateurRepository
                .findByIdentifiantOrEmail(request.getUsername(), request.getUsername())
                .orElse(null);

        if (utilisateur == null) {
            logAuthAccess(null, request.getUsername(), AuditAcces.TypeEvenement.FAIL_LOGIN, AuditAcces.Resultat.FAIL,
                    httpRequest, "{\"message\":\"invalidCredentials\"}");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "auth.error.invalidCredentials");
        }

        if (!"Y".equalsIgnoreCase(utilisateur.getActif())) {
            logAuthAccess(utilisateur, utilisateur.getIdentifiant(), AuditAcces.TypeEvenement.FAIL_LOGIN, AuditAcces.Resultat.DENY,
                    httpRequest, "{\"message\":\"userInactive\"}");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "auth.error.userInactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), utilisateur.getPasswordHash())) {
            logAuthAccess(utilisateur, utilisateur.getIdentifiant(), AuditAcces.TypeEvenement.FAIL_LOGIN, AuditAcces.Resultat.FAIL,
                    httpRequest, "{\"message\":\"invalidCredentials\"}");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "auth.error.invalidCredentials");
        }

        boolean passwordExpired = isPasswordExpired(utilisateur);
        String accessToken = jwtTokenProvider.generateAccessToken(utilisateur.getIdentifiant(), passwordExpired);
        RefreshToken refreshToken = createRefreshToken(utilisateur);

        AuthLoginResponse response = new AuthLoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken.getToken());
        response.setExpiresIn(jwtProperties.getAccessTokenExpirationMinutes() * 60);
        response.setUser(toAuthUser(utilisateur));
        response.setPasswordExpired(passwordExpired);
        logAuthAccess(utilisateur, utilisateur.getIdentifiant(), AuditAcces.TypeEvenement.LOGIN, AuditAcces.Resultat.SUCCESS,
                httpRequest, "{}");
        return response;
    }

    @Transactional
    public AuthRefreshResponse refresh(AuthRefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "auth.error.invalidRefreshToken"));

        if ("Y".equalsIgnoreCase(refreshToken.getRevoked()) ||
                refreshToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "auth.error.invalidRefreshToken");
        }

        Utilisateur utilisateur = refreshToken.getUtilisateur();
        if (!"Y".equalsIgnoreCase(utilisateur.getActif())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "auth.error.userInactive");
        }

        boolean passwordExpired = isPasswordExpired(utilisateur);
        String accessToken = jwtTokenProvider.generateAccessToken(utilisateur.getIdentifiant(), passwordExpired);
        AuthRefreshResponse response = new AuthRefreshResponse();
        response.setAccessToken(accessToken);
        response.setExpiresIn(jwtProperties.getAccessTokenExpirationMinutes() * 60);
        response.setPasswordExpired(passwordExpired);
        return response;
    }

    @Transactional
    public void logout(AuthRefreshRequest request, HttpServletRequest httpRequest) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken()).orElse(null);
        if (refreshToken == null) {
            logAuthAccess(null, null, AuditAcces.TypeEvenement.LOGOUT, AuditAcces.Resultat.FAIL,
                    httpRequest, "{\"message\":\"invalidRefreshToken\"}");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "auth.error.invalidRefreshToken");
        }
        if ("Y".equalsIgnoreCase(refreshToken.getRevoked()) ||
                refreshToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            logAuthAccess(refreshToken.getUtilisateur(), refreshToken.getUtilisateur() != null ? refreshToken.getUtilisateur().getIdentifiant() : null,
                    AuditAcces.TypeEvenement.LOGOUT, AuditAcces.Resultat.FAIL, httpRequest, "{\"message\":\"invalidRefreshToken\"}");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "auth.error.invalidRefreshToken");
        }

        refreshToken.setRevoked("Y");
        refreshTokenRepository.save(refreshToken);
        Utilisateur utilisateur = refreshToken.getUtilisateur();
        logAuthAccess(utilisateur, utilisateur != null ? utilisateur.getIdentifiant() : null,
                AuditAcces.TypeEvenement.LOGOUT, AuditAcces.Resultat.SUCCESS, httpRequest, "{}");
    }

    private RefreshToken createRefreshToken(Utilisateur utilisateur) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUtilisateur(utilisateur);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(OffsetDateTime.now().plusDays(jwtProperties.getRefreshTokenExpirationDays()));
        refreshToken.setRevoked("N");
        refreshToken.setCreatedOn(OffsetDateTime.now());
        return refreshTokenRepository.save(refreshToken);
    }

    private AuthUserDTO toAuthUser(Utilisateur utilisateur) {
        AuthUserDTO user = new AuthUserDTO();
        user.setId(utilisateur.getId());
        user.setIdentifiant(utilisateur.getIdentifiant());
        user.setEmail(utilisateur.getEmail());
        user.setNom(utilisateur.getNom());
        user.setPrenom(utilisateur.getPrenom());
        user.setLangue(utilisateur.getLangue());
        if (utilisateur.getEntreprise() != null) {
            user.setEntrepriseId(utilisateur.getEntreprise().getId());
            user.setEntrepriseNom(utilisateur.getEntreprise().getNomEntreprise());
        }
        return user;
    }

    private boolean isPasswordExpired(Utilisateur utilisateur) {
        if (utilisateur.getDateExpPassword() == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return !utilisateur.getDateExpPassword().isAfter(today);
    }

    private void logAuthAccess(Utilisateur utilisateur,
                               String username,
                               AuditAcces.TypeEvenement type,
                               AuditAcces.Resultat resultat,
                               HttpServletRequest request,
                               String detailsJson) {
        try {
            AuditAcces audit = new AuditAcces();
            audit.setDateEvenement(OffsetDateTime.now());
            audit.setEntreprise(utilisateur != null ? utilisateur.getEntreprise() : null);
            audit.setUtilisateur(username);
            audit.setTypeEvenement(type);
            audit.setResultat(resultat);
            audit.setResourceType("AUTH");
            audit.setActionCode(type.name());
            audit.setIpAddress(resolveIp(request));
            audit.setUserAgent(request != null ? request.getHeader("User-Agent") : null);
            audit.setSessionId(request != null && request.getSession(false) != null ? request.getSession(false).getId() : null);
            audit.setRequestId(request != null ? request.getHeader("X-Request-Id") : null);
            audit.setDetails(detailsJson != null ? detailsJson : "{}");
            auditAccesService.create(audit);
        } catch (Exception ignored) {
        }
    }

    private String resolveIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
