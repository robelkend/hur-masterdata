package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.report.PresenceReportResponseDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.report.PresenceUniteReportResponseDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.Utilisateur;
import com.rsoft.hurmanagement.hurmasterdata.repository.UtilisateurRepository;
import com.rsoft.hurmanagement.hurmasterdata.service.PresenceReportService;
import com.rsoft.hurmanagement.hurmasterdata.service.PresenceUniteReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/presence-reports")
@RequiredArgsConstructor
public class PresenceReportController {

    private final PresenceReportService service;
    private final PresenceUniteReportService presenceUniteReportService;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping("/feuille-presence")
    public ResponseEntity<PresenceReportResponseDTO> preview(
            @RequestParam LocalDate dateDebut,
            @RequestParam LocalDate dateFin,
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(required = false) String nuit,
            @RequestParam(required = false) List<Long> uniteOrganisationnelleId,
            @RequestParam(required = false) List<Long> typeEmployeId,
            @RequestParam(required = false) Long gestionnaireId,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) String actif,
            @RequestParam(required = false) List<Long> regimePaieIds,
            @RequestParam(defaultValue = "true") Boolean showAbsences,
            @RequestParam(defaultValue = "true") Boolean showConges,
            @RequestParam(defaultValue = "true") Boolean showOffs,
            @RequestParam(defaultValue = "true") Boolean showFeries
    ) {
        Long effectiveEntrepriseId = resolveEffectiveEntrepriseId(entrepriseId);
        return ResponseEntity.ok(service.buildReport(
                dateDebut,
                dateFin,
                effectiveEntrepriseId,
                nuit,
                uniteOrganisationnelleId,
                typeEmployeId,
                gestionnaireId,
                employeId,
                actif,
                regimePaieIds,
                Boolean.TRUE.equals(showAbsences),
                Boolean.TRUE.equals(showConges),
                Boolean.TRUE.equals(showOffs),
                Boolean.TRUE.equals(showFeries)
        ));
    }

    @GetMapping("/presence-unite-organisationnelle")
    public ResponseEntity<PresenceUniteReportResponseDTO> previewPresenceUnite(
            @RequestParam LocalDate dateDebut,
            @RequestParam LocalDate dateFin,
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(required = false) String nuit,
            @RequestParam(required = false) List<Long> uniteOrganisationnelleId,
            @RequestParam(required = false) List<Long> typeEmployeId,
            @RequestParam(required = false) Long gestionnaireId,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) String actif,
            @RequestParam(required = false) List<Long> regimePaieIds,
            @RequestParam(required = false) String orientation
    ) {
        Long effectiveEntrepriseId = resolveEffectiveEntrepriseId(entrepriseId);
        return ResponseEntity.ok(presenceUniteReportService.buildReport(
                dateDebut,
                dateFin,
                effectiveEntrepriseId,
                nuit,
                uniteOrganisationnelleId,
                typeEmployeId,
                gestionnaireId,
                employeId,
                actif,
                regimePaieIds
        ));
    }

    @GetMapping("/presence-unite-organisationnelle/export")
    public ResponseEntity<byte[]> exportPresenceUnite(
            @RequestParam LocalDate dateDebut,
            @RequestParam LocalDate dateFin,
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(required = false) String nuit,
            @RequestParam(required = false) List<Long> uniteOrganisationnelleId,
            @RequestParam(required = false) List<Long> typeEmployeId,
            @RequestParam(required = false) Long gestionnaireId,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) String actif,
            @RequestParam(required = false) List<Long> regimePaieIds,
            @RequestParam(required = false) String orientation,
            @RequestParam(required = false) Integer textSize,
            @RequestParam(defaultValue = "pdf") String format
    ) {
        Long effectiveEntrepriseId = resolveEffectiveEntrepriseId(entrepriseId);
        PresenceUniteReportResponseDTO report = presenceUniteReportService.buildReport(
                dateDebut,
                dateFin,
                effectiveEntrepriseId,
                nuit,
                uniteOrganisationnelleId,
                typeEmployeId,
                gestionnaireId,
                employeId,
                actif,
                regimePaieIds
        );

        byte[] content;
        String filename;
        String safeFormat = format == null ? "pdf" : format.toLowerCase();
        switch (safeFormat) {
            case "xlsx" -> {
                content = presenceUniteReportService.exportXlsx(report, textSize, orientation);
                filename = "presence_unite_organisationnelle.xlsx";
            }
            case "docx" -> {
                content = presenceUniteReportService.exportDocx(report, textSize, orientation);
                filename = "presence_unite_organisationnelle.docx";
            }
            default -> {
                content = presenceUniteReportService.exportPdf(report, textSize, orientation);
                filename = "presence_unite_organisationnelle.pdf";
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    @GetMapping("/feuille-presence/export")
    public ResponseEntity<byte[]> export(
            @RequestParam LocalDate dateDebut,
            @RequestParam LocalDate dateFin,
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(required = false) String nuit,
            @RequestParam(required = false) List<Long> uniteOrganisationnelleId,
            @RequestParam(required = false) List<Long> typeEmployeId,
            @RequestParam(required = false) Long gestionnaireId,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) String actif,
            @RequestParam(required = false) List<Long> regimePaieIds,
            @RequestParam(defaultValue = "true") Boolean showAbsences,
            @RequestParam(defaultValue = "true") Boolean showConges,
            @RequestParam(defaultValue = "true") Boolean showOffs,
            @RequestParam(defaultValue = "true") Boolean showFeries,
            @RequestParam(required = false) Integer textSize,
            @RequestParam(defaultValue = "pdf") String format
    ) {
        Long effectiveEntrepriseId = resolveEffectiveEntrepriseId(entrepriseId);
        PresenceReportResponseDTO report = service.buildReport(
                dateDebut,
                dateFin,
                effectiveEntrepriseId,
                nuit,
                uniteOrganisationnelleId,
                typeEmployeId,
                gestionnaireId,
                employeId,
                actif,
                regimePaieIds,
                Boolean.TRUE.equals(showAbsences),
                Boolean.TRUE.equals(showConges),
                Boolean.TRUE.equals(showOffs),
                Boolean.TRUE.equals(showFeries)
        );

        byte[] content;
        String filename;
        String safeFormat = format == null ? "pdf" : format.toLowerCase();
        switch (safeFormat) {
            case "xlsx" -> {
                content = service.exportXlsx(report, dateDebut, dateFin, textSize);
                filename = "feuille_presence.xlsx";
            }
            case "docx" -> {
                content = service.exportDocx(report, dateDebut, dateFin, textSize);
                filename = "feuille_presence.docx";
            }
            default -> {
                content = service.exportPdf(report, dateDebut, dateFin, textSize);
                filename = "feuille_presence.pdf";
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    private Long resolveEffectiveEntrepriseId(Long requestedEntrepriseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return requestedEntrepriseId;
        }
        Utilisateur utilisateur = utilisateurRepository.findByIdentifiant(authentication.getName()).orElse(null);
        if (utilisateur != null && utilisateur.getEntreprise() != null && utilisateur.getEntreprise().getId() != null) {
            // If user is bound to an entreprise, reports with employee scope are always constrained to it.
            return utilisateur.getEntreprise().getId();
        }
        return requestedEntrepriseId;
    }
}
