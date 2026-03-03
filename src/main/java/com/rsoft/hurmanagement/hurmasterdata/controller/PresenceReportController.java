package com.rsoft.hurmanagement.hurmasterdata.controller;

import com.rsoft.hurmanagement.hurmasterdata.dto.report.PresenceReportResponseDTO;
import com.rsoft.hurmanagement.hurmasterdata.service.PresenceReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/presence-reports")
@RequiredArgsConstructor
public class PresenceReportController {

    private final PresenceReportService service;

    @GetMapping("/feuille-presence")
    public ResponseEntity<PresenceReportResponseDTO> preview(
            @RequestParam LocalDate dateDebut,
            @RequestParam LocalDate dateFin,
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(required = false) String nuit,
            @RequestParam(required = false) Long uniteOrganisationnelleId,
            @RequestParam(required = false) Long typeEmployeId,
            @RequestParam(required = false) Long gestionnaireId,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) String actif,
            @RequestParam(required = false) List<Long> regimePaieIds
    ) {
        return ResponseEntity.ok(service.buildReport(
                dateDebut,
                dateFin,
                entrepriseId,
                nuit,
                uniteOrganisationnelleId,
                typeEmployeId,
                gestionnaireId,
                employeId,
                actif,
                regimePaieIds
        ));
    }

    @GetMapping("/feuille-presence/export")
    public ResponseEntity<byte[]> export(
            @RequestParam LocalDate dateDebut,
            @RequestParam LocalDate dateFin,
            @RequestParam(required = false) Long entrepriseId,
            @RequestParam(required = false) String nuit,
            @RequestParam(required = false) Long uniteOrganisationnelleId,
            @RequestParam(required = false) Long typeEmployeId,
            @RequestParam(required = false) Long gestionnaireId,
            @RequestParam(required = false) Long employeId,
            @RequestParam(required = false) String actif,
            @RequestParam(required = false) List<Long> regimePaieIds,
            @RequestParam(defaultValue = "pdf") String format
    ) {
        PresenceReportResponseDTO report = service.buildReport(
                dateDebut,
                dateFin,
                entrepriseId,
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
                content = service.exportXlsx(report, dateDebut, dateFin);
                filename = "feuille_presence.xlsx";
            }
            case "docx" -> {
                content = service.exportDocx(report, dateDebut, dateFin);
                filename = "feuille_presence.docx";
            }
            default -> {
                content = service.exportPdf(report, dateDebut, dateFin);
                filename = "feuille_presence.pdf";
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }
}
