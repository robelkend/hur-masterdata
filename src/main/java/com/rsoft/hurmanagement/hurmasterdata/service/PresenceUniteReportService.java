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
import com.rsoft.hurmanagement.hurmasterdata.dto.report.PresenceReportHeaderDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.report.PresenceReportResponseDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.report.PresenceReportRowDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.report.PresenceUniteReportGroupDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.report.PresenceUniteReportResponseDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.report.PresenceUniteReportRowDTO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
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

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class PresenceUniteReportService {

    private final PresenceReportService presenceReportService;

    @Transactional(readOnly = true)
    public PresenceUniteReportResponseDTO buildReport(LocalDate dateDebut,
                                                      LocalDate dateFin,
                                                      Long entrepriseId,
                                                      String nuit,
                                                      List<Long> uniteOrganisationnelleId,
                                                      List<Long> typeEmployeId,
                                                      Long gestionnaireId,
                                                      Long employeId,
                                                      String actif,
                                                      List<Long> regimePaieIds) {
        List<Long> safeRegimeIds = (regimePaieIds == null || regimePaieIds.isEmpty()) ? null : regimePaieIds;
        List<Long> safeUniteIds = (uniteOrganisationnelleId == null || uniteOrganisationnelleId.isEmpty()) ? null : uniteOrganisationnelleId;
        List<Long> safeTypeIds = (typeEmployeId == null || typeEmployeId.isEmpty()) ? null : typeEmployeId;

        PresenceReportResponseDTO base = presenceReportService.buildReport(
                dateDebut,
                dateFin,
                entrepriseId,
                nuit,
                safeUniteIds,
                safeTypeIds,
                gestionnaireId,
                employeId,
                "Y",
                safeRegimeIds,
                true,
                true,
                true,
                true
        );

        List<String> dates = buildDateHeaders(dateDebut, dateFin);
        List<PresenceUniteReportGroupDTO> groupes = buildGroupedReport(base.getRows(), dates);
        return new PresenceUniteReportResponseDTO(base.getHeader(), dates, groupes);
    }

    public byte[] exportPdf(PresenceUniteReportResponseDTO report, Integer textSize, String orientation) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            float fontSize = resolveExportFontSize(textSize);
            Document document = new Document(resolvePdfPageSize(orientation), 16, 16, 16, 16);
            PdfWriter.getInstance(document, out);
            document.open();

            addPdfHeader(document, report.getHeader(), fontSize);
            for (PresenceUniteReportGroupDTO groupe : report.getGroupes()) {
                addPdfGroup(document, groupe, report.getDates(), fontSize);
            }
            addPdfCriteria(document, report.getHeader(), fontSize);

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("presenceUniteReport.error.pdf", ex);
        }
    }

    public byte[] exportXlsx(PresenceUniteReportResponseDTO report, Integer textSize, String orientation) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            short fontSize = (short) Math.round(resolveExportFontSize(textSize));
            Sheet sheet = workbook.createSheet("Presence unite");
            applyXlsxOrientation(sheet, orientation);
            int totalCols = Math.max(2 + report.getDates().size(), 3);
            int rowIndex = writeXlsxHeader(sheet, report.getHeader(), totalCols, fontSize);

            CellStyle groupStyle = workbook.createCellStyle();
            Font groupFont = workbook.createFont();
            groupFont.setBold(true);
            groupFont.setFontHeightInPoints((short) Math.max(7, fontSize));
            groupStyle.setFont(groupFont);
            groupStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            groupStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());

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

            for (PresenceUniteReportGroupDTO groupe : report.getGroupes()) {
                Row groupRow = sheet.createRow(rowIndex++);
                Cell groupCell = groupRow.createCell(0);
                groupCell.setCellValue("UNITE: " + safe(groupe.getUniteCode()) + " - " + safe(groupe.getUniteNom()));
                groupCell.setCellStyle(groupStyle);

                Row headerRow = sheet.createRow(rowIndex++);
                int c = 0;
                setCell(headerRow, c++, "EMPLOYE", headerStyle);
                for (String date : report.getDates()) {
                    setCell(headerRow, c++, date, headerStyle);
                }
                setCell(headerRow, c, "NB HEURES TOT", headerStyle);

                for (PresenceUniteReportRowDTO row : groupe.getRows()) {
                    Row r = sheet.createRow(rowIndex++);
                    int col = 0;
                    setCell(r, col++, safe(row.getCodeEmploye()) + " - " + safe(row.getNomEmploye()), bodyStyle);
                    for (String date : report.getDates()) {
                        setCell(r, col++, safe(row.getValeursParDate().get(date)), bodyStyle);
                    }
                    setCell(r, col, row.getNbHeuresTot() != null ? row.getNbHeuresTot().toString() : "0.00", bodyStyle);
                }
                rowIndex++;
            }
            rowIndex = writeXlsxCriteria(sheet, report.getHeader(), rowIndex, fontSize);

            int columns = 2 + report.getDates().size();
            for (int i = 0; i < columns; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("presenceUniteReport.error.xlsx", ex);
        }
    }

    public byte[] exportDocx(PresenceUniteReportResponseDTO report, Integer textSize, String orientation) {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int fontSize = Math.max(6, Math.round(resolveExportFontSize(textSize)));
            applyDocxOrientation(document, orientation);
            addDocxHeader(document, report.getHeader(), fontSize);

            for (PresenceUniteReportGroupDTO groupe : report.getGroupes()) {
                XWPFParagraph groupTitle = document.createParagraph();
                groupTitle.setBorderBottom(Borders.SINGLE);
                XWPFRun gr = groupTitle.createRun();
                gr.setBold(true);
                gr.setFontSize(fontSize);
                gr.setText("UNITE: " + safe(groupe.getUniteCode()) + " - " + safe(groupe.getUniteNom()));

                int cols = 2 + report.getDates().size();
                XWPFTable table = document.createTable(groupe.getRows().size() + 1, cols);
                int col = 0;
                table.getRow(0).getCell(col++).setText("EMPLOYE");
                for (String date : report.getDates()) {
                    table.getRow(0).getCell(col++).setText(date);
                }
                table.getRow(0).getCell(col).setText("NB HEURES TOT");

                for (int i = 0; i < groupe.getRows().size(); i++) {
                    PresenceUniteReportRowDTO row = groupe.getRows().get(i);
                    int r = i + 1;
                    int c = 0;
                    table.getRow(r).getCell(c++).setText(safe(row.getCodeEmploye()) + " - " + safe(row.getNomEmploye()));
                    for (String date : report.getDates()) {
                        table.getRow(r).getCell(c++).setText(safe(row.getValeursParDate().get(date)));
                    }
                    table.getRow(r).getCell(c).setText(row.getNbHeuresTot() != null ? row.getNbHeuresTot().toString() : "0.00");
                }
            }
            addDocxCriteria(document, report.getHeader(), fontSize);
            document.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("presenceUniteReport.error.docx", ex);
        }
    }

    private List<PresenceUniteReportGroupDTO> buildGroupedReport(List<PresenceReportRowDTO> rows,
                                                                 List<String> dates) {
        Map<String, List<PresenceReportRowDTO>> rowMap = new HashMap<>();
        Map<String, EmployeeMeta> employees = new LinkedHashMap<>();
        for (PresenceReportRowDTO row : rows) {
            if (row.getCodeEmploye() == null || row.getDateDebut() == null) {
                continue;
            }
            String key = row.getCodeEmploye() + "|" + row.getDateDebut();
            rowMap.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
            employees.compute(row.getCodeEmploye(), (k, existing) -> mergeEmployeeMeta(existing, row));
        }

        Map<String, GroupAccumulator> grouped = new LinkedHashMap<>();
        for (EmployeeMeta meta : employees.values()) {
            Long uniteId = meta.uniteId != null ? meta.uniteId : 0L;
            String uniteCode = safe(meta.uniteCode);
            String uniteNom = safe(meta.uniteNom);
            String uniteKey = uniteId + "|" + uniteCode + "|" + uniteNom;

            GroupAccumulator group = grouped.computeIfAbsent(uniteKey, k -> new GroupAccumulator(uniteId, uniteCode, uniteNom));
            String empCode = safe(meta.codeEmploye);
            String empNom = safe(meta.nomEmploye);
            String empKey = empCode + "|" + empNom;
            RowAccumulator emp = group.rows.computeIfAbsent(empKey, k -> new RowAccumulator(empCode, empNom, dates));

            for (String dateKey : dates) {
                LocalDate date = LocalDate.parse(dateKey);
                List<PresenceReportRowDTO> candidates = rowMap.getOrDefault(empCode + "|" + date, java.util.Collections.emptyList());
                PresenceReportRowDTO selected = selectBestCandidate(candidates);
                if (selected != null && hasPresenceHours(selected) && selected.getNbHeures() != null) {
                    emp.totalHours = emp.totalHours.add(selected.getNbHeures());
                }
                String value = selected != null ? formatPresenceValue(selected) : "ABS";
                emp.setCellValue(dateKey, value);
            }
        }

        List<PresenceUniteReportGroupDTO> result = new ArrayList<>();
        for (GroupAccumulator group : grouped.values()) {
            List<PresenceUniteReportRowDTO> rowsOut = new ArrayList<>();
            for (RowAccumulator row : group.rows.values()) {
                rowsOut.add(new PresenceUniteReportRowDTO(
                        row.codeEmploye,
                        row.nomEmploye,
                        row.valuesByDate,
                        row.totalHours.setScale(2, RoundingMode.HALF_UP)
                ));
            }
            result.add(new PresenceUniteReportGroupDTO(group.uniteId, group.uniteCode, group.uniteNom, rowsOut));
        }
        return result;
    }

    private PresenceReportRowDTO selectBestCandidate(List<PresenceReportRowDTO> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        for (PresenceReportRowDTO row : candidates) {
            if (hasPresenceHours(row)) {
                return row;
            }
        }
        for (PresenceReportRowDTO row : candidates) {
            if (isYes(row.getFerie())) {
                return row;
            }
        }
        for (PresenceReportRowDTO row : candidates) {
            if (isYes(row.getOff())) {
                return row;
            }
        }
        for (PresenceReportRowDTO row : candidates) {
            if (isYes(row.getConge())) {
                return row;
            }
        }
        for (PresenceReportRowDTO row : candidates) {
            if (isYes(row.getAbsence())) {
                return row;
            }
        }
        return candidates.get(0);
    }

    private String formatPresenceValue(PresenceReportRowDTO row) {
        // If real presence exists, hours always win over status labels.
        if (hasPresenceHours(row)) {
            String arrivee = bracket(safe(row.getHeureDebut()));
            if (row.getDateFin() == null || row.getDateDebut() == null || row.getDateFin().equals(row.getDateDebut())) {
                return arrivee + " - " + bracket(safe(row.getHeureFin()));
            }
            return arrivee + " - " + bracket(row.getDateFin() + " " + safe(row.getHeureFin()));
        }
        if (isYes(row.getFerie())) {
            return "FERIE";
        }
        if (isYes(row.getOff())) {
            return "OFF";
        }
        if (isYes(row.getConge())) {
            return "CONGE";
        }
        return "ABS";
    }

    private boolean hasPresenceHours(PresenceReportRowDTO row) {
        if (row == null) {
            return false;
        }
        String debut = safe(row.getHeureDebut());
        String fin = safe(row.getHeureFin());
        if (debut.isBlank() || fin.isBlank()) {
            return false;
        }
        // Only real presence_employe rows can show hours.
        // Synthetic rows (OFF/FERIE/CONGE/ABS generated rows) must never show planned schedule times.
        if (row.getNbPresencesJour() == null || row.getNbPresencesJour() <= 0) {
            return false;
        }
        BigDecimal hours = row.getNbHeures();
        return hours != null && hours.compareTo(BigDecimal.ZERO) > 0;
    }

    private EmployeeMeta mergeEmployeeMeta(EmployeeMeta existing, PresenceReportRowDTO row) {
        if (row == null || row.getCodeEmploye() == null) {
            return existing;
        }
        EmployeeMeta candidate = new EmployeeMeta(
                row.getCodeEmploye(),
                safe(row.getNomEmploye()),
                row.getUniteOrganisationnelleId(),
                row.getUniteOrganisationnelleCode(),
                row.getUniteOrganisationnelleNom(),
                hasPresenceHours(row)
        );
        if (existing == null) {
            return candidate;
        }
        // Prefer unit metadata coming from a true presence row when available.
        if (!existing.fromPresenceHours && candidate.fromPresenceHours) {
            return candidate;
        }
        return existing;
    }

    private List<String> buildDateHeaders(LocalDate from, LocalDate to) {
        List<String> dates = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            dates.add(d.toString());
        }
        return dates;
    }

    private boolean isYes(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        return "OUI".equals(normalized) || "Y".equals(normalized) || "YES".equals(normalized);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String bracket(String value) {
        return "[" + safe(value) + "]";
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
            return new OrientationSpec("LETTER", false);
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

    private int writeTitleRow(Sheet sheet, int rowIndex, String value) {
        Row row = sheet.createRow(rowIndex++);
        Cell cell = row.createCell(0);
        cell.setCellValue(value);
        return rowIndex;
    }

    private int writeXlsxHeader(Sheet sheet, PresenceReportHeaderDTO header, int totalCols, short fontSize) {
        Workbook workbook = sheet.getWorkbook();
        CellStyle centerStyle = workbook.createCellStyle();
        centerStyle.setAlignment(HorizontalAlignment.CENTER);
        Font headerFont = workbook.createFont();
        headerFont.setFontHeightInPoints((short) Math.max(4, fontSize));
        centerStyle.setFont(headerFont);

        int rowIndex = 0;
        rowIndex = writeCenteredMergedRow(sheet, rowIndex, safe(header != null ? header.getNomEntreprise() : ""), centerStyle, totalCols - 1);
        rowIndex = writeCenteredMergedRowIfNotBlank(sheet, rowIndex, buildAddressLine(header), centerStyle, totalCols - 1);
        rowIndex = writeCenteredMergedRowIfNotBlank(sheet, rowIndex, buildTelephonesLine(header), centerStyle, totalCols - 1);
        rowIndex = writeCenteredMergedRowIfNotBlank(sheet, rowIndex, buildFaxLine(header), centerStyle, totalCols - 1);
        rowIndex = writeCenteredMergedRow(sheet, rowIndex, "PRESENCE PAR UNITE ORGANISATIONNELLE", centerStyle, totalCols - 1);

        Row rowSortie = sheet.createRow(rowIndex++);
        Cell sortieCell = rowSortie.createCell(0);
        sortieCell.setCellValue("Sorti le: " + safe(header != null ? header.getGeneratedAt() : ""));
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
        addXlsxLogo(sheet, header != null ? header.getLogoUrl() : null, Math.max(0, totalCols - 2), 0);
        return rowIndex;
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

    private int writeXlsxCriteria(Sheet sheet, PresenceReportHeaderDTO header, int rowIndex, short fontSize) {
        if (header == null || header.getCriteriaSummary() == null || header.getCriteriaSummary().isBlank()) {
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

    private void setCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void addPdfHeader(Document document, PresenceReportHeaderDTO header, float fontSize) throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 1});

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.setHorizontalAlignment(Element.ALIGN_CENTER);
        left.addElement(centeredParagraph(safe(header != null ? header.getNomEntreprise() : ""), FontFactory.getFont(FontFactory.HELVETICA_BOLD, fontSize + 2)));
        addIfNotBlank(left, centeredParagraph(buildAddressLine(header), FontFactory.getFont(FontFactory.HELVETICA, fontSize)));
        addIfNotBlank(left, centeredParagraph(buildTelephonesLine(header), FontFactory.getFont(FontFactory.HELVETICA, fontSize)));
        addIfNotBlank(left, centeredParagraph(buildFaxLine(header), FontFactory.getFont(FontFactory.HELVETICA, fontSize)));
        left.addElement(centeredParagraph("PRESENCE PAR UNITE ORGANISATIONNELLE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, fontSize + 1)));
        left.addElement(new Paragraph("Sorti le: " + safe(header != null ? header.getGeneratedAt() : ""), FontFactory.getFont(FontFactory.HELVETICA, fontSize)));
        table.addCell(left);

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        right.setVerticalAlignment(Element.ALIGN_TOP);
        Image logo = resolveLogoAsPdfImage(header != null ? header.getLogoUrl() : null);
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

    private void addPdfGroup(Document document, PresenceUniteReportGroupDTO groupe, List<String> dates, float fontSize) throws Exception {
        Paragraph groupTitle = new Paragraph(
                "UNITE: " + safe(groupe.getUniteCode()) + " - " + safe(groupe.getUniteNom()),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, fontSize)
        );
        groupTitle.setSpacingBefore(8f);
        groupTitle.setSpacingAfter(4f);
        document.add(groupTitle);

        int cols = 2 + dates.size();
        PdfPTable table = new PdfPTable(cols);
        table.setWidthPercentage(100);
        float[] widths = new float[cols];
        widths[0] = 2.5f;
        for (int i = 0; i < dates.size(); i++) {
            widths[i + 1] = 1.2f;
        }
        widths[cols - 1] = 1.0f;
        table.setWidths(widths);

        addHeaderCell(table, "EMPLOYE", fontSize);
        for (String date : dates) {
            addHeaderCell(table, date, fontSize);
        }
        addHeaderCell(table, "NB HEURES TOT", fontSize);

        for (PresenceUniteReportRowDTO row : groupe.getRows()) {
            table.addCell(bodyCell(safe(row.getCodeEmploye()) + " - " + safe(row.getNomEmploye()), fontSize));
            for (String date : dates) {
                table.addCell(bodyCell(safe(row.getValeursParDate().get(date)), fontSize));
            }
            table.addCell(bodyCell(row.getNbHeuresTot() != null ? row.getNbHeuresTot().toString() : "0.00", fontSize));
        }
        document.add(table);
    }

    private void addHeaderCell(PdfPTable table, String text, float fontSize) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, fontSize)));
        cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        table.addCell(cell);
    }

    private PdfPCell bodyCell(String text, float fontSize) {
        return new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, Math.max(4, fontSize - 1))));
    }

    private void addPdfCriteria(Document document, PresenceReportHeaderDTO header, float fontSize) throws Exception {
        if (header == null || header.getCriteriaSummary() == null || header.getCriteriaSummary().isBlank()) {
            return;
        }
        document.add(com.lowagie.text.Chunk.NEWLINE);
        document.add(new Paragraph("CRITERES: " + header.getCriteriaSummary(), FontFactory.getFont(FontFactory.HELVETICA, fontSize)));
    }

    private void addDocxHeader(XWPFDocument document, PresenceReportHeaderDTO header, int fontSize) {
        XWPFTable table = document.createTable(1, 2);
        XWPFParagraph left = table.getRow(0).getCell(0).addParagraph();
        left.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = left.createRun();
        run.setBold(true);
        run.setFontSize(fontSize + 2);
        run.setText(safe(header != null ? header.getNomEntreprise() : ""));
        run.setBold(false);
        addDocxCenteredIfNotBlank(left, buildAddressLine(header), fontSize);
        addDocxCenteredIfNotBlank(left, buildTelephonesLine(header), fontSize);
        addDocxCenteredIfNotBlank(left, buildFaxLine(header), fontSize);
        addDocxCenteredIfNotBlank(left, "PRESENCE PAR UNITE ORGANISATIONNELLE", fontSize + 1);

        XWPFParagraph leftInfo = table.getRow(0).getCell(0).addParagraph();
        leftInfo.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun infoRun = leftInfo.createRun();
        infoRun.setFontSize(fontSize);
        infoRun.setText("Sorti le: " + safe(header != null ? header.getGeneratedAt() : ""));

        XWPFParagraph right = table.getRow(0).getCell(1).addParagraph();
        right.setAlignment(ParagraphAlignment.RIGHT);
        byte[] logoBytes = resolveLogoBytes(header != null ? header.getLogoUrl() : null);
        if (logoBytes != null) {
            try {
                XWPFRun imgRun = right.createRun();
                imgRun.addPicture(
                        new ByteArrayInputStream(logoBytes),
                        detectPictureType(header != null ? header.getLogoUrl() : null, logoBytes),
                        "logo",
                        Units.toEMU(120),
                        Units.toEMU(60)
                );
            } catch (Exception ignored) {
            }
        }
        XWPFParagraph separator = document.createParagraph();
        separator.setBorderBottom(Borders.SINGLE);
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

    private void addDocxCriteria(XWPFDocument document, PresenceReportHeaderDTO header, int fontSize) {
        if (header == null || header.getCriteriaSummary() == null || header.getCriteriaSummary().isBlank()) {
            return;
        }
        XWPFParagraph p = document.createParagraph();
        XWPFRun run = p.createRun();
        run.setFontSize(fontSize);
        run.setText("CRITERES: " + header.getCriteriaSummary());
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
        if (header != null && header.getTelephone1() != null && !header.getTelephone1().isBlank()) {
            phones.add(header.getTelephone1().trim());
        }
        if (header != null && header.getTelephone2() != null && !header.getTelephone2().isBlank()) {
            phones.add(header.getTelephone2().trim());
        }
        if (header != null && header.getTelephone3() != null && !header.getTelephone3().isBlank()) {
            phones.add(header.getTelephone3().trim());
        }
        if (phones.isEmpty()) {
            return "";
        }
        return "Telephones: " + String.join(" / ", phones);
    }

    private String buildFaxLine(PresenceReportHeaderDTO header) {
        if (header == null || header.getFax() == null || header.getFax().isBlank()) {
            return "";
        }
        return "Fax: " + header.getFax().trim();
    }

    private String buildAddressLine(PresenceReportHeaderDTO header) {
        if (header == null) {
            return "";
        }
        java.util.StringJoiner joiner = new java.util.StringJoiner(", ");
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

    private static class GroupAccumulator {
        private final Long uniteId;
        private final String uniteCode;
        private final String uniteNom;
        private final Map<String, RowAccumulator> rows = new TreeMap<>();

        private GroupAccumulator(Long uniteId, String uniteCode, String uniteNom) {
            this.uniteId = uniteId;
            this.uniteCode = uniteCode;
            this.uniteNom = uniteNom;
        }
    }

    private static class RowAccumulator {
        private final String codeEmploye;
        private final String nomEmploye;
        private final Map<String, String> valuesByDate;
        private BigDecimal totalHours = BigDecimal.ZERO;

        private RowAccumulator(String codeEmploye, String nomEmploye, List<String> dates) {
            this.codeEmploye = codeEmploye;
            this.nomEmploye = nomEmploye;
            this.valuesByDate = new LinkedHashMap<>();
            for (String date : dates) {
                this.valuesByDate.put(date, "");
            }
        }

        private void setCellValue(String date, String value) {
            valuesByDate.put(date, value != null ? value : "");
        }
    }

    private static class EmployeeMeta {
        private final String codeEmploye;
        private final String nomEmploye;
        private final Long uniteId;
        private final String uniteCode;
        private final String uniteNom;
        private final boolean fromPresenceHours;

        private EmployeeMeta(String codeEmploye,
                             String nomEmploye,
                             Long uniteId,
                             String uniteCode,
                             String uniteNom,
                             boolean fromPresenceHours) {
            this.codeEmploye = codeEmploye;
            this.nomEmploye = nomEmploye;
            this.uniteId = uniteId;
            this.uniteCode = uniteCode;
            this.uniteNom = uniteNom;
            this.fromPresenceHours = fromPresenceHours;
        }
    }

    private static class OrientationSpec {
        private final String paper;
        private final boolean landscape;

        private OrientationSpec(String paper, boolean landscape) {
            this.paper = paper;
            this.landscape = landscape;
        }
    }
}
