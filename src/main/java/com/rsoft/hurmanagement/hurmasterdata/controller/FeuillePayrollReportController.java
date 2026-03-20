package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.report.FeuillePayrollReportResponseDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.Utilisateur;
import com.rsoft.hurmanagement.hurmasterdata.repository.UtilisateurRepository;
import com.rsoft.hurmanagement.hurmasterdata.service.FeuillePayrollReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/payroll-reports")
@RequiredArgsConstructor
public class FeuillePayrollReportController {

    private final FeuillePayrollReportService service;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping("/feuille-payroll")
    public ResponseEntity<FeuillePayrollReportResponseDTO> preview(
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
            @RequestParam(required = false) String statut
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
                statut
        ));
    }

    @GetMapping("/feuille-payroll/export")
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
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String orientation,
            @RequestParam(required = false) Integer textSize,
            @RequestParam(defaultValue = "pdf") String format
    ) {
        Long effectiveEntrepriseId = resolveEffectiveEntrepriseId(entrepriseId);
        FeuillePayrollReportResponseDTO report = service.buildReport(
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
                statut
        );

        byte[] content;
        String filename;
        String safeFormat = format == null ? "pdf" : format.toLowerCase();
        switch (safeFormat) {
            case "xlsx" -> {
                content = service.exportXlsx(report, textSize, orientation);
                filename = "feuille_payroll.xlsx";
            }
            case "docx" -> {
                content = service.exportDocx(report, textSize, orientation);
                filename = "feuille_payroll.docx";
            }
            default -> {
                content = service.exportPdf(report, textSize, orientation);
                filename = "feuille_payroll.pdf";
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
            return utilisateur.getEntreprise().getId();
        }
        return requestedEntrepriseId;
    }
}
