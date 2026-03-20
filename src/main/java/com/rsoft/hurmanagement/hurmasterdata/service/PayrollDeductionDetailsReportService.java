package com.rsoft.hurmanagement.hurmasterdata.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.rsoft.hurmanagement.hurmasterdata.dto.report.PayrollDeductionDetailsGroupDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.report.PayrollDeductionDetailsReportResponseDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.report.PayrollDeductionDetailsRowDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.report.PayrollDeductionDetailsTotalDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.report.PresenceReportHeaderDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.Employe;
import com.rsoft.hurmanagement.hurmasterdata.entity.Entreprise;
import com.rsoft.hurmanagement.hurmasterdata.entity.Payroll;
import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollDeduction;
import com.rsoft.hurmanagement.hurmasterdata.entity.PayrollEmploye;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmploiEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EntrepriseRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollDeductionRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PayrollEmployeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
public class PayrollDeductionDetailsReportService {

    private final PayrollEmployeRepository payrollEmployeRepository;
    private final PayrollDeductionRepository payrollDeductionRepository;
    private final EmploiEmployeRepository emploiEmployeRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final EmployeRepository employeRepository;

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional(readOnly = true)
    public PayrollDeductionDetailsReportResponseDTO buildReport(LocalDate dateDebut,
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
                dateDebut, dateFin, entrepriseId, nightFilter, safeUniteIds, safeTypeIds, gestionnaireId, employeId, actifFilter, safeRegimeIds, statutFilter
        );
        if (payrollEmployes.isEmpty()) {
            PresenceReportHeaderDTO header = buildHeader(entrepriseId);
            header.setGeneratedAt(LocalDateTime.now().format(DATE_TIME_FORMAT));
            header.setCriteriaSummary(buildCriteriaSummary(dateDebut, dateFin, entrepriseId, statutFilter));
            PayrollDeductionDetailsTotalDTO total = emptyTotal("GRAND TOTAL");
            return new PayrollDeductionDetailsReportResponseDTO(header, List.of(), List.of(), List.of(), total, List.of(), total);
        }

        Set<String> payrollDateHeadersSet = new LinkedHashSet<>();
        Map<Long, PayrollEmploye> peById = new LinkedHashMap<>();
        for (PayrollEmploye pe : payrollEmployes) {
            peById.put(pe.getId(), pe);
            payrollDateHeadersSet.add(formatDate(pe.getPayroll().getDateFin()));
        }
        List<String> payrollDateHeaders = payrollDateHeadersSet.stream().sorted().toList();

        List<Long> peIds = new ArrayList<>(peById.keySet());
        List<PayrollDeduction> deductions = peIds.isEmpty() ? List.of() : payrollDeductionRepository.findByPayrollEmployeIds(peIds);
        Map<Long, List<PayrollDeduction>> deductionsByPe = new LinkedHashMap<>();
        for (PayrollDeduction d : deductions) {
            deductionsByPe.computeIfAbsent(d.getPayrollEmploye().getId(), k -> new ArrayList<>()).add(d);
        }

        Set<String> dedEmployeHeadersSet = new LinkedHashSet<>();
        Set<String> dedEmployeurHeadersSet = new LinkedHashSet<>();
        for (PayrollDeduction d : deductions) {
            String key = deductionLabel(d);
            dedEmployeHeadersSet.add(key);
            if (nz(d.getMontantCouvert())) {
                dedEmployeurHeadersSet.add(key);
            }
        }
        List<String> dedEmployeHeaders = new ArrayList<>(dedEmployeHeadersSet);
        List<String> dedEmployeurHeaders = new ArrayList<>(dedEmployeurHeadersSet);

        Map<String, EmploiEmploye> emploiCache = new LinkedHashMap<>();
        Map<String, GroupAccumulator> groupAccMap = new LinkedHashMap<>();
        PayrollDeductionDetailsTotalDTO grandTotal = initTotal("GRAND TOTAL", payrollDateHeaders, dedEmployeHeaders, dedEmployeurHeaders);

        for (PayrollEmploye pe : payrollEmployes) {
            Employe employe = pe.getEmploye();
            LocalDate dateFinPayroll = pe.getPayroll().getDateFin();
            EmploiEmploye emploi = resolveEmploiForDate(emploiCache, employe.getId(), dateFinPayroll);
            Long uniteId = emploi != null && emploi.getUniteOrganisationnelle() != null ? emploi.getUniteOrganisationnelle().getId() : 0L;
            String uniteCode = emploi != null && emploi.getUniteOrganisationnelle() != null ? safe(emploi.getUniteOrganisationnelle().getCode()) : "";
            String uniteNom = emploi != null && emploi.getUniteOrganisationnelle() != null ? safe(emploi.getUniteOrganisationnelle().getNom()) : "Non definie";
            String groupKey = uniteId + "|" + uniteCode + "|" + uniteNom;

            GroupAccumulator group = groupAccMap.computeIfAbsent(groupKey, k -> new GroupAccumulator(
                    uniteId, uniteCode, uniteNom, new LinkedHashMap<>(),
                    initTotal("TOTAL " + (uniteNom.isBlank() ? "UNITE" : uniteNom), payrollDateHeaders, dedEmployeHeaders, dedEmployeurHeaders)
            ));

            PayrollDeductionDetailsRowDTO row = group.rowsByEmployeId.computeIfAbsent(employe.getId(), id -> initRow(employe, payrollDateHeaders, dedEmployeHeaders, dedEmployeurHeaders));
            String dateKey = formatDate(dateFinPayroll);

            BigDecimal montant = nvl(pe.getMontantSalaireBase()).subtract(nvl(pe.getMontantSanctions()));
            addMapValue(row.getMontantsByDate(), dateKey, montant);
            row.setMontantSupplementaire(row.getMontantSupplementaire().add(nvl(pe.getMontantSupplementaire())));
            row.setMontantAutreRevenu(row.getMontantAutreRevenu().add(nvl(pe.getMontantAutreRevenu())));
            row.setMontantDeductions(row.getMontantDeductions().add(nvl(pe.getMontantDeductions()).add(nvl(pe.getMontantRecouvrements()))));
            row.setMontantBrut(row.getMontantBrut().add(nvl(pe.getMontantBrut())));
            row.setMontantNetAPayer(row.getMontantNetAPayer().add(nvl(pe.getMontantNetAPayer())));

            addMapValue(group.total.getMontantsByDate(), dateKey, montant);
            group.total.setMontantSupplementaire(group.total.getMontantSupplementaire().add(nvl(pe.getMontantSupplementaire())));
            group.total.setMontantAutreRevenu(group.total.getMontantAutreRevenu().add(nvl(pe.getMontantAutreRevenu())));
            group.total.setMontantDeductions(group.total.getMontantDeductions().add(nvl(pe.getMontantDeductions()).add(nvl(pe.getMontantRecouvrements()))));
            group.total.setMontantBrut(group.total.getMontantBrut().add(nvl(pe.getMontantBrut())));
            group.total.setMontantNetAPayer(group.total.getMontantNetAPayer().add(nvl(pe.getMontantNetAPayer())));

            addMapValue(grandTotal.getMontantsByDate(), dateKey, montant);
            grandTotal.setMontantSupplementaire(grandTotal.getMontantSupplementaire().add(nvl(pe.getMontantSupplementaire())));
            grandTotal.setMontantAutreRevenu(grandTotal.getMontantAutreRevenu().add(nvl(pe.getMontantAutreRevenu())));
            grandTotal.setMontantDeductions(grandTotal.getMontantDeductions().add(nvl(pe.getMontantDeductions()).add(nvl(pe.getMontantRecouvrements()))));
            grandTotal.setMontantBrut(grandTotal.getMontantBrut().add(nvl(pe.getMontantBrut())));
            grandTotal.setMontantNetAPayer(grandTotal.getMontantNetAPayer().add(nvl(pe.getMontantNetAPayer())));

            List<PayrollDeduction> peDeds = deductionsByPe.getOrDefault(pe.getId(), List.of());
            for (PayrollDeduction ded : peDeds) {
                String dedKey = deductionLabel(ded);
                addMapValue(row.getDeductionsEmploye(), dedKey, nvl(ded.getMontant()));
                addMapValue(group.total.getDeductionsEmploye(), dedKey, nvl(ded.getMontant()));
                addMapValue(grandTotal.getDeductionsEmploye(), dedKey, nvl(ded.getMontant()));
                if (dedEmployeurHeadersSet.contains(dedKey)) {
                    addMapValue(row.getDeductionsEmployeur(), dedKey, nvl(ded.getMontantCouvert()));
                    addMapValue(group.total.getDeductionsEmployeur(), dedKey, nvl(ded.getMontantCouvert()));
                    addMapValue(grandTotal.getDeductionsEmployeur(), dedKey, nvl(ded.getMontantCouvert()));
                }
            }
        }

        List<PayrollDeductionDetailsGroupDTO> groups = groupAccMap.values().stream()
                .sorted(Comparator.comparing(g -> g.uniteNom))
                .map(g -> new PayrollDeductionDetailsGroupDTO(
                        g.uniteId,
                        g.uniteCode,
                        g.uniteNom,
                        g.rowsByEmployeId.values().stream()
                                .sorted(Comparator.comparing(PayrollDeductionDetailsRowDTO::getCodeEmploye, Comparator.nullsLast(String::compareTo)))
                                .toList(),
                        g.total
                ))
                .toList();

        PresenceReportHeaderDTO header = buildHeader(entrepriseId);
        header.setGeneratedAt(LocalDateTime.now().format(DATE_TIME_FORMAT));
        header.setCriteriaSummary(buildCriteriaSummary(dateDebut, dateFin, entrepriseId, statutFilter));
        return new PayrollDeductionDetailsReportResponseDTO(
                header,
                payrollDateHeaders,
                dedEmployeHeaders,
                dedEmployeurHeaders,
                grandTotal,
                groups,
                cloneTotal(grandTotal)
        );
    }

    public byte[] exportPdf(PayrollDeductionDetailsReportResponseDTO report) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 12, 12, 12, 12);
            PdfWriter.getInstance(document, out);
            document.open();
            addPdfHeader(document, report.getHeader());
            addPdfTotal(document, report.getGrandTotalTop());
            for (PayrollDeductionDetailsGroupDTO g : report.getGroupes()) {
                document.add(new Paragraph("UNITE: " + safe(g.getUniteCode()) + " - " + safe(g.getUniteNom()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
                addPdfTable(document, report, g);
                addPdfTotal(document, g.getTotal());
                document.add(new Paragraph(" "));
            }
            addPdfTotal(document, report.getGrandTotalBottom());
            if (report.getHeader() != null && report.getHeader().getCriteriaSummary() != null) {
                document.add(new Paragraph("CRITERES: " + report.getHeader().getCriteriaSummary(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
            }
            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("payrollDeductionReport.error.pdf", ex);
        }
    }

    public byte[] exportXlsx(PayrollDeductionDetailsReportResponseDTO report) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Payroll et deduction");
            int rowIdx = 0;

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font hFont = workbook.createFont();
            hFont.setBold(true);
            headerStyle.setFont(hFont);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            Row h1 = sheet.createRow(rowIdx++);
            h1.createCell(0).setCellValue(safe(report.getHeader() != null ? report.getHeader().getNomEntreprise() : ""));
            Row h2 = sheet.createRow(rowIdx++);
            h2.createCell(0).setCellValue("DETAILS DE PAIES - PAYROLL ET DEDUCTION");
            rowIdx = writeXlsxTotalRow(sheet, rowIdx, report, report.getGrandTotalTop(), "GRAND TOTAL (HAUT)", headerStyle, null);
            rowIdx++;

            for (PayrollDeductionDetailsGroupDTO g : report.getGroupes()) {
                Row gr = sheet.createRow(rowIdx++);
                gr.createCell(0).setCellValue("UNITE: " + safe(g.getUniteCode()) + " - " + safe(g.getUniteNom()));
                Row headerRow = sheet.createRow(rowIdx++);
                List<String> cols = exportColumns(report);
                for (int c = 0; c < cols.size(); c++) {
                    headerRow.createCell(c).setCellValue(cols.get(c));
                    headerRow.getCell(c).setCellStyle(headerStyle);
                }
                for (PayrollDeductionDetailsRowDTO row : g.getRows()) {
                    Row r = sheet.createRow(rowIdx++);
                    fillXlsxDataRow(r, report, row);
                }
                rowIdx = writeXlsxTotalRow(sheet, rowIdx, report, g.getTotal(), "TOTAL UNITE", headerStyle, null);
                rowIdx++;
            }
            rowIdx = writeXlsxTotalRow(sheet, rowIdx, report, report.getGrandTotalBottom(), "GRAND TOTAL (BAS)", headerStyle, null);

            List<String> cols = exportColumns(report);
            for (int i = 0; i < cols.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("payrollDeductionReport.error.xlsx", ex);
        }
    }

    public byte[] exportDocx(PayrollDeductionDetailsReportResponseDTO report) {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            doc.createParagraph().createRun().setText(safe(report.getHeader() != null ? report.getHeader().getNomEntreprise() : ""));
            doc.createParagraph().createRun().setText("DETAILS DE PAIES - PAYROLL ET DEDUCTION");
            addDocxTotal(doc, report, report.getGrandTotalTop(), "GRAND TOTAL (HAUT)");

            for (PayrollDeductionDetailsGroupDTO g : report.getGroupes()) {
                doc.createParagraph().createRun().setText("UNITE: " + safe(g.getUniteCode()) + " - " + safe(g.getUniteNom()));
                List<String> cols = exportColumns(report);
                XWPFTable table = doc.createTable(g.getRows().size() + 1, cols.size());
                for (int c = 0; c < cols.size(); c++) {
                    table.getRow(0).getCell(c).setText(cols.get(c));
                }
                for (int i = 0; i < g.getRows().size(); i++) {
                    fillDocxDataRow(table, i + 1, report, g.getRows().get(i));
                }
                addDocxTotal(doc, report, g.getTotal(), "TOTAL UNITE");
            }
            addDocxTotal(doc, report, report.getGrandTotalBottom(), "GRAND TOTAL (BAS)");
            doc.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("payrollDeductionReport.error.docx", ex);
        }
    }

    private List<String> exportColumns(PayrollDeductionDetailsReportResponseDTO report) {
        List<String> cols = new ArrayList<>();
        cols.add("CODE");
        cols.add("NOM");
        cols.add("PRENOM");
        cols.add("DATE EMBAUCHE");
        for (String d : report.getPayrollDates()) cols.add("MONTANT " + d);
        cols.add("SUPPL");
        cols.add("AUTRE REV");
        cols.add("BRUT");
        cols.add("DEDUCTIONS");
        cols.add("NET");
        for (String h : report.getDeductionEmployeHeaders()) cols.add("DED EMP " + h);
        for (String h : report.getDeductionEmployeurHeaders()) cols.add("EMPLOYEUR " + h);
        return cols;
    }

    private void addPdfHeader(Document doc, PresenceReportHeaderDTO header) throws Exception {
        if (header == null) return;
        Paragraph p1 = new Paragraph(safe(header.getNomEntreprise()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11));
        p1.setAlignment(Element.ALIGN_CENTER);
        doc.add(p1);
        Paragraph p2 = new Paragraph("DETAILS DE PAIES - PAYROLL ET DEDUCTION", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10));
        p2.setAlignment(Element.ALIGN_CENTER);
        doc.add(p2);
        doc.add(new Paragraph("Sorti le: " + safe(header.getGeneratedAt()), FontFactory.getFont(FontFactory.HELVETICA, 8)));
        doc.add(new Paragraph(" "));
    }

    private void addPdfTable(Document doc, PayrollDeductionDetailsReportResponseDTO report, PayrollDeductionDetailsGroupDTO g) throws Exception {
        List<String> cols = exportColumns(report);
        PdfPTable table = new PdfPTable(cols.size());
        table.setWidthPercentage(100);
        for (String c : cols) {
            PdfPCell hc = new PdfPCell(new Phrase(c, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7)));
            table.addCell(hc);
        }
        for (PayrollDeductionDetailsRowDTO row : g.getRows()) {
            addPdfDataRow(table, report, row);
        }
        doc.add(table);
    }

    private void addPdfDataRow(PdfPTable table, PayrollDeductionDetailsReportResponseDTO report, PayrollDeductionDetailsRowDTO row) {
        table.addCell(body(safe(row.getCodeEmploye())));
        table.addCell(body(safe(row.getNomEmploye())));
        table.addCell(body(safe(row.getPrenomEmploye())));
        table.addCell(body(row.getDatePremiereEmbauche() != null ? row.getDatePremiereEmbauche().toString() : ""));
        for (String d : report.getPayrollDates()) table.addCell(body(formatAmount(row.getMontantsByDate().get(d))));
        table.addCell(body(formatAmount(row.getMontantSupplementaire())));
        table.addCell(body(formatAmount(row.getMontantAutreRevenu())));
        table.addCell(body(formatAmount(row.getMontantBrut())));
        table.addCell(body(formatAmount(row.getMontantDeductions())));
        table.addCell(body(formatAmount(row.getMontantNetAPayer())));
        for (String h : report.getDeductionEmployeHeaders()) table.addCell(body(formatAmount(row.getDeductionsEmploye().get(h))));
        for (String h : report.getDeductionEmployeurHeaders()) table.addCell(body(formatAmount(row.getDeductionsEmployeur().get(h))));
    }

    private PdfPCell body(String v) {
        return new PdfPCell(new Phrase(v, FontFactory.getFont(FontFactory.HELVETICA, 7)));
    }

    private void addPdfTotal(Document doc, PayrollDeductionDetailsTotalDTO total) throws Exception {
        doc.add(new Paragraph(renderTotal(total), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8)));
    }

    private int writeXlsxTotalRow(Sheet sheet, int rowIdx, PayrollDeductionDetailsReportResponseDTO report,
                                  PayrollDeductionDetailsTotalDTO total, String label, CellStyle labelStyle, CellStyle valueStyle) {
        Row r = sheet.createRow(rowIdx++);
        int c = 0;
        r.createCell(c).setCellValue(label);
        r.getCell(c).setCellStyle(labelStyle);
        c = 4;
        for (String d : report.getPayrollDates()) r.createCell(c++).setCellValue(toDouble(total.getMontantsByDate().get(d)));
        r.createCell(c++).setCellValue(toDouble(total.getMontantSupplementaire()));
        r.createCell(c++).setCellValue(toDouble(total.getMontantAutreRevenu()));
        r.createCell(c++).setCellValue(toDouble(total.getMontantBrut()));
        r.createCell(c++).setCellValue(toDouble(total.getMontantDeductions()));
        r.createCell(c++).setCellValue(toDouble(total.getMontantNetAPayer()));
        for (String h : report.getDeductionEmployeHeaders()) r.createCell(c++).setCellValue(toDouble(total.getDeductionsEmploye().get(h)));
        for (String h : report.getDeductionEmployeurHeaders()) r.createCell(c++).setCellValue(toDouble(total.getDeductionsEmployeur().get(h)));
        return rowIdx;
    }

    private void fillXlsxDataRow(Row r, PayrollDeductionDetailsReportResponseDTO report, PayrollDeductionDetailsRowDTO row) {
        int c = 0;
        r.createCell(c++).setCellValue(safe(row.getCodeEmploye()));
        r.createCell(c++).setCellValue(safe(row.getNomEmploye()));
        r.createCell(c++).setCellValue(safe(row.getPrenomEmploye()));
        r.createCell(c++).setCellValue(row.getDatePremiereEmbauche() != null ? row.getDatePremiereEmbauche().toString() : "");
        for (String d : report.getPayrollDates()) r.createCell(c++).setCellValue(toDouble(row.getMontantsByDate().get(d)));
        r.createCell(c++).setCellValue(toDouble(row.getMontantSupplementaire()));
        r.createCell(c++).setCellValue(toDouble(row.getMontantAutreRevenu()));
        r.createCell(c++).setCellValue(toDouble(row.getMontantBrut()));
        r.createCell(c++).setCellValue(toDouble(row.getMontantDeductions()));
        r.createCell(c++).setCellValue(toDouble(row.getMontantNetAPayer()));
        for (String h : report.getDeductionEmployeHeaders()) r.createCell(c++).setCellValue(toDouble(row.getDeductionsEmploye().get(h)));
        for (String h : report.getDeductionEmployeurHeaders()) r.createCell(c++).setCellValue(toDouble(row.getDeductionsEmployeur().get(h)));
    }

    private void addDocxTotal(XWPFDocument doc, PayrollDeductionDetailsReportResponseDTO report, PayrollDeductionDetailsTotalDTO total, String label) {
        doc.createParagraph().createRun().setText(label + " | " + renderTotal(total));
    }

    private void fillDocxDataRow(XWPFTable table, int rowIndex, PayrollDeductionDetailsReportResponseDTO report, PayrollDeductionDetailsRowDTO row) {
        int c = 0;
        table.getRow(rowIndex).getCell(c++).setText(safe(row.getCodeEmploye()));
        table.getRow(rowIndex).getCell(c++).setText(safe(row.getNomEmploye()));
        table.getRow(rowIndex).getCell(c++).setText(safe(row.getPrenomEmploye()));
        table.getRow(rowIndex).getCell(c++).setText(row.getDatePremiereEmbauche() != null ? row.getDatePremiereEmbauche().toString() : "");
        for (String d : report.getPayrollDates()) table.getRow(rowIndex).getCell(c++).setText(formatAmount(row.getMontantsByDate().get(d)));
        table.getRow(rowIndex).getCell(c++).setText(formatAmount(row.getMontantSupplementaire()));
        table.getRow(rowIndex).getCell(c++).setText(formatAmount(row.getMontantAutreRevenu()));
        table.getRow(rowIndex).getCell(c++).setText(formatAmount(row.getMontantBrut()));
        table.getRow(rowIndex).getCell(c++).setText(formatAmount(row.getMontantDeductions()));
        table.getRow(rowIndex).getCell(c++).setText(formatAmount(row.getMontantNetAPayer()));
        for (String h : report.getDeductionEmployeHeaders()) table.getRow(rowIndex).getCell(c++).setText(formatAmount(row.getDeductionsEmploye().get(h)));
        for (String h : report.getDeductionEmployeurHeaders()) table.getRow(rowIndex).getCell(c++).setText(formatAmount(row.getDeductionsEmployeur().get(h)));
    }

    private PayrollDeductionDetailsRowDTO initRow(Employe e, List<String> dates, List<String> dedEmp, List<String> dedEmpr) {
        PayrollDeductionDetailsRowDTO row = new PayrollDeductionDetailsRowDTO();
        row.setEmployeId(e.getId());
        row.setCodeEmploye(e.getCodeEmploye());
        row.setNomEmploye(e.getNom());
        row.setPrenomEmploye(e.getPrenom());
        row.setDatePremiereEmbauche(e.getDatePremiereEmbauche());
        row.setMontantsByDate(initMap(dates));
        row.setDeductionsEmploye(initMap(dedEmp));
        row.setDeductionsEmployeur(initMap(dedEmpr));
        return row;
    }

    private PayrollDeductionDetailsTotalDTO initTotal(String label, List<String> dates, List<String> dedEmp, List<String> dedEmpr) {
        PayrollDeductionDetailsTotalDTO total = new PayrollDeductionDetailsTotalDTO();
        total.setLabel(label);
        total.setMontantsByDate(initMap(dates));
        total.setDeductionsEmploye(initMap(dedEmp));
        total.setDeductionsEmployeur(initMap(dedEmpr));
        return total;
    }

    private PayrollDeductionDetailsTotalDTO emptyTotal(String label) {
        PayrollDeductionDetailsTotalDTO total = new PayrollDeductionDetailsTotalDTO();
        total.setLabel(label);
        return total;
    }

    private PayrollDeductionDetailsTotalDTO cloneTotal(PayrollDeductionDetailsTotalDTO src) {
        PayrollDeductionDetailsTotalDTO clone = new PayrollDeductionDetailsTotalDTO();
        clone.setLabel(src.getLabel());
        clone.setMontantsByDate(new LinkedHashMap<>(src.getMontantsByDate()));
        clone.setMontantSupplementaire(src.getMontantSupplementaire());
        clone.setMontantAutreRevenu(src.getMontantAutreRevenu());
        clone.setMontantDeductions(src.getMontantDeductions());
        clone.setMontantBrut(src.getMontantBrut());
        clone.setMontantNetAPayer(src.getMontantNetAPayer());
        clone.setDeductionsEmploye(new LinkedHashMap<>(src.getDeductionsEmploye()));
        clone.setDeductionsEmployeur(new LinkedHashMap<>(src.getDeductionsEmployeur()));
        return clone;
    }

    private Map<String, BigDecimal> initMap(List<String> keys) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (String key : keys) map.put(key, BigDecimal.ZERO);
        return map;
    }

    private void addMapValue(Map<String, BigDecimal> map, String key, BigDecimal add) {
        if (map == null || key == null) return;
        map.put(key, nvl(map.get(key)).add(nvl(add)));
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private boolean nz(BigDecimal v) {
        return v != null && v.compareTo(BigDecimal.ZERO) != 0;
    }

    private String formatDate(LocalDate d) {
        return d == null ? "" : d.toString();
    }

    private String deductionLabel(PayrollDeduction d) {
        if (d.getLibelle() != null && !d.getLibelle().isBlank()) return d.getLibelle().trim();
        return safe(d.getCodeDeduction());
    }

    private EmploiEmploye resolveEmploiForDate(Map<String, EmploiEmploye> cache, Long employeId, LocalDate date) {
        String key = employeId + "|" + date;
        if (cache.containsKey(key)) return cache.get(key);
        EmploiEmploye emploi = emploiEmployeRepository.findActiveForDate(employeId, date).stream().findFirst().orElse(null);
        cache.put(key, emploi);
        return emploi;
    }

    private String formatAmount(BigDecimal value) {
        return nvl(value).setScale(2, RoundingMode.HALF_UP).toString();
    }

    private double toDouble(BigDecimal v) {
        return nvl(v).doubleValue();
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }

    private String renderTotal(PayrollDeductionDetailsTotalDTO t) {
        StringJoiner j = new StringJoiner(" | ");
        for (Map.Entry<String, BigDecimal> e : t.getMontantsByDate().entrySet()) {
            j.add(e.getKey() + "=" + formatAmount(e.getValue()));
        }
        j.add("SUPPL=" + formatAmount(t.getMontantSupplementaire()));
        j.add("AUTRE=" + formatAmount(t.getMontantAutreRevenu()));
        j.add("BRUT=" + formatAmount(t.getMontantBrut()));
        j.add("DEDUCTIONS=" + formatAmount(t.getMontantDeductions()));
        j.add("NET=" + formatAmount(t.getMontantNetAPayer()));
        return j.toString();
    }

    private Payroll.StatutPayroll parseStatut(String statut) {
        if (statut == null || statut.isBlank()) return null;
        try {
            return Payroll.StatutPayroll.valueOf(statut.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            return null;
        }
    }

    private PresenceReportHeaderDTO buildHeader(Long entrepriseId) {
        Entreprise entreprise = null;
        if (entrepriseId != null) entreprise = entrepriseRepository.findById(entrepriseId).orElse(null);
        if (entreprise == null) entreprise = entrepriseRepository.findFirstByEntrepriseMereIsNullOrderByIdAsc().orElse(null);
        if (entreprise == null) entreprise = entrepriseRepository.findFirstByActif("Y").orElse(null);
        if (entreprise == null) return new PresenceReportHeaderDTO();
        return new PresenceReportHeaderDTO(
                entreprise.getId(), entreprise.getCodeEntreprise(), entreprise.getNomEntreprise(), entreprise.getNomLegal(),
                entreprise.getAdresse(), entreprise.getVille(), entreprise.getEtat(), entreprise.getPays(), entreprise.getCodePostal(),
                entreprise.getTelephone1(), entreprise.getTelephone2(), entreprise.getTelephone3(), entreprise.getFax(), entreprise.getCourriel(),
                entreprise.getLogoUrl(), null, null
        );
    }

    private String buildCriteriaSummary(LocalDate dateDebut, LocalDate dateFin, Long entrepriseId, Payroll.StatutPayroll statut) {
        List<String> parts = new ArrayList<>();
        parts.add("Dates paie(date fin): " + dateDebut + " / " + dateFin);
        if (entrepriseId != null) {
            Entreprise e = entrepriseRepository.findById(entrepriseId).orElse(null);
            parts.add("Entreprise: " + (e != null ? safe(e.getNomEntreprise()) : entrepriseId));
        }
        if (statut != null) parts.add("Statut: " + statut.name());
        return String.join(" | ", parts);
    }

    private static final class GroupAccumulator {
        private final Long uniteId;
        private final String uniteCode;
        private final String uniteNom;
        private final Map<Long, PayrollDeductionDetailsRowDTO> rowsByEmployeId;
        private final PayrollDeductionDetailsTotalDTO total;

        private GroupAccumulator(Long uniteId,
                                 String uniteCode,
                                 String uniteNom,
                                 Map<Long, PayrollDeductionDetailsRowDTO> rowsByEmployeId,
                                 PayrollDeductionDetailsTotalDTO total) {
            this.uniteId = uniteId;
            this.uniteCode = uniteCode;
            this.uniteNom = uniteNom;
            this.rowsByEmployeId = rowsByEmployeId;
            this.total = total;
        }
    }
}
