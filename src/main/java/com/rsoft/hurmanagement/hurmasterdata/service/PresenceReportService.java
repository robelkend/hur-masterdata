package com.rsoft.hurmanagement.hurmasterdata.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.rsoft.hurmanagement.hurmasterdata.dto.report.PresenceReportHeaderDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.report.PresenceReportResponseDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.report.PresenceReportRowDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.report.PresenceReportStatsDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.Employe;
import com.rsoft.hurmanagement.hurmasterdata.entity.Entreprise;
import com.rsoft.hurmanagement.hurmasterdata.entity.PresenceEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.RegimePaie;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.UniteOrganisationnelle;
import com.rsoft.hurmanagement.hurmasterdata.repository.EntrepriseRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PresenceEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RegimePaieRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.TypeEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.UniteOrganisationnelleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PresenceReportService {

    private final PresenceEmployeRepository presenceEmployeRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final EmployeRepository employeRepository;
    private final TypeEmployeRepository typeEmployeRepository;
    private final UniteOrganisationnelleRepository uniteOrganisationnelleRepository;
    private final RegimePaieRepository regimePaieRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional(readOnly = true)
    public PresenceReportResponseDTO buildReport(LocalDate dateDebut,
                                                 LocalDate dateFin,
                                                 Long entrepriseId,
                                                 String nuit,
                                                 Long uniteOrganisationnelleId,
                                                 Long typeEmployeId,
                                                 Long gestionnaireId,
                                                 Long employeId,
                                                 String actif,
                                                 List<Long> regimePaieIds) {
        validateDateRange(dateDebut, dateFin);
        List<Long> safeRegimeIds = (regimePaieIds == null || regimePaieIds.isEmpty()) ? null : regimePaieIds;
        String nightFilter = (nuit != null && !nuit.isBlank()) ? nuit.trim().toUpperCase() : null;
        String actifFilter = (actif != null && !actif.isBlank()) ? actif.trim().toUpperCase() : null;

        List<PresenceEmploye> presences = presenceEmployeRepository.findForPresenceReport(
                dateDebut,
                dateFin,
                entrepriseId,
                employeId,
                actifFilter,
                typeEmployeId,
                uniteOrganisationnelleId,
                gestionnaireId,
                nightFilter,
                safeRegimeIds
        );

        PresenceReportHeaderDTO header = buildHeader(entrepriseId);
        header.setGeneratedAt(LocalDateTime.now().format(DATE_TIME_FORMAT));
        header.setCriteriaSummary(this.buildCriteriaSummary(
                dateDebut,
                dateFin,
                entrepriseId,
                nightFilter,
                uniteOrganisationnelleId,
                typeEmployeId,
                gestionnaireId,
                employeId,
                actifFilter,
                safeRegimeIds
        ));
        List<PresenceReportRowDTO> rows = buildRows(presences);
        PresenceReportStatsDTO stats = buildStats(rows, presences);

        return new PresenceReportResponseDTO(header, rows, stats);
    }

    public byte[] exportPdf(PresenceReportResponseDTO report, LocalDate dateDebut, LocalDate dateFin) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(document, out);
            document.open();

            addPdfHeader(document, report.getHeader(), dateDebut, dateFin);
            addPdfTable(document, report.getRows());
            addPdfStats(document, report.getStats());

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("presenceReport.error.pdf", ex);
        }
    }

    public byte[] exportXlsx(PresenceReportResponseDTO report, LocalDate dateDebut, LocalDate dateFin) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Feuille de presence");
            int rowIndex = 0;

            rowIndex = writeXlsxHeader(sheet, report.getHeader(), dateDebut, dateFin, rowIndex);
            rowIndex++;
            rowIndex = writeXlsxTable(sheet, report.getRows(), rowIndex);
            rowIndex++;
            writeXlsxStats(sheet, report.getStats(), rowIndex);

            autoSize(sheet, 11);
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("presenceReport.error.xlsx", ex);
        }
    }

    public byte[] exportDocx(PresenceReportResponseDTO report, LocalDate dateDebut, LocalDate dateFin) {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            addDocxHeader(document, report.getHeader(), dateDebut, dateFin);
            addDocxTable(document, report.getRows());
            addDocxStats(document, report.getStats());
            document.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("presenceReport.error.docx", ex);
        }
    }

    private void validateDateRange(LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut == null || dateFin == null) {
            throw new RuntimeException("presenceReport.error.dateRequired");
        }
        if (dateFin.isBefore(dateDebut)) {
            throw new RuntimeException("presenceReport.error.dateInvalid");
        }
        long days = ChronoUnit.DAYS.between(dateDebut, dateFin);
        if (days > 6) {
            throw new RuntimeException("presenceReport.error.dateRangeTooLong");
        }
    }

    private PresenceReportHeaderDTO buildHeader(Long entrepriseId) {
        Entreprise entreprise = null;
        if (entrepriseId != null) {
            entreprise = entrepriseRepository.findById(entrepriseId).orElse(null);
        }
        if (entreprise == null) {
            entreprise = entrepriseRepository.findFirstByEntrepriseMereIsNullOrderByIdAsc().orElse(null);
        }
        if (entreprise == null) {
            entreprise = entrepriseRepository.findFirstByActif("Y").orElse(null);
        }
        if (entreprise == null) {
            return new PresenceReportHeaderDTO();
        }
        return new PresenceReportHeaderDTO(
                entreprise.getId(),
                entreprise.getCodeEntreprise(),
                entreprise.getNomEntreprise(),
                entreprise.getNomLegal(),
                entreprise.getAdresse(),
                entreprise.getVille(),
                entreprise.getEtat(),
                entreprise.getTelephone1(),
                entreprise.getTelephone2(),
                entreprise.getFax(),
                entreprise.getCourriel(),
                entreprise.getLogoUrl(),
                null,
                null
        );
    }

    private List<PresenceReportRowDTO> buildRows(List<PresenceEmploye> presences) {
        List<PresenceReportRowDTO> rows = new ArrayList<>();
        int index = 1;
        for (PresenceEmploye presence : presences) {
            PresenceReportRowDTO row = new PresenceReportRowDTO();
            row.setRang(index++);
            row.setCodeEmploye(presence.getEmploye() != null ? presence.getEmploye().getCodeEmploye() : null);
            row.setNomEmploye(buildEmployeName(presence));
            row.setDateDebut(presence.getDateJour());
            row.setHeureDebut(presence.getHeureArrivee());
            row.setDateFin(presence.getDateDepart() != null ? presence.getDateDepart() : presence.getDateJour());
            row.setHeureFin(presence.getHeureDepart());
            boolean isNight = presence.getDateDepart() != null && presence.getDateDepart().isAfter(presence.getDateJour());
            row.setNuit(isNight ? "OUI" : "NON");
            row.setHoraireSpecialNuit(resolveHoraireSpecialNuit(presence));
            row.setOff(resolveFlag(presence, "off"));
            row.setFerie(resolveFlag(presence, "ferie"));
            row.setAbsence(isAbsent(presence) ? "OUI" : "NON");
            row.setNbHeures(resolveNbHeures(presence));
            row.setRefSup(resolveRefSup(presence));
            rows.add(row);
        }
        return rows;
    }

    private PresenceReportStatsDTO buildStats(List<PresenceReportRowDTO> rows, List<PresenceEmploye> presences) {
        int total = rows.size();
        int presencesCount = 0;
        int offs = 0;
        int conges = 0;
        int feries = 0;
        int absences = 0;
        BigDecimal totalMinutes = BigDecimal.ZERO;

        for (int i = 0; i < rows.size(); i++) {
            PresenceReportRowDTO row = rows.get(i);
            PresenceEmploye presence = presences.get(i);
            String ref = row.getRefSup();
            if ("OFF".equalsIgnoreCase(ref)) {
                offs++;
            } else if ("CONGE".equalsIgnoreCase(ref)) {
                conges++;
            } else if ("FERIE".equalsIgnoreCase(ref)) {
                feries++;
            } else if ("ABS".equalsIgnoreCase(ref) || "ABSENCE".equalsIgnoreCase(ref)) {
                absences++;
            } else {
                presencesCount++;
            }
            BigDecimal hours = resolveNbHeures(presence);
            if (hours != null) {
                totalMinutes = totalMinutes.add(hours.multiply(BigDecimal.valueOf(60)));
            }
        }
        BigDecimal tauxAbsence = total == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(absences * 100.0 / total).setScale(2, RoundingMode.HALF_UP);

        return new PresenceReportStatsDTO(
                total,
                presencesCount,
                offs,
                conges,
                feries,
                absences,
                tauxAbsence,
                totalMinutes.setScale(0, RoundingMode.HALF_UP)
        );
    }

    private String buildEmployeName(PresenceEmploye presence) {
        if (presence.getEmploye() == null) {
            return null;
        }
        String nom = presence.getEmploye().getNom() != null ? presence.getEmploye().getNom().trim() : "";
        String prenom = presence.getEmploye().getPrenom() != null ? presence.getEmploye().getPrenom().trim() : "";
        return (nom + " " + prenom).trim();
    }

    private String resolveRefSup(PresenceEmploye presence) {
        Map<String, Object> details = parseDetails(presence.getDetails());
        if ("Y".equalsIgnoreCase(String.valueOf(details.get("off")))) {
            return "OFF";
        }
        if ("Y".equalsIgnoreCase(String.valueOf(details.get("conge")))) {
            return "CONGE";
        }
        if ("Y".equalsIgnoreCase(String.valueOf(details.get("ferie")))) {
            return "FERIE";
        }
        if (presence.getStatutPresence() != PresenceEmploye.StatutPresence.VALIDE) {
            return "ABS";
        }
        return "";
    }

    private String resolveHoraireSpecialNuit(PresenceEmploye presence) {
        boolean special = "Y".equalsIgnoreCase(presence.getHoraireSpecial());
        boolean nuit = "Y".equalsIgnoreCase(presence.getNuitPlanifiee());
        return (special && nuit) ? "OUI" : "NON";
    }

    private String resolveFlag(PresenceEmploye presence, String key) {
        Map<String, Object> details = parseDetails(presence.getDetails());
        return "Y".equalsIgnoreCase(String.valueOf(details.get(key))) ? "OUI" : "NON";
    }

    private boolean isAbsent(PresenceEmploye presence) {
        return presence.getStatutPresence() != PresenceEmploye.StatutPresence.VALIDE;
    }

    private BigDecimal resolveNbHeures(PresenceEmploye presence) {
        Map<String, Object> details = parseDetails(presence.getDetails());
        Object nb = details.get("nb_heures_jour");
        if (nb instanceof Number) {
            return new BigDecimal(nb.toString()).setScale(2, RoundingMode.HALF_UP);
        }
        return computeHours(presence);
    }

    private BigDecimal computeHours(PresenceEmploye presence) {
        if (presence.getDateJour() == null || presence.getHeureArrivee() == null || presence.getHeureDepart() == null) {
            return BigDecimal.ZERO;
        }
        try {
            LocalDate dateFin = presence.getDateDepart() != null ? presence.getDateDepart() : presence.getDateJour();
            LocalTime arrivee = LocalTime.parse(presence.getHeureArrivee());
            LocalTime depart = LocalTime.parse(presence.getHeureDepart());
            LocalDateTime start = LocalDateTime.of(presence.getDateJour(), arrivee);
            LocalDateTime end = LocalDateTime.of(dateFin, depart);
            if (end.isBefore(start)) {
                end = end.plusDays(1);
            }
            long minutes = ChronoUnit.MINUTES.between(start, end);
            if (minutes < 0) {
                minutes = 0;
            }
            return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private Map<String, Object> parseDetails(String details) {
        if (details == null || details.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(details, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private void addPdfHeader(Document document, PresenceReportHeaderDTO header, LocalDate dateDebut, LocalDate dateFin) throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 1});

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.addElement(new Paragraph(safe(header.getNomEntreprise()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        left.addElement(new Paragraph(safe(header.getAdresse()), FontFactory.getFont(FontFactory.HELVETICA, 10)));
        left.addElement(new Paragraph("Tel 1: " + safe(header.getTelephone1()), FontFactory.getFont(FontFactory.HELVETICA, 9)));
        left.addElement(new Paragraph("Tel 2: " + safe(header.getTelephone2()), FontFactory.getFont(FontFactory.HELVETICA, 9)));
        left.addElement(new Paragraph("Fax: " + safe(header.getFax()), FontFactory.getFont(FontFactory.HELVETICA, 9)));
        left.addElement(new Paragraph("FEUILLE DE PRESENCES", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        left.addElement(new Paragraph("Dates: " + dateDebut + " / " + dateFin, FontFactory.getFont(FontFactory.HELVETICA, 9)));
        left.addElement(new Paragraph("Sorti le: " + safe(header.getGeneratedAt()), FontFactory.getFont(FontFactory.HELVETICA, 9)));
        if (header.getCriteriaSummary() != null && !header.getCriteriaSummary().isBlank()) {
            left.addElement(new Paragraph("Criteres: " + header.getCriteriaSummary(), FontFactory.getFont(FontFactory.HELVETICA, 9)));
        }
        table.addCell(left);

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        Image logo = resolveLogo(header.getLogoUrl());
        if (logo != null) {
            logo.scaleToFit(80, 80);
            right.addElement(logo);
        }
        table.addCell(right);
        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addPdfTable(Document document, List<PresenceReportRowDTO> rows) throws Exception {
        PdfPTable table = new PdfPTable(11);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1});

        addPdfHeaderCell(table, "EMPLOYES");
        addPdfHeaderCell(table, "DEBUT");
        addPdfHeaderCell(table, "FIN");
        addPdfHeaderCell(table, "NUIT");
        addPdfHeaderCell(table, "HS NUIT");
        addPdfHeaderCell(table, "OFF");
        addPdfHeaderCell(table, "FERIE");
        addPdfHeaderCell(table, "ABS");
        addPdfHeaderCell(table, "NB HE");
        addPdfHeaderCell(table, "REF SUP");
        addPdfHeaderCell(table, "RANG");

        for (PresenceReportRowDTO row : rows) {
            table.addCell(buildCell(row.getCodeEmploye() + " - " + safe(row.getNomEmploye())));
            table.addCell(buildCell(formatDateTime(row.getDateDebut(), row.getHeureDebut())));
            table.addCell(buildCell(formatDateTime(row.getDateFin(), row.getHeureFin())));
            table.addCell(buildCell(safe(row.getNuit())));
            table.addCell(buildCell(safe(row.getHoraireSpecialNuit())));
            table.addCell(buildCell(safe(row.getOff())));
            table.addCell(buildCell(safe(row.getFerie())));
            table.addCell(buildCell(safe(row.getAbsence())));
            table.addCell(buildCell(row.getNbHeures() != null ? row.getNbHeures().toString() : ""));
            table.addCell(buildCell(safe(row.getRefSup())));
            table.addCell(buildCell(String.valueOf(row.getRang())));
        }
        document.add(table);
    }

    private void addPdfStats(Document document, PresenceReportStatsDTO stats) throws Exception {
        document.add(Chunk.NEWLINE);
        Paragraph p = new Paragraph();
        p.add("TOTAL: " + stats.getTotal() + "   ");
        p.add("PRESENCES: " + stats.getPresences() + "   ");
        p.add("OFFS: " + stats.getOffs() + "   ");
        p.add("CONGES: " + stats.getConges() + "   ");
        p.add("FERIES: " + stats.getFeries() + "   ");
        p.add("ABSENCES: " + stats.getAbsences() + "   ");
        p.add("% ABSENCE: " + stats.getTauxAbsence());
        document.add(p);
        Paragraph p2 = new Paragraph("TOTAL SUPPLEMENTAIRE (MINUTES): " + stats.getTotalSupplementaireMinutes());
        document.add(p2);
    }

    private int writeXlsxHeader(Sheet sheet, PresenceReportHeaderDTO header, LocalDate dateDebut, LocalDate dateFin, int rowIndex) {
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(safe(header.getNomEntreprise()));
        Row row2 = sheet.createRow(rowIndex++);
        row2.createCell(0).setCellValue(safe(header.getAdresse()));
        Row row3 = sheet.createRow(rowIndex++);
        row3.createCell(0).setCellValue("Tel 1: " + safe(header.getTelephone1()));
        Row row4 = sheet.createRow(rowIndex++);
        row4.createCell(0).setCellValue("Tel 2: " + safe(header.getTelephone2()));
        Row row5 = sheet.createRow(rowIndex++);
        row5.createCell(0).setCellValue("Fax: " + safe(header.getFax()));
        Row row6 = sheet.createRow(rowIndex++);
        row6.createCell(0).setCellValue("FEUILLE DE PRESENCES");
        Row row7 = sheet.createRow(rowIndex++);
        row7.createCell(0).setCellValue("Dates: " + dateDebut + " / " + dateFin);
        Row row8 = sheet.createRow(rowIndex++);
        row8.createCell(0).setCellValue("Sorti le: " + safe(header.getGeneratedAt()));
        if (header.getCriteriaSummary() != null && !header.getCriteriaSummary().isBlank()) {
            Row row9 = sheet.createRow(rowIndex++);
            row9.createCell(0).setCellValue("Criteres: " + header.getCriteriaSummary());
        }
        addXlsxLogo(sheet, header.getLogoUrl());
        return rowIndex;
    }

    private int writeXlsxTable(Sheet sheet, List<PresenceReportRowDTO> rows, int rowIndex) {
        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"EMPLOYES", "DEBUT", "FIN", "NUIT", "HS NUIT", "OFF", "FERIE", "ABS", "NB HE", "REF SUP", "RANG"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        for (PresenceReportRowDTO row : rows) {
            Row r = sheet.createRow(rowIndex++);
            r.createCell(0).setCellValue(row.getCodeEmploye() + " - " + safe(row.getNomEmploye()));
            r.createCell(1).setCellValue(formatDateTime(row.getDateDebut(), row.getHeureDebut()));
            r.createCell(2).setCellValue(formatDateTime(row.getDateFin(), row.getHeureFin()));
            r.createCell(3).setCellValue(safe(row.getNuit()));
            r.createCell(4).setCellValue(safe(row.getHoraireSpecialNuit()));
            r.createCell(5).setCellValue(safe(row.getOff()));
            r.createCell(6).setCellValue(safe(row.getFerie()));
            r.createCell(7).setCellValue(safe(row.getAbsence()));
            r.createCell(8).setCellValue(row.getNbHeures() != null ? row.getNbHeures().toString() : "");
            r.createCell(9).setCellValue(safe(row.getRefSup()));
            r.createCell(10).setCellValue(row.getRang() != null ? row.getRang() : 0);
        }
        return rowIndex;
    }

    private void writeXlsxStats(Sheet sheet, PresenceReportStatsDTO stats, int rowIndex) {
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("TOTAL");
        row.createCell(1).setCellValue(stats.getTotal());
        row.createCell(2).setCellValue("PRESENCES");
        row.createCell(3).setCellValue(stats.getPresences());
        row.createCell(4).setCellValue("OFFS");
        row.createCell(5).setCellValue(stats.getOffs());
        row.createCell(6).setCellValue("CONGES");
        row.createCell(7).setCellValue(stats.getConges());
        Row row2 = sheet.createRow(rowIndex++);
        row2.createCell(0).setCellValue("FERIES");
        row2.createCell(1).setCellValue(stats.getFeries());
        row2.createCell(2).setCellValue("ABSENCES");
        row2.createCell(3).setCellValue(stats.getAbsences());
        row2.createCell(4).setCellValue("% ABSENCE");
        row2.createCell(5).setCellValue(stats.getTauxAbsence() != null ? stats.getTauxAbsence().toString() : "0");
        Row row3 = sheet.createRow(rowIndex);
        row3.createCell(0).setCellValue("TOTAL SUPPLEMENTAIRE (MINUTES)");
        row3.createCell(1).setCellValue(stats.getTotalSupplementaireMinutes() != null ? stats.getTotalSupplementaireMinutes().toString() : "0");
    }

    private void addDocxHeader(XWPFDocument document, PresenceReportHeaderDTO header, LocalDate dateDebut, LocalDate dateFin) {
        XWPFTable table = document.createTable(1, 2);
        XWPFParagraph left = table.getRow(0).getCell(0).addParagraph();
        XWPFRun run = left.createRun();
        run.setBold(true);
        run.setText(safe(header.getNomEntreprise()));
        left.createRun().addBreak();
        left.createRun().setText(safe(header.getAdresse()));
        left.createRun().addBreak();
        left.createRun().setText("Tel 1: " + safe(header.getTelephone1()));
        left.createRun().addBreak();
        left.createRun().setText("Tel 2: " + safe(header.getTelephone2()));
        left.createRun().addBreak();
        left.createRun().setText("Fax: " + safe(header.getFax()));
        left.createRun().addBreak();
        left.createRun().setText("FEUILLE DE PRESENCES");
        left.createRun().addBreak();
        left.createRun().setText("Dates: " + dateDebut + " / " + dateFin);
        left.createRun().addBreak();
        left.createRun().setText("Sorti le: " + safe(header.getGeneratedAt()));
        if (header.getCriteriaSummary() != null && !header.getCriteriaSummary().isBlank()) {
            left.createRun().addBreak();
            left.createRun().setText("Criteres: " + header.getCriteriaSummary());
        }

        if (header.getLogoUrl() != null && !header.getLogoUrl().isBlank()) {
            try (InputStream is = new URL(header.getLogoUrl()).openStream()) {
                byte[] imgBytes = is.readAllBytes();
                XWPFParagraph right = table.getRow(0).getCell(1).addParagraph();
                XWPFRun imgRun = right.createRun();
                imgRun.addPicture(new java.io.ByteArrayInputStream(imgBytes), org.apache.poi.xwpf.usermodel.Document.PICTURE_TYPE_PNG, "logo",
                        Units.toEMU(120), Units.toEMU(60));
            } catch (Exception ignored) {
            }
        }
    }

    private void addDocxTable(XWPFDocument document, List<PresenceReportRowDTO> rows) {
        XWPFTable table = document.createTable(rows.size() + 1, 11);
        String[] headers = {"EMPLOYES", "DEBUT", "FIN", "NUIT", "HS NUIT", "OFF", "FERIE", "ABS", "NB HE", "REF SUP", "RANG"};
        for (int i = 0; i < headers.length; i++) {
            table.getRow(0).getCell(i).setText(headers[i]);
        }
        for (int i = 0; i < rows.size(); i++) {
            PresenceReportRowDTO row = rows.get(i);
            int r = i + 1;
            table.getRow(r).getCell(0).setText(row.getCodeEmploye() + " - " + safe(row.getNomEmploye()));
            table.getRow(r).getCell(1).setText(formatDateTime(row.getDateDebut(), row.getHeureDebut()));
            table.getRow(r).getCell(2).setText(formatDateTime(row.getDateFin(), row.getHeureFin()));
            table.getRow(r).getCell(3).setText(safe(row.getNuit()));
            table.getRow(r).getCell(4).setText(safe(row.getHoraireSpecialNuit()));
            table.getRow(r).getCell(5).setText(safe(row.getOff()));
            table.getRow(r).getCell(6).setText(safe(row.getFerie()));
            table.getRow(r).getCell(7).setText(safe(row.getAbsence()));
            table.getRow(r).getCell(8).setText(row.getNbHeures() != null ? row.getNbHeures().toString() : "");
            table.getRow(r).getCell(9).setText(safe(row.getRefSup()));
            table.getRow(r).getCell(10).setText(row.getRang() != null ? row.getRang().toString() : "");
        }
    }

    private void addDocxStats(XWPFDocument document, PresenceReportStatsDTO stats) {
        XWPFParagraph p = document.createParagraph();
        p.createRun().setText("TOTAL: " + stats.getTotal() + " | PRESENCES: " + stats.getPresences() +
                " | OFFS: " + stats.getOffs() + " | CONGES: " + stats.getConges() +
                " | FERIES: " + stats.getFeries() + " | ABSENCES: " + stats.getAbsences() +
                " | % ABSENCE: " + stats.getTauxAbsence());
    }

    private Image resolveLogo(String logoUrl) {
        if (logoUrl == null || logoUrl.isBlank()) {
            return null;
        }
        try (InputStream is = new URL(logoUrl).openStream()) {
            java.awt.Image awt = ImageIO.read(is);
            if (awt == null) {
                return null;
            }
            return Image.getInstance(awt, null);
        } catch (Exception ex) {
            return null;
        }
    }

    private void addPdfHeaderCell(PdfPTable table, String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        table.addCell(cell);
    }

    private PdfPCell buildCell(String value) {
        return new PdfPCell(new Phrase(value != null ? value : "", FontFactory.getFont(FontFactory.HELVETICA, 8)));
    }

    private void autoSize(Sheet sheet, int cols) {
        for (int i = 0; i < cols; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void addXlsxLogo(Sheet sheet, String logoUrl) {
        if (logoUrl == null || logoUrl.isBlank()) {
            return;
        }
        try (InputStream is = new URL(logoUrl).openStream()) {
            java.awt.image.BufferedImage image = ImageIO.read(is);
            if (image == null) {
                return;
            }
            ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
            ImageIO.write(image, "png", imgOut);
            int pictureIdx = sheet.getWorkbook().addPicture(imgOut.toByteArray(), Workbook.PICTURE_TYPE_PNG);
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            ClientAnchor anchor = sheet.getWorkbook().getCreationHelper().createClientAnchor();
            anchor.setCol1(6);
            anchor.setRow1(0);
            Picture pict = drawing.createPicture(anchor, pictureIdx);
            pict.resize(1.2);
        } catch (Exception ignored) {
        }
    }

    private String buildCriteriaSummary(LocalDate dateDebut,
                                        LocalDate dateFin,
                                        Long entrepriseId,
                                        String nuit,
                                        Long uniteOrganisationnelleId,
                                        Long typeEmployeId,
                                        Long gestionnaireId,
                                        Long employeId,
                                        String actif,
                                        List<Long> regimePaieIds) {
        List<String> parts = new ArrayList<>();
        parts.add("Dates: " + dateDebut + " / " + dateFin);

        if (entrepriseId != null) {
            Entreprise ent = entrepriseRepository.findById(entrepriseId).orElse(null);
            parts.add("Entreprise: " + (ent != null ? safe(ent.getNomEntreprise()) : entrepriseId));
        }
        if (nuit != null) {
            parts.add("Nuit: " + ("Y".equalsIgnoreCase(nuit) ? "Oui" : "Non"));
        }
        if (uniteOrganisationnelleId != null) {
            UniteOrganisationnelle unite = uniteOrganisationnelleRepository.findById(uniteOrganisationnelleId).orElse(null);
            parts.add("Unite: " + (unite != null ? (safe(unite.getCode()) + " - " + safe(unite.getNom())) : uniteOrganisationnelleId));
        }
        if (typeEmployeId != null) {
            TypeEmploye type = typeEmployeRepository.findById(typeEmployeId).orElse(null);
            parts.add("Type employe: " + (type != null ? safe(type.getDescription()) : typeEmployeId));
        }
        if (gestionnaireId != null) {
            Employe sup = employeRepository.findById(gestionnaireId).orElse(null);
            parts.add("Superviseur: " + (sup != null ? safe(sup.getCodeEmploye()) + " - " + safe(sup.getNom()) + " " + safe(sup.getPrenom()) : gestionnaireId));
        }
        if (employeId != null) {
            Employe emp = employeRepository.findById(employeId).orElse(null);
            parts.add("Employe: " + (emp != null ? safe(emp.getCodeEmploye()) + " - " + safe(emp.getNom()) + " " + safe(emp.getPrenom()) : employeId));
        }
        if (actif != null) {
            parts.add("Actif: " + ("Y".equalsIgnoreCase(actif) ? "Oui" : "Non"));
        }
        if (regimePaieIds != null && !regimePaieIds.isEmpty()) {
            List<String> regimes = new ArrayList<>();
            for (Long id : regimePaieIds) {
                RegimePaie rp = regimePaieRepository.findById(id).orElse(null);
                regimes.add(rp != null ? safe(rp.getCodeRegimePaie()) : String.valueOf(id));
            }
            parts.add("Regime paie: " + String.join(", ", regimes));
        }
        return String.join(" | ", parts);
    }

    private String formatDateTime(LocalDate date, String heure) {
        if (date == null) {
            return "";
        }
        if (heure == null || heure.isBlank()) {
            return date.toString();
        }
        return date + " " + heure;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
