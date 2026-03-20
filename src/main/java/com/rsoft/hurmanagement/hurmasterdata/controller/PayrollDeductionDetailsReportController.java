package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.report.PayrollDeductionDetailsReportResponseDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.Utilisateur;
import com.rsoft.hurmanagement.hurmasterdata.repository.UtilisateurRepository;
import com.rsoft.hurmanagement.hurmasterdata.service.PayrollDeductionDetailsReportService;
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
public class PayrollDeductionDetailsReportController {

    private final PayrollDeductionDetailsReportService service;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping("/payroll-et-deduction")
    public ResponseEntity<PayrollDeductionDetailsReportResponseDTO> preview(
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
                dateDebut, dateFin, effectiveEntrepriseId, nuit, uniteOrganisationnelleId, typeEmployeId,
                gestionnaireId, employeId, actif, regimePaieIds, statut
        ));
    }

    @GetMapping("/payroll-et-deduction/export")
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
            @RequestParam(defaultValue = "pdf") String format
    ) {
        Long effectiveEntrepriseId = resolveEffectiveEntrepriseId(entrepriseId);
        PayrollDeductionDetailsReportResponseDTO report = service.buildReport(
                dateDebut, dateFin, effectiveEntrepriseId, nuit, uniteOrganisationnelleId, typeEmployeId,
                gestionnaireId, employeId, actif, regimePaieIds, statut
        );

        byte[] content;
        String filename;
        String f = format == null ? "pdf" : format.toLowerCase();
        switch (f) {
            case "xlsx" -> {
                content = service.exportXlsx(report);
                filename = "payroll_et_deduction.xlsx";
            }
            case "docx" -> {
                content = service.exportDocx(report);
                filename = "payroll_et_deduction.docx";
            }
            default -> {
                content = service.exportPdf(report);
                filename = "payroll_et_deduction.pdf";
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
