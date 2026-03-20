package com.rsoft.hurmanagement.hurmasterdata.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.rsoft.hurmanagement.hurmasterdata.dto.report.FeuillePayrollReportResponseDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.report.FeuillePayrollReportRowDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.report.PresenceReportHeaderDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.Employe;
import com.rsoft.hurmanagement.hurmasterdata.entity.Entreprise;
import com.rsoft.hurmanagement.hurmasterdata.entity.Payroll;
import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.RegimePaie;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.UniteOrganisationnelle;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EntrepriseRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RegimePaieRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.TypeEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.UniteOrganisationnelleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
public class FeuillePayrollReportService {

    private final PayrollEmployeRepository payrollEmployeRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final UniteOrganisationnelleRepository uniteOrganisationnelleRepository;
    private final TypeEmployeRepository typeEmployeRepository;
    private final RegimePaieRepository regimePaieRepository;
    private final EmployeRepository employeRepository;

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional(readOnly = true)
    public FeuillePayrollReportResponseDTO buildReport(LocalDate dateDebut,
                                                       LocalDate dateFin,
                                                       Long entrepriseId,
                                                       String nuit,
                                                       List<Long> uniteOrganisationnelleId,
                                                       List<Long> typeEmployeId,
                                                       Long gestionnaireId,
                                                       Long employeId,
                                                       String actif,
                                                       List<Long> regimePaieIds,
                                                       String statut) {
        if (dateDebut == null || dateFin == null) {
            throw new RuntimeException("presenceReport.error.dateRequired");
        }
        if (dateFin.isBefore(dateDebut)) {
            throw new RuntimeException("presenceReport.error.dateInvalid");
        }

        List<Long> safeRegimeIds = (regimePaieIds == null || regimePaieIds.isEmpty()) ? null : regimePaieIds;
        List<Long> safeUniteIds = (uniteOrganisationnelleId == null || uniteOrganisationnelleId.isEmpty()) ? null : uniteOrganisationnelleId;
        List<Long> safeTypeIds = (typeEmployeId == null || typeEmployeId.isEmpty()) ? null : typeEmployeId;
        String nightFilter = (nuit != null && !nuit.isBlank()) ? nuit.trim().toUpperCase(Locale.ROOT) : null;
        String actifFilter = (actif != null && !actif.isBlank()) ? actif.trim().toUpperCase(Locale.ROOT) : null;
        Payroll.StatutPayroll statutFilter = parseStatut(statut);

        List<PayrollEmploye> payrollEmployes = payrollEmployeRepository.findForFeuillePayrollReport(
                dateDebut,
                dateFin,
                entrepriseId,
                nightFilter,
                safeUniteIds,
                safeTypeIds,
                gestionnaireId,
                employeId,
                actifFilter,
                safeRegimeIds,
                statutFilter
        );

        List<FeuillePayrollReportRowDTO> rows = new ArrayList<>();
        for (PayrollEmploye pe : payrollEmployes) {
            Employe e = pe.getEmploye();
            rows.add(new FeuillePayrollReportRowDTO(
                    e != null ? e.getCodeEmploye() : null,
                    e != null ? e.getNom() : null,
                    e != null ? e.getPrenom() : null,
                    e != null ? e.getDatePremiereEmbauche() : null,
                    pe.getMontantSalaireBase(),
                    pe.getMontantSupplementaire(),
                    pe.getMontantAutreRevenu(),
                    pe.getMontantDeductions(),
                    pe.getMontantRecouvrements(),
                    pe.getMontantSanctions(),
                    pe.getMontantBrut(),
                    pe.getMontantNetAPayer()
            ));
        }

        PresenceReportHeaderDTO header = buildHeader(entrepriseId);
        header.setGeneratedAt(LocalDateTime.now().format(DATE_TIME_FORMAT));
        header.setCriteriaSummary(buildCriteriaSummary(
                dateDebut,
                dateFin,
                entrepriseId,
                nightFilter,
                safeUniteIds,
                safeTypeIds,
                gestionnaireId,
                employeId,
                actifFilter,
                safeRegimeIds,
                statutFilter
        ));

        return new FeuillePayrollReportResponseDTO(header, rows);
    }

    public byte[] exportPdf(FeuillePayrollReportResponseDTO report, Integer textSize, String orientation) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            float fontSize = resolveExportFontSize(textSize);
            Document document = new Document(resolvePdfPageSize(orientation), 16, 16, 16, 16);
            PdfWriter.getInstance(document, out);
            document.open();
            addPdfHeader(document, report.getHeader(), fontSize);
            addPdfTable(document, report.getRows(), fontSize);
            addPdfCriteria(document, report.getHeader(), fontSize);
            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("payrollSheetReport.error.pdf", ex);
        }
    }

    public byte[] exportXlsx(FeuillePayrollReportResponseDTO report, Integer textSize, String orientation) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            short fontSize = (short) Math.round(resolveExportFontSize(textSize));
            Sheet sheet = workbook.createSheet("Feuille payroll");
            applyXlsxOrientation(sheet, orientation);
            int rowIndex = writeXlsxHeader(sheet, report.getHeader(), 12, fontSize);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) Math.max(6, fontSize));
            headerStyle.setFont(headerFont);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle bodyStyle = workbook.createCellStyle();
            Font bodyFont = workbook.createFont();
            bodyFont.setFontHeightInPoints((short) Math.max(6, fontSize));
            bodyStyle.setFont(bodyFont);
            bodyStyle.setBorderBottom(BorderStyle.THIN);

            String[] headers = {"CODE", "NOM", "PRENOM", "DATE EMBAUCHE", "SAL BASE", "SUPPL", "AUTRE REV", "BRUT", "DEDUCTIONS", "RECOUVR", "SANCTIONS", "NET A PAYER"};
            Row headerRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < headers.length; i++) {
                setCell(headerRow, i, headers[i], headerStyle);
            }
            for (FeuillePayrollReportRowDTO row : report.getRows()) {
                Row r = sheet.createRow(rowIndex++);
                setCell(r, 0, safe(row.getCodeEmploye()), bodyStyle);
                setCell(r, 1, safe(row.getNomEmploye()), bodyStyle);
                setCell(r, 2, safe(row.getPrenomEmploye()), bodyStyle);
                setCell(r, 3, row.getDatePremiereEmbauche() != null ? row.getDatePremiereEmbauche().toString() : "", bodyStyle);
                setCell(r, 4, toAmount(row.getMontantSalaireBase()), bodyStyle);
                setCell(r, 5, toAmount(row.getMontantSupplementaire()), bodyStyle);
                setCell(r, 6, toAmount(row.getMontantAutreRevenu()), bodyStyle);
                setCell(r, 7, toAmount(row.getMontantBrut()), bodyStyle);
                setCell(r, 8, toAmount(row.getMontantDeductions()), bodyStyle);
                setCell(r, 9, toAmount(row.getMontantRecouvrements()), bodyStyle);
                setCell(r, 10, toAmount(row.getMontantSanctions()), bodyStyle);
                setCell(r, 11, toAmount(row.getMontantNetAPayer()), bodyStyle);
            }
            rowIndex = writeXlsxCriteria(sheet, report.getHeader(), rowIndex, fontSize);
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("payrollSheetReport.error.xlsx", ex);
        }
    }

    public byte[] exportDocx(FeuillePayrollReportResponseDTO report, Integer textSize, String orientation) {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int fontSize = Math.max(6, Math.round(resolveExportFontSize(textSize)));
            applyDocxOrientation(document, orientation);
            addDocxHeader(document, report.getHeader(), fontSize);
            XWPFTable table = document.createTable(report.getRows().size() + 1, 12);
            String[] headers = {"CODE", "NOM", "PRENOM", "DATE EMBAUCHE", "SAL BASE", "SUPPL", "AUTRE REV", "BRUT", "DEDUCTIONS", "RECOUVR", "SANCTIONS", "NET A PAYER"};
            for (int i = 0; i < headers.length; i++) {
                table.getRow(0).getCell(i).setText(headers[i]);
            }
            for (int i = 0; i < report.getRows().size(); i++) {
                FeuillePayrollReportRowDTO row = report.getRows().get(i);
                int r = i + 1;
                table.getRow(r).getCell(0).setText(safe(row.getCodeEmploye()));
                table.getRow(r).getCell(1).setText(safe(row.getNomEmploye()));
                table.getRow(r).getCell(2).setText(safe(row.getPrenomEmploye()));
                table.getRow(r).getCell(3).setText(row.getDatePremiereEmbauche() != null ? row.getDatePremiereEmbauche().toString() : "");
                table.getRow(r).getCell(4).setText(toAmount(row.getMontantSalaireBase()));
                table.getRow(r).getCell(5).setText(toAmount(row.getMontantSupplementaire()));
                table.getRow(r).getCell(6).setText(toAmount(row.getMontantAutreRevenu()));
                table.getRow(r).getCell(7).setText(toAmount(row.getMontantBrut()));
                table.getRow(r).getCell(8).setText(toAmount(row.getMontantDeductions()));
                table.getRow(r).getCell(9).setText(toAmount(row.getMontantRecouvrements()));
                table.getRow(r).getCell(10).setText(toAmount(row.getMontantSanctions()));
                table.getRow(r).getCell(11).setText(toAmount(row.getMontantNetAPayer()));
            }
            addDocxCriteria(document, report.getHeader(), fontSize);
            document.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("payrollSheetReport.error.docx", ex);
        }
    }

    private Payroll.StatutPayroll parseStatut(String statut) {
        if (statut == null || statut.isBlank()) {
            return null;
        }
        try {
            return Payroll.StatutPayroll.valueOf(statut.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            return null;
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
                entreprise.getPays(),
                entreprise.getCodePostal(),
                entreprise.getTelephone1(),
                entreprise.getTelephone2(),
                entreprise.getTelephone3(),
                entreprise.getFax(),
                entreprise.getCourriel(),
                entreprise.getLogoUrl(),
                null,
                null
        );
    }

    private String buildCriteriaSummary(LocalDate dateDebut,
                                        LocalDate dateFin,
                                        Long entrepriseId,
                                        String nuit,
                                        List<Long> uniteIds,
                                        List<Long> typeIds,
                                        Long gestionnaireId,
                                        Long employeId,
                                        String actif,
                                        List<Long> regimeIds,
                                        Payroll.StatutPayroll statut) {
        List<String> parts = new ArrayList<>();
        parts.add("Dates paie (date fin): " + dateDebut + " / " + dateFin);
        if (entrepriseId != null) {
            Entreprise ent = entrepriseRepository.findById(entrepriseId).orElse(null);
            parts.add("Entreprise: " + (ent != null ? safe(ent.getNomEntreprise()) : entrepriseId));
        }
        if (statut != null) {
            parts.add("Statut payroll: " + statut.name());
        }
        if (nuit != null) {
            parts.add("Nuit: " + ("Y".equalsIgnoreCase(nuit) ? "Oui" : "Non"));
        }
        if (uniteIds != null && !uniteIds.isEmpty()) {
            List<String> values = new ArrayList<>();
            for (Long id : uniteIds) {
                UniteOrganisationnelle unite = uniteOrganisationnelleRepository.findById(id).orElse(null);
                values.add(unite != null ? safe(unite.getCode()) + " - " + safe(unite.getNom()) : String.valueOf(id));
            }
            parts.add("Unite: " + String.join(", ", values));
        }
        if (typeIds != null && !typeIds.isEmpty()) {
            List<String> values = new ArrayList<>();
            for (Long id : typeIds) {
                TypeEmploye type = typeEmployeRepository.findById(id).orElse(null);
                values.add(type != null ? safe(type.getDescription()) : String.valueOf(id));
            }
            parts.add("Type employe: " + String.join(", ", values));
        }
        if (gestionnaireId != null) {
            Employe manager = employeRepository.findById(gestionnaireId).orElse(null);
            parts.add("Superviseur: " + (manager != null ? safe(manager.getCodeEmploye()) : gestionnaireId));
        }
        if (employeId != null) {
            Employe employe = employeRepository.findById(employeId).orElse(null);
            parts.add("Employe: " + (employe != null ? safe(employe.getCodeEmploye()) : employeId));
        }
        if (actif != null) {
            parts.add("Actif: " + ("Y".equalsIgnoreCase(actif) ? "Oui" : "Non"));
        }
        if (regimeIds != null && !regimeIds.isEmpty()) {
            List<String> values = new ArrayList<>();
            for (Long id : regimeIds) {
                RegimePaie reg = regimePaieRepository.findById(id).orElse(null);
                values.add(reg != null ? safe(reg.getCodeRegimePaie()) : String.valueOf(id));
            }
            parts.add("Regime paie: " + String.join(", ", values));
        }
        return String.join(" | ", parts);
    }

    private void addPdfHeader(Document document, PresenceReportHeaderDTO header, float fontSize) throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 1});

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.setHorizontalAlignment(Element.ALIGN_CENTER);
        left.addElement(centeredParagraph(safe(header.getNomEntreprise()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, fontSize + 2)));
        addIfNotBlank(left, centeredParagraph(buildAddressLine(header), FontFactory.getFont(FontFactory.HELVETICA, fontSize)));
        addIfNotBlank(left, centeredParagraph(buildTelephonesLine(header), FontFactory.getFont(FontFactory.HELVETICA, fontSize)));
        addIfNotBlank(left, centeredParagraph(buildFaxLine(header), FontFactory.getFont(FontFactory.HELVETICA, fontSize)));
        left.addElement(centeredParagraph("FEUILLE DE PAIE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, fontSize + 1)));
        left.addElement(new Paragraph("Sorti le: " + safe(header.getGeneratedAt()), FontFactory.getFont(FontFactory.HELVETICA, fontSize)));
        table.addCell(left);

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        right.setVerticalAlignment(Element.ALIGN_TOP);
        Image logo = resolveLogoAsPdfImage(header.getLogoUrl());
        if (logo != null) {
            logo.scaleToFit(80, 80);
            logo.setAlignment(Image.ALIGN_RIGHT);
            right.addElement(logo);
        }
        table.addCell(right);
        document.add(table);
        addPdfSectionSeparator(document);
        document.add(com.lowagie.text.Chunk.NEWLINE);
    }

    private void addPdfTable(Document document, List<FeuillePayrollReportRowDTO> rows, float fontSize) throws Exception {
        PdfPTable table = new PdfPTable(12);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 1.4f, 1.4f, 1.1f, 1.1f, 1f, 1f, 1f, 1f, 1f, 1.1f, 1.1f});
        String[] headers = {"CODE", "NOM", "PRENOM", "DATE EMBAUCHE", "SAL BASE", "SUPPL", "AUTRE REV", "BRUT", "DEDUCTIONS", "RECOUVR", "SANCTIONS", "NET A PAYER"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, fontSize)));
            cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
            table.addCell(cell);
        }
        for (FeuillePayrollReportRowDTO row : rows) {
            table.addCell(bodyCell(safe(row.getCodeEmploye()), fontSize));
            table.addCell(bodyCell(safe(row.getNomEmploye()), fontSize));
            table.addCell(bodyCell(safe(row.getPrenomEmploye()), fontSize));
            table.addCell(bodyCell(row.getDatePremiereEmbauche() != null ? row.getDatePremiereEmbauche().toString() : "", fontSize));
            table.addCell(bodyCell(toAmount(row.getMontantSalaireBase()), fontSize));
            table.addCell(bodyCell(toAmount(row.getMontantSupplementaire()), fontSize));
            table.addCell(bodyCell(toAmount(row.getMontantAutreRevenu()), fontSize));
            table.addCell(bodyCell(toAmount(row.getMontantBrut()), fontSize));
            table.addCell(bodyCell(toAmount(row.getMontantDeductions()), fontSize));
            table.addCell(bodyCell(toAmount(row.getMontantRecouvrements()), fontSize));
            table.addCell(bodyCell(toAmount(row.getMontantSanctions()), fontSize));
            table.addCell(bodyCell(toAmount(row.getMontantNetAPayer()), fontSize));
        }
        document.add(table);
    }

    private void addPdfCriteria(Document document, PresenceReportHeaderDTO header, float fontSize) throws Exception {
        if (header.getCriteriaSummary() == null || header.getCriteriaSummary().isBlank()) {
            return;
        }
        document.add(com.lowagie.text.Chunk.NEWLINE);
        document.add(new Paragraph("CRITERES: " + header.getCriteriaSummary(), FontFactory.getFont(FontFactory.HELVETICA, fontSize)));
    }

    private int writeXlsxHeader(Sheet sheet, PresenceReportHeaderDTO header, int totalCols, short fontSize) {
        Workbook workbook = sheet.getWorkbook();
        CellStyle centerStyle = workbook.createCellStyle();
        centerStyle.setAlignment(HorizontalAlignment.CENTER);
        Font headerFont = workbook.createFont();
        headerFont.setFontHeightInPoints((short) Math.max(4, fontSize));
        centerStyle.setFont(headerFont);
        int rowIndex = 0;
        rowIndex = writeCenteredMergedRow(sheet, rowIndex, safe(header.getNomEntreprise()), centerStyle, totalCols - 1);
        rowIndex = writeCenteredMergedRowIfNotBlank(sheet, rowIndex, buildAddressLine(header), centerStyle, totalCols - 1);
        rowIndex = writeCenteredMergedRowIfNotBlank(sheet, rowIndex, buildTelephonesLine(header), centerStyle, totalCols - 1);
        rowIndex = writeCenteredMergedRowIfNotBlank(sheet, rowIndex, buildFaxLine(header), centerStyle, totalCols - 1);
        rowIndex = writeCenteredMergedRow(sheet, rowIndex, "FEUILLE DE PAIE", centerStyle, totalCols - 1);

        Row rowSortie = sheet.createRow(rowIndex++);
        Cell sortieCell = rowSortie.createCell(0);
        sortieCell.setCellValue("Sorti le: " + safe(header.getGeneratedAt()));
        CellStyle sortieStyle = workbook.createCellStyle();
        Font sortieFont = workbook.createFont();
        sortieFont.setFontHeightInPoints((short) Math.max(4, fontSize));
        sortieStyle.setFont(sortieFont);
        sortieCell.setCellStyle(sortieStyle);
        Row separatorRow = sheet.createRow(rowIndex++);
        CellStyle separatorStyle = workbook.createCellStyle();
        separatorStyle.setBorderBottom(BorderStyle.THIN);
        for (int col = 0; col < totalCols; col++) {
            Cell cell = separatorRow.createCell(col);
            cell.setCellStyle(separatorStyle);
        }
        addXlsxLogo(sheet, header.getLogoUrl(), Math.max(0, totalCols - 2), 0);
        return rowIndex;
    }

    private int writeXlsxCriteria(Sheet sheet, PresenceReportHeaderDTO header, int rowIndex, short fontSize) {
        if (header.getCriteriaSummary() == null || header.getCriteriaSummary().isBlank()) {
            return rowIndex;
        }
        Workbook workbook = sheet.getWorkbook();
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) Math.max(4, fontSize));
        style.setFont(font);
        Row row = sheet.createRow(rowIndex++);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue("CRITERES");
        labelCell.setCellStyle(style);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(header.getCriteriaSummary());
        valueCell.setCellStyle(style);
        return rowIndex;
    }

    private void addDocxHeader(XWPFDocument document, PresenceReportHeaderDTO header, int fontSize) {
        XWPFTable table = document.createTable(1, 2);
        XWPFParagraph left = table.getRow(0).getCell(0).addParagraph();
        left.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = left.createRun();
        run.setBold(true);
        run.setFontSize(fontSize + 2);
        run.setText(safe(header.getNomEntreprise()));
        run.setBold(false);
        addDocxCenteredIfNotBlank(left, buildAddressLine(header), fontSize);
        addDocxCenteredIfNotBlank(left, buildTelephonesLine(header), fontSize);
        addDocxCenteredIfNotBlank(left, buildFaxLine(header), fontSize);
        addDocxCenteredIfNotBlank(left, "FEUILLE DE PAIE", fontSize + 1);

        XWPFParagraph leftInfo = table.getRow(0).getCell(0).addParagraph();
        leftInfo.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun infoRun = leftInfo.createRun();
        infoRun.setFontSize(fontSize);
        infoRun.setText("Sorti le: " + safe(header.getGeneratedAt()));

        XWPFParagraph right = table.getRow(0).getCell(1).addParagraph();
        right.setAlignment(ParagraphAlignment.RIGHT);
        byte[] logoBytes = resolveLogoBytes(header.getLogoUrl());
        if (logoBytes != null) {
            try {
                XWPFRun imgRun = right.createRun();
                imgRun.addPicture(new ByteArrayInputStream(logoBytes), detectPictureType(header.getLogoUrl(), logoBytes), "logo",
                        Units.toEMU(120), Units.toEMU(60));
            } catch (Exception ignored) {
            }
        }
        XWPFParagraph separator = document.createParagraph();
        separator.setBorderBottom(Borders.SINGLE);
    }

    private void addDocxCriteria(XWPFDocument document, PresenceReportHeaderDTO header, int fontSize) {
        if (header.getCriteriaSummary() == null || header.getCriteriaSummary().isBlank()) {
            return;
        }
        XWPFParagraph p = document.createParagraph();
        XWPFRun run = p.createRun();
        run.setFontSize(fontSize);
        run.setText("CRITERES: " + header.getCriteriaSummary());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String toAmount(java.math.BigDecimal value) {
        return value != null ? value.setScale(2, java.math.RoundingMode.HALF_UP).toString() : "0.00";
    }

    private float resolveExportFontSize(Integer textSize) {
        if (textSize == null) {
            return 10f;
        }
        return Math.max(4f, Math.min(17f, textSize));
    }

    private Rectangle resolvePdfPageSize(String orientation) {
        OrientationSpec spec = parseOrientation(orientation);
        Rectangle base = switch (spec.paper) {
            case "LEGAL" -> PageSize.LEGAL;
            case "A4" -> PageSize.A4;
            default -> PageSize.LETTER;
        };
        return spec.landscape ? base.rotate() : base;
    }

    private void applyXlsxOrientation(Sheet sheet, String orientation) {
        OrientationSpec spec = parseOrientation(orientation);
        org.apache.poi.ss.usermodel.PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setLandscape(spec.landscape);
        short paperSize = switch (spec.paper) {
            case "LEGAL" -> org.apache.poi.ss.usermodel.PrintSetup.LEGAL_PAPERSIZE;
            case "A4" -> org.apache.poi.ss.usermodel.PrintSetup.A4_PAPERSIZE;
            default -> org.apache.poi.ss.usermodel.PrintSetup.LETTER_PAPERSIZE;
        };
        printSetup.setPaperSize(paperSize);
        sheet.setFitToPage(true);
    }

    private void applyDocxOrientation(XWPFDocument document, String orientation) {
        OrientationSpec spec = parseOrientation(orientation);
        CTSectPr sectPr = document.getDocument().getBody().isSetSectPr()
                ? document.getDocument().getBody().getSectPr()
                : document.getDocument().getBody().addNewSectPr();
        CTPageSz pageSz = sectPr.isSetPgSz() ? sectPr.getPgSz() : sectPr.addNewPgSz();
        long portraitW;
        long portraitH;
        switch (spec.paper) {
            case "LEGAL" -> {
                portraitW = 12240;
                portraitH = 20160;
            }
            case "A4" -> {
                portraitW = 11906;
                portraitH = 16838;
            }
            default -> {
                portraitW = 12240;
                portraitH = 15840;
            }
        }
        long w = spec.landscape ? portraitH : portraitW;
        long h = spec.landscape ? portraitW : portraitH;
        pageSz.setW(BigInteger.valueOf(w));
        pageSz.setH(BigInteger.valueOf(h));
        pageSz.setOrient(spec.landscape ? STPageOrientation.LANDSCAPE : STPageOrientation.PORTRAIT);
    }

    private OrientationSpec parseOrientation(String orientation) {
        if (orientation == null || orientation.isBlank()) {
            return new OrientationSpec("LETTER", true);
        }
        String value = orientation.trim().toUpperCase(Locale.ROOT);
        String paper = "LETTER";
        if (value.startsWith("LEGAL")) {
            paper = "LEGAL";
        } else if (value.startsWith("A4")) {
            paper = "A4";
        }
        boolean landscape = value.endsWith("LANDSCAPE");
        return new OrientationSpec(paper, landscape);
    }

    private int writeCenteredMergedRow(Sheet sheet, int rowIndex, String text, CellStyle style, int lastCol) {
        Row row = sheet.createRow(rowIndex);
        Cell cell = row.createCell(0);
        cell.setCellValue(text);
        cell.setCellStyle(style);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIndex, rowIndex, 0, Math.max(0, lastCol)));
        return rowIndex + 1;
    }

    private int writeCenteredMergedRowIfNotBlank(Sheet sheet, int rowIndex, String text, CellStyle style, int lastCol) {
        if (text == null || text.isBlank()) {
            return rowIndex;
        }
        return writeCenteredMergedRow(sheet, rowIndex, text, style, lastCol);
    }

    private void setCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private Paragraph centeredParagraph(String text, com.lowagie.text.Font font) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        return paragraph;
    }

    private void addIfNotBlank(PdfPCell cell, Paragraph paragraph) {
        if (paragraph == null || paragraph.getContent() == null || paragraph.getContent().isBlank()) {
            return;
        }
        cell.addElement(paragraph);
    }

    private void addPdfSectionSeparator(Document document) throws Exception {
        PdfPTable separator = new PdfPTable(1);
        separator.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderWidthBottom(0.8f);
        cell.setBorderWidthTop(0);
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthRight(0);
        separator.addCell(cell);
        document.add(separator);
    }

    private PdfPCell bodyCell(String text, float fontSize) {
        return new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, Math.max(4, fontSize - 1))));
    }

    private void addDocxCenteredIfNotBlank(XWPFParagraph baseParagraph, String text, int fontSize) {
        if (text == null || text.isBlank()) {
            return;
        }
        XWPFParagraph p = baseParagraph.getBody().insertNewParagraph(baseParagraph.getCTP().newCursor());
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = p.createRun();
        run.setFontSize(fontSize);
        run.setText(text);
    }

    private Image resolveLogoAsPdfImage(String logoUrl) {
        byte[] logoBytes = resolveLogoBytes(logoUrl);
        if (logoBytes == null) {
            return null;
        }
        try {
            return Image.getInstance(logoBytes);
        } catch (Exception ex) {
            return null;
        }
    }

    private void addXlsxLogo(Sheet sheet, String logoUrl, int col1, int row1) {
        byte[] logoBytes = resolveLogoBytes(logoUrl);
        if (logoBytes == null) {
            return;
        }
        try {
            Workbook workbook = sheet.getWorkbook();
            int pictureType = detectWorkbookPictureType(logoUrl, logoBytes);
            int pictureIdx = workbook.addPicture(logoBytes, pictureType);
            org.apache.poi.ss.usermodel.Drawing<?> drawing = sheet.createDrawingPatriarch();
            org.apache.poi.ss.usermodel.ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
            anchor.setCol1(Math.max(0, col1));
            anchor.setRow1(Math.max(0, row1));
            drawing.createPicture(anchor, pictureIdx).resize(0.9);
        } catch (Exception ignored) {
        }
    }

    private byte[] resolveLogoBytes(String logoUrl) {
        if (logoUrl == null || logoUrl.isBlank()) {
            return null;
        }
        try {
            String trimmed = logoUrl.trim();
            if (trimmed.startsWith("data:image")) {
                int commaIndex = trimmed.indexOf(',');
                if (commaIndex > 0) {
                    return Base64.getDecoder().decode(trimmed.substring(commaIndex + 1));
                }
            }
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                try (InputStream is = new URL(trimmed).openStream()) {
                    return is.readAllBytes();
                }
            }
            Path path = Path.of(trimmed);
            if (Files.exists(path)) {
                return Files.readAllBytes(path);
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private int detectWorkbookPictureType(String logoUrl, byte[] logoBytes) {
        String normalizedUrl = logoUrl != null ? logoUrl.toLowerCase() : "";
        if (normalizedUrl.endsWith(".jpg") || normalizedUrl.endsWith(".jpeg")) {
            return Workbook.PICTURE_TYPE_JPEG;
        }
        if (isJpeg(logoBytes)) {
            return Workbook.PICTURE_TYPE_JPEG;
        }
        return Workbook.PICTURE_TYPE_PNG;
    }

    private int detectPictureType(String logoUrl, byte[] logoBytes) {
        String normalizedUrl = logoUrl != null ? logoUrl.toLowerCase() : "";
        if (normalizedUrl.endsWith(".jpg") || normalizedUrl.endsWith(".jpeg")) {
            return org.apache.poi.xwpf.usermodel.Document.PICTURE_TYPE_JPEG;
        }
        if (isJpeg(logoBytes)) {
            return org.apache.poi.xwpf.usermodel.Document.PICTURE_TYPE_JPEG;
        }
        return org.apache.poi.xwpf.usermodel.Document.PICTURE_TYPE_PNG;
    }

    private boolean isJpeg(byte[] bytes) {
        return bytes != null
                && bytes.length > 2
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8;
    }

    private String buildTelephonesLine(PresenceReportHeaderDTO header) {
        List<String> phones = new ArrayList<>();
        if (header.getTelephone1() != null && !header.getTelephone1().isBlank()) {
            phones.add(header.getTelephone1().trim());
        }
        if (header.getTelephone2() != null && !header.getTelephone2().isBlank()) {
            phones.add(header.getTelephone2().trim());
        }
        if (header.getTelephone3() != null && !header.getTelephone3().isBlank()) {
            phones.add(header.getTelephone3().trim());
        }
        if (phones.isEmpty()) {
            return "";
        }
        return "Telephones: " + String.join(" / ", phones);
    }

    private String buildFaxLine(PresenceReportHeaderDTO header) {
        if (header.getFax() == null || header.getFax().isBlank()) {
            return "";
        }
        return "Fax: " + header.getFax().trim();
    }

    private String buildAddressLine(PresenceReportHeaderDTO header) {
        StringJoiner joiner = new StringJoiner(", ");
        if (header.getAdresse() != null && !header.getAdresse().isBlank()) {
            joiner.add(header.getAdresse().trim());
        }
        if (header.getVille() != null && !header.getVille().isBlank()) {
            joiner.add(header.getVille().trim());
        }
        if (header.getEtat() != null && !header.getEtat().isBlank()) {
            joiner.add(header.getEtat().trim());
        }
        if (header.getPays() != null && !header.getPays().isBlank()) {
            joiner.add(header.getPays().trim());
        }
        if (header.getCodePostal() != null && !header.getCodePostal().isBlank()) {
            joiner.add(header.getCodePostal().trim());
        }
        String value = joiner.toString();
        if (value.isBlank()) {
            return "";
        }
        return "Adresse: " + value;
    }

    private static final class OrientationSpec {
        private final String paper;
        private final boolean landscape;

        private OrientationSpec(String paper, boolean landscape) {
            this.paper = paper;
            this.landscape = landscape;
        }
    }
}
