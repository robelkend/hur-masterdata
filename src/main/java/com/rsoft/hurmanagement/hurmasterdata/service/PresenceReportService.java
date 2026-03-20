package com.rsoft.hurmanagement.hurmasterdata.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Chunk;
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
import com.rsoft.hurmanagement.hurmasterdata.dto.report.PresenceReportStatsDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.EmploiEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.Employe;
import com.rsoft.hurmanagement.hurmasterdata.entity.EmployeSalaire;
import com.rsoft.hurmanagement.hurmasterdata.entity.Entreprise;
import com.rsoft.hurmanagement.hurmasterdata.entity.Horaire;
import com.rsoft.hurmanagement.hurmasterdata.entity.HoraireDt;
import com.rsoft.hurmanagement.hurmasterdata.entity.HoraireSpecial;
import com.rsoft.hurmanagement.hurmasterdata.entity.JourConge;
import com.rsoft.hurmanagement.hurmasterdata.entity.PresenceEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.RegimePaie;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.UniteOrganisationnelle;
import com.rsoft.hurmanagement.hurmasterdata.repository.CongeEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmploiEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EntrepriseRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.EmployeSalaireRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.HoraireDtRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.HoraireSpecialRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.JourCongeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.PresenceEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.RegimePaieRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.TypeEmployeRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.UniteOrganisationnelleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STShd;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
public class PresenceReportService {

    private final PresenceEmployeRepository presenceEmployeRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final EmployeRepository employeRepository;
    private final EmployeSalaireRepository employeSalaireRepository;
    private final EmploiEmployeRepository emploiEmployeRepository;
    private final JourCongeRepository jourCongeRepository;
    private final CongeEmployeRepository congeEmployeRepository;
    private final HoraireSpecialRepository horaireSpecialRepository;
    private final HoraireDtRepository horaireDtRepository;
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
                                                 List<Long> uniteOrganisationnelleId,
                                                 List<Long> typeEmployeId,
                                                 Long gestionnaireId,
                                                 Long employeId,
                                                 String actif,
                                                 List<Long> regimePaieIds,
                                                 boolean showAbsences,
                                                 boolean showConges,
                                                 boolean showOffs,
                                                 boolean showFeries) {
        validateDateRange(dateDebut, dateFin);
        List<Long> safeRegimeIds = (regimePaieIds == null || regimePaieIds.isEmpty()) ? null : regimePaieIds;
        List<Long> safeUniteIds = (uniteOrganisationnelleId == null || uniteOrganisationnelleId.isEmpty()) ? null : uniteOrganisationnelleId;
        List<Long> safeTypeIds = (typeEmployeId == null || typeEmployeId.isEmpty()) ? null : typeEmployeId;
        String nightFilter = (nuit != null && !nuit.isBlank()) ? nuit.trim().toUpperCase() : null;
        String actifFilter = (actif != null && !actif.isBlank()) ? actif.trim().toUpperCase() : null;

        List<PresenceEmploye> presences = presenceEmployeRepository.findForPresenceReport(
                dateDebut,
                dateFin,
                entrepriseId,
                employeId,
                actifFilter,
                safeTypeIds,
                safeUniteIds,
                gestionnaireId,
                nightFilter,
                safeRegimeIds
        );
        Map<String, Integer> presencesByEmployeDate = countPresencesByEmployeDate(presences);

        PresenceReportHeaderDTO header = buildHeader(entrepriseId);
        header.setGeneratedAt(LocalDateTime.now().format(DATE_TIME_FORMAT));
        header.setCriteriaSummary(this.buildCriteriaSummary(
                dateDebut,
                dateFin,
                entrepriseId,
                nightFilter,
                safeUniteIds,
                safeTypeIds,
                gestionnaireId,
                employeId,
                actifFilter,
                safeRegimeIds
        ));
        List<PresenceReportRowDTO> rows = new ArrayList<>(buildRows(presences, presencesByEmployeDate, showAbsences, showConges));
        rows.addAll(buildMissingRows(
                dateDebut,
                dateFin,
                entrepriseId,
                employeId,
                actifFilter,
                safeTypeIds,
                safeUniteIds,
                gestionnaireId,
                nightFilter,
                presences,
                presencesByEmployeDate,
                showAbsences,
                showConges,
                showOffs,
                showFeries
        ));
        sortAndReindex(rows);
        PresenceReportStatsDTO stats = buildStats(rows);

        return new PresenceReportResponseDTO(header, rows, stats);
    }

    public byte[] exportPdf(PresenceReportResponseDTO report, LocalDate dateDebut, LocalDate dateFin, Integer textSize) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            float exportFontSize = resolveExportFontSize(textSize);
            Document document = new Document(PageSize.LETTER, 18, 18, 18, 18);
            PdfWriter.getInstance(document, out);
            document.open();

            addPdfHeader(document, report.getHeader(), dateDebut, dateFin, exportFontSize);
            addPdfTable(document, report.getRows(), exportFontSize);
            addPdfStats(document, report.getStats(), report.getHeader(), exportFontSize);

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("presenceReport.error.pdf", ex);
        }
    }

    public byte[] exportXlsx(PresenceReportResponseDTO report, LocalDate dateDebut, LocalDate dateFin, Integer textSize) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            short exportFontSize = (short) Math.round(resolveExportFontSize(textSize));
            Sheet sheet = workbook.createSheet("Feuille de presence");
            int rowIndex = 0;
            sheet.setDefaultRowHeightInPoints((short) Math.max(12, exportFontSize + 3));

            rowIndex = writeXlsxHeader(sheet, report.getHeader(), dateDebut, dateFin, rowIndex, exportFontSize);
            rowIndex++;
            rowIndex = writeXlsxTable(sheet, report.getRows(), rowIndex, exportFontSize);
            rowIndex++;
            writeXlsxStats(sheet, report.getStats(), report.getHeader(), rowIndex, exportFontSize);

            autoSize(sheet, 12);
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("presenceReport.error.xlsx", ex);
        }
    }

    public byte[] exportDocx(PresenceReportResponseDTO report, LocalDate dateDebut, LocalDate dateFin, Integer textSize) {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int exportFontSize = Math.round(resolveExportFontSize(textSize));
            addDocxHeader(document, report.getHeader(), dateDebut, dateFin, exportFontSize);
            addDocxTable(document, report.getRows(), exportFontSize);
            addDocxStats(document, report.getStats(), report.getHeader(), exportFontSize);
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

    private List<PresenceReportRowDTO> buildRows(List<PresenceEmploye> presences,
                                                 Map<String, Integer> presencesByEmployeDate,
                                                 boolean showAbsences,
                                                 boolean showConges) {
        List<PresenceReportRowDTO> rows = new ArrayList<>();
        Map<String, UniteOrganisationnelle> uniteCache = new HashMap<>();
        for (PresenceEmploye presence : presences) {
            PresenceReportRowDTO row = new PresenceReportRowDTO();
            fillUniteInfos(row, presence, uniteCache);
            row.setCodeEmploye(presence.getEmploye() != null ? presence.getEmploye().getCodeEmploye() : null);
            row.setNomEmploye(buildEmployeName(presence));
            boolean isNight = presence.getDateDepart() != null && presence.getDateDepart().isAfter(presence.getDateJour());
            row.setNuit(isNight ? "OUI" : "NON");
            row.setHoraireSpecialNuit(resolveHoraireSpecialNuit(presence));
            String offFlag = resolveFlag(presence, "off");
            String ferieFlag = resolveFlag(presence, "ferie");
            String congeFlag = resolveFlag(presence, "conge");
            String absenceFlag = isAbsent(presence) && !isYes(offFlag) && !isYes(ferieFlag) && !isYes(congeFlag) ? "OUI" : "NON";
            row.setOff(offFlag);
            row.setFerie(ferieFlag);
            row.setConge(congeFlag);
            row.setAbsence(absenceFlag);

            if (isYes(absenceFlag) && !isYes(offFlag) && !isYes(ferieFlag)) {
                // For true absence rows, show planned schedule in debut/fin.
                row.setDateDebut(presence.getDateJour());
                row.setHeureDebut(firstNotBlank(presence.getHeureDebutPrevue(), presence.getHeureArrivee()));
                row.setDateFin(resolvePlannedDateFin(presence));
                row.setHeureFin(firstNotBlank(presence.getHeureFinPrevue(), presence.getHeureDepart()));
            } else {
                row.setDateDebut(presence.getDateJour());
                row.setHeureDebut(presence.getHeureArrivee());
                row.setDateFin(presence.getDateDepart() != null ? presence.getDateDepart() : presence.getDateJour());
                row.setHeureFin(presence.getHeureDepart());
            }
            row.setNbPresencesJour(resolvePresenceCount(presence.getEmploye(), presence.getDateJour(), presencesByEmployeDate));
            row.setIssueLevel(resolveIssueLevel(row, presence));
            row.setNbHeures(resolveNbHeures(presence));
            rows.add(row);
        }
        return rows;
    }

    private PresenceReportStatsDTO buildStats(List<PresenceReportRowDTO> rows) {
        int total = rows.size();
        int presencesCount = 0;
        int offs = 0;
        int conges = 0;
        int feries = 0;
        int absences = 0;
        BigDecimal totalMinutes = BigDecimal.ZERO;

        for (PresenceReportRowDTO row : rows) {
            if (isYes(row.getOff())) {
                offs++;
            } else if (isYes(row.getConge())) {
                conges++;
            } else if (isYes(row.getFerie())) {
                feries++;
            } else if (isYes(row.getAbsence())) {
                absences++;
            } else {
                presencesCount++;
            }
            BigDecimal hours = row.getNbHeures();
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

    private List<PresenceReportRowDTO> buildMissingRows(LocalDate dateDebut,
                                                        LocalDate dateFin,
                                                        Long entrepriseId,
                                                        Long employeId,
                                                        String actifFilter,
                                                        List<Long> typeEmployeIds,
                                                        List<Long> uniteOrganisationnelleIds,
                                                        Long gestionnaireId,
                                                        String nightFilter,
                                                        List<PresenceEmploye> presences,
                                                        Map<String, Integer> presencesByEmployeDate,
                                                        boolean showAbsences,
                                                        boolean showConges,
                                                        boolean showOffs,
                                                        boolean showFeries) {
        if ("N".equalsIgnoreCase(actifFilter)) {
            return Collections.emptyList();
        }

        Set<String> presentKeys = new HashSet<>();
        for (PresenceEmploye p : presences) {
            Long id = p.getEmploye() != null ? p.getEmploye().getId() : null;
            if (id != null && p.getDateJour() != null) {
                presentKeys.add(id + "|" + p.getDateJour());
            }
        }

        List<EmploiEmploye> emplois = emploiEmployeRepository.findEligibleForAbsence(entrepriseId, employeId);
        List<PresenceReportRowDTO> rows = new ArrayList<>();

        for (EmploiEmploye emploi : emplois) {
            if (!"Y".equalsIgnoreCase(emploi.getPrincipal())) {
                continue;
            }
            if (emploi.getStatutEmploi() != EmploiEmploye.StatutEmploi.ACTIF) {
                continue;
            }
            if (!matchesReportFilters(emploi, typeEmployeIds, uniteOrganisationnelleIds, gestionnaireId)) {
                continue;
            }
            Employe employe = emploi.getEmploye();
            if (employe == null || employe.getId() == null) {
                continue;
            }

            for (LocalDate date = dateDebut; !date.isAfter(dateFin); date = date.plusDays(1)) {
                String key = employe.getId() + "|" + date;
                if (presentKeys.contains(key)) {
                    continue;
                }
                boolean offDay = isOffDay(emploi, date);
                boolean ferieDay = jourCongeRepository.existsByDateCongeAndActif(date, JourConge.Actif.Y);

                boolean onConge = congeEmployeRepository.existsCongeForDate(employe.getId(), date);
                boolean absenceDay = !offDay && !ferieDay && !onConge;
                boolean includeFerie = ferieDay && showFeries;
                boolean includeOff = !ferieDay && offDay && showOffs;
                boolean includeConge = !ferieDay && !offDay && onConge && showConges;
                boolean includeAbsence = absenceDay && showAbsences;
                if (!includeFerie && !includeOff && !includeConge && !includeAbsence) {
                    continue;
                }
                PlannedSchedule schedule = resolvePlannedSchedule(emploi, date);
                String night = schedule.isNight ? "OUI" : "NON";
                if (nightFilter != null && !nightFilter.equalsIgnoreCase(schedule.isNight ? "Y" : "N")) {
                    continue;
                }

                PresenceReportRowDTO row = new PresenceReportRowDTO();
                row.setCodeEmploye(employe.getCodeEmploye());
                row.setNomEmploye(buildEmployeName(employe));
                row.setUniteOrganisationnelleId(emploi.getUniteOrganisationnelle() != null ? emploi.getUniteOrganisationnelle().getId() : null);
                row.setUniteOrganisationnelleCode(emploi.getUniteOrganisationnelle() != null ? emploi.getUniteOrganisationnelle().getCode() : null);
                row.setUniteOrganisationnelleNom(emploi.getUniteOrganisationnelle() != null ? emploi.getUniteOrganisationnelle().getNom() : null);
                row.setDateDebut(date);
                row.setHeureDebut(schedule.heureDebut);
                row.setDateFin(schedule.isNight ? date.plusDays(1) : date);
                row.setHeureFin(schedule.heureFin);
                row.setNuit(night);
                row.setHoraireSpecialNuit(schedule.isFromSpecial && schedule.isNight ? "OUI" : "NON");
                row.setOff(includeOff ? "OUI" : "NON");
                row.setFerie(includeFerie ? "OUI" : "NON");
                row.setConge(includeConge ? "OUI" : "NON");
                row.setAbsence(includeAbsence ? "OUI" : "NON");
                row.setNbPresencesJour(resolvePresenceCount(employe, date, presencesByEmployeDate));
                row.setIssueLevel(resolveIssueLevel(row, null));
                row.setNbHeures(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
                rows.add(row);
            }
        }

        return rows;
    }

    private void sortAndReindex(List<PresenceReportRowDTO> rows) {
        rows.sort(Comparator
                .comparing((PresenceReportRowDTO row) -> safe(row.getCodeEmploye()))
                .thenComparing(row -> row.getDateDebut() != null ? row.getDateDebut() : LocalDate.MIN)
                .thenComparing(row -> safe(row.getHeureDebut())));
        int index = 1;
        for (PresenceReportRowDTO row : rows) {
            row.setRang(index++);
        }
    }

    private boolean matchesReportFilters(EmploiEmploye emploi,
                                         List<Long> typeEmployeIds,
                                         List<Long> uniteOrganisationnelleIds,
                                         Long gestionnaireId) {
        if (typeEmployeIds != null) {
            Long typeId = emploi.getTypeEmploye() != null ? emploi.getTypeEmploye().getId() : null;
            if (typeId == null || !typeEmployeIds.contains(typeId)) {
                return false;
            }
        }
        if (uniteOrganisationnelleIds != null) {
            Long uniteId = emploi.getUniteOrganisationnelle() != null ? emploi.getUniteOrganisationnelle().getId() : null;
            if (uniteId == null || !uniteOrganisationnelleIds.contains(uniteId)) {
                return false;
            }
        }
        if (gestionnaireId != null) {
            Long managerId = emploi.getGestionnaire() != null ? emploi.getGestionnaire().getId() : null;
            if (managerId == null || !gestionnaireId.equals(managerId)) {
                return false;
            }
        }
        return true;
    }

    private boolean isOffDay(EmploiEmploye emploi, LocalDate date) {
        if (emploi == null || date == null) {
            return false;
        }
        int dayIndex = date.getDayOfWeek().getValue();
        return (emploi.getJourOff1() != null && emploi.getJourOff1() == dayIndex)
                || (emploi.getJourOff2() != null && emploi.getJourOff2() == dayIndex)
                || (emploi.getJourOff3() != null && emploi.getJourOff3() == dayIndex);
    }

    private PlannedSchedule resolvePlannedSchedule(EmploiEmploye emploi, LocalDate date) {
        if (emploi == null || emploi.getEmploye() == null || emploi.getEmploye().getId() == null) {
            return PlannedSchedule.empty();
        }

        HoraireSpecial special = resolveHoraireSpecial(emploi, date);
        if (special != null) {
            String debut = firstNotBlank(special.getHeureDebut());
            String fin = firstNotBlank(special.getHeureFin());
            boolean night = crossesMidnight(debut, fin);
            return new PlannedSchedule(debut, fin, night, true);
        }

        Horaire horaire = emploi.getHoraire();
        HoraireDt horaireDt = (horaire != null && horaire.getId() != null)
                ? horaireDtRepository.findByHoraireIdAndJour(horaire.getId(), date.getDayOfWeek().getValue())
                : null;

        String debut = firstNotBlank(
                horaireDt != null ? horaireDt.getHeureDebutJour() : null,
                horaireDt != null ? horaireDt.getHeureDebutNuit() : null,
                horaire != null ? horaire.getHeureDebut() : null,
                horaire != null ? horaire.getHeureDebutNuit() : null
        );
        String fin = firstNotBlank(
                horaireDt != null ? horaireDt.getHeureFinJour() : null,
                horaireDt != null ? horaireDt.getHeureFinNuit() : null,
                horaire != null ? horaire.getHeureFin() : null,
                horaire != null ? horaire.getHeureFinNuit() : null
        );
        boolean night = crossesMidnight(debut, fin);
        return new PlannedSchedule(debut, fin, night, false);
    }

    private HoraireSpecial resolveHoraireSpecial(EmploiEmploye emploi, LocalDate date) {
        List<HoraireSpecial> specials = horaireSpecialRepository.findActiveByEmployeIdAndDate(emploi.getEmploye().getId(), date);
        if (specials == null || specials.isEmpty()) {
            return null;
        }
        for (HoraireSpecial special : specials) {
            Long emploiId = special.getEmploiEmploye() != null ? special.getEmploiEmploye().getId() : null;
            if (emploiId != null && emploiId.equals(emploi.getId())) {
                return special;
            }
        }
        return specials.get(0);
    }

    private boolean crossesMidnight(String heureDebut, String heureFin) {
        if (heureDebut == null || heureFin == null || heureDebut.isBlank() || heureFin.isBlank()) {
            return false;
        }
        try {
            return LocalTime.parse(heureFin).isBefore(LocalTime.parse(heureDebut));
        } catch (Exception ex) {
            return false;
        }
    }

    private void fillUniteInfos(PresenceReportRowDTO row, PresenceEmploye presence, Map<String, UniteOrganisationnelle> uniteCache) {
        if (row == null || presence == null || presence.getEmploye() == null || presence.getEmploye().getId() == null || presence.getDateJour() == null) {
            return;
        }
        String key = presence.getEmploye().getId() + "|" + presence.getDateJour() + "|" + (presence.getRegimePaie() != null ? presence.getRegimePaie().getId() : 0L);
        UniteOrganisationnelle unite = uniteCache.get(key);
        if (unite == null && !uniteCache.containsKey(key)) {
            EmploiEmploye emploi = resolveEmploiForPresence(presence);
            if (emploi != null) {
                unite = emploi.getUniteOrganisationnelle();
                if (row.getUniteOrganisationnelleId() == null) {
                    row.setUniteOrganisationnelleId(unite != null ? unite.getId() : null);
                }
                if (row.getUniteOrganisationnelleCode() == null || row.getUniteOrganisationnelleCode().isBlank()) {
                    row.setUniteOrganisationnelleCode(unite != null ? unite.getCode() : null);
                }
                if (row.getUniteOrganisationnelleNom() == null || row.getUniteOrganisationnelleNom().isBlank()) {
                    row.setUniteOrganisationnelleNom(unite != null ? unite.getNom() : null);
                }
            }
            uniteCache.put(key, unite);
        }
        if (unite == null) {
            return;
        }
        row.setUniteOrganisationnelleId(unite.getId());
        row.setUniteOrganisationnelleCode(unite.getCode());
        row.setUniteOrganisationnelleNom(unite.getNom());
    }

    private EmploiEmploye resolveEmploiForPresence(PresenceEmploye presence) {
        if (presence == null || presence.getEmploye() == null || presence.getEmploye().getId() == null) {
            return null;
        }
        Long employeId = presence.getEmploye().getId();
        Long regimePaieId = presence.getRegimePaie() != null ? presence.getRegimePaie().getId() : null;

        if (regimePaieId != null) {
            EmployeSalaire salaire = employeSalaireRepository
                    .findFirstByEmployeIdAndActifAndRegimePaieIdOrderByPrincipalDescIdDesc(employeId, "Y", regimePaieId)
                    .orElse(null);
            if (salaire != null && salaire.getEmploi() != null && "Y".equalsIgnoreCase(salaire.getEmploi().getPrincipal())) {
                return salaire.getEmploi();
            }
        }

        List<EmploiEmploye> emplois = emploiEmployeRepository.findActiveForDate(employeId, presence.getDateJour());
        if (emplois.isEmpty()) {
            return null;
        }
        for (EmploiEmploye emploi : emplois) {
            if ("Y".equalsIgnoreCase(emploi.getPrincipal())) {
                return emploi;
            }
        }
        return emplois.get(0);
    }

    private String buildEmployeName(PresenceEmploye presence) {
        if (presence.getEmploye() == null) {
            return null;
        }
        String nom = presence.getEmploye().getNom() != null ? presence.getEmploye().getNom().trim() : "";
        String prenom = presence.getEmploye().getPrenom() != null ? presence.getEmploye().getPrenom().trim() : "";
        return (nom + " " + prenom).trim();
    }

    private String buildEmployeName(Employe employe) {
        if (employe == null) {
            return null;
        }
        String nom = employe.getNom() != null ? employe.getNom().trim() : "";
        String prenom = employe.getPrenom() != null ? employe.getPrenom().trim() : "";
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

    private void addPdfHeader(Document document, PresenceReportHeaderDTO header, LocalDate dateDebut, LocalDate dateFin, float fontSize) throws Exception {
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
        left.addElement(centeredParagraph("RAPPORT DE PRESENCES", FontFactory.getFont(FontFactory.HELVETICA_BOLD, fontSize + 1)));
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
        document.add(Chunk.NEWLINE);
    }

    private void addPdfTable(Document document, List<PresenceReportRowDTO> rows, float fontSize) throws Exception {
        PdfPTable table = new PdfPTable(12);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.35f, 2.35f, 1.0f, 1.0f, 0.55f, 0.75f, 0.55f, 0.55f, 0.55f, 0.55f, 0.7f, 0.7f});

        addPdfHeaderCell(table, "", fontSize);
        addPdfHeaderCell(table, "EMPLOYES", fontSize);
        addPdfHeaderCell(table, "DEBUT", fontSize);
        addPdfHeaderCell(table, "FIN", fontSize);
        addPdfHeaderCell(table, "NUIT", fontSize);
        addPdfHeaderCell(table, "HS NUIT", fontSize);
        addPdfHeaderCell(table, "OFF", fontSize);
        addPdfHeaderCell(table, "FERIE", fontSize);
        addPdfHeaderCell(table, "CONGE", fontSize);
        addPdfHeaderCell(table, "ABS", fontSize);
        addPdfHeaderCell(table, "NB HE", fontSize);
        addPdfHeaderCell(table, "DOUBLON", fontSize);

        for (PresenceReportRowDTO row : rows) {
            Color rowColor = resolveIssueColor(row);
            table.addCell(buildCell(row.getRang() != null ? String.valueOf(row.getRang()) : "", fontSize, rowColor));
            table.addCell(buildCell(row.getCodeEmploye() + " - " + safe(row.getNomEmploye()), fontSize, rowColor));
            table.addCell(buildCell(formatDateTime(row.getDateDebut(), row.getHeureDebut()), fontSize, rowColor));
            table.addCell(buildCell(formatDateTime(row.getDateFin(), row.getHeureFin()), fontSize, rowColor));
            table.addCell(buildCell(safe(row.getNuit()), fontSize, rowColor));
            table.addCell(buildCell(safe(row.getHoraireSpecialNuit()), fontSize, rowColor));
            table.addCell(buildCell(safe(row.getOff()), fontSize, rowColor));
            table.addCell(buildCell(safe(row.getFerie()), fontSize, rowColor));
            table.addCell(buildCell(safe(row.getConge()), fontSize, rowColor));
            table.addCell(buildCell(safe(row.getAbsence()), fontSize, rowColor));
            table.addCell(buildCell(row.getNbHeures() != null ? row.getNbHeures().toString() : "", fontSize, rowColor));
            table.addCell(buildCell(row.getNbPresencesJour() != null ? String.valueOf(row.getNbPresencesJour()) : "0", fontSize, rowColor));
        }
        document.add(table);
    }

    private void addPdfStats(Document document, PresenceReportStatsDTO stats, PresenceReportHeaderDTO header, float fontSize) throws Exception {
        document.add(Chunk.NEWLINE);
        Paragraph p = new Paragraph("", FontFactory.getFont(FontFactory.HELVETICA, fontSize));
        p.add("TOTAL: " + stats.getTotal() + "   ");
        p.add("PRESENCES: " + stats.getPresences() + "   ");
        p.add("OFFS: " + stats.getOffs() + "   ");
        p.add("CONGES: " + stats.getConges() + "   ");
        p.add("FERIES: " + stats.getFeries() + "   ");
        p.add("ABSENCES: " + stats.getAbsences() + "   ");
        p.add("% ABSENCE: " + stats.getTauxAbsence());
        document.add(p);
        Paragraph p2 = new Paragraph("TOTAL SUPPLEMENTAIRE (MINUTES): " + stats.getTotalSupplementaireMinutes(),
                FontFactory.getFont(FontFactory.HELVETICA, fontSize));
        document.add(p2);
        if (header.getCriteriaSummary() != null && !header.getCriteriaSummary().isBlank()) {
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("CRITERES: " + header.getCriteriaSummary(), FontFactory.getFont(FontFactory.HELVETICA, fontSize)));
        }
    }

    private int writeXlsxHeader(Sheet sheet, PresenceReportHeaderDTO header, LocalDate dateDebut, LocalDate dateFin, int rowIndex, short fontSize) {
        Workbook workbook = sheet.getWorkbook();
        CellStyle centerStyle = workbook.createCellStyle();
        centerStyle.setAlignment(HorizontalAlignment.CENTER);
        Font headerFont = workbook.createFont();
        headerFont.setFontHeightInPoints((short) Math.max(4, fontSize));
        centerStyle.setFont(headerFont);

        rowIndex = writeCenteredMergedRow(sheet, rowIndex, safe(header.getNomEntreprise()), centerStyle);
        rowIndex = writeCenteredMergedRowIfNotBlank(sheet, rowIndex, buildAddressLine(header), centerStyle);
        rowIndex = writeCenteredMergedRowIfNotBlank(sheet, rowIndex, buildTelephonesLine(header), centerStyle);
        rowIndex = writeCenteredMergedRowIfNotBlank(sheet, rowIndex, buildFaxLine(header), centerStyle);
        rowIndex = writeCenteredMergedRow(sheet, rowIndex, "RAPPORT DE PRESENCES", centerStyle);

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
        for (int col = 0; col <= 11; col++) {
            Cell cell = separatorRow.createCell(col);
            cell.setCellStyle(separatorStyle);
        }
        addXlsxLogo(sheet, header.getLogoUrl(), 8, 0);
        return rowIndex;
    }

    private int writeXlsxTable(Sheet sheet, List<PresenceReportRowDTO> rows, int rowIndex, short fontSize) {
        Workbook workbook = sheet.getWorkbook();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) Math.max(4, fontSize));
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);

        Font bodyFont = workbook.createFont();
        bodyFont.setFontHeightInPoints((short) Math.max(4, fontSize));
        CellStyle bodyStyle = workbook.createCellStyle();
        bodyStyle.setFont(bodyFont);
        Map<String, CellStyle> issueStyles = buildXlsxIssueStyles(workbook, bodyStyle, bodyFont);

        Row headerRow = sheet.createRow(rowIndex++);
        String[] headers = {"", "EMPLOYES", "DEBUT", "FIN", "NUIT", "HS NUIT", "OFF", "FERIE", "CONGE", "ABS", "NB HE", "DOUBLON"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        for (PresenceReportRowDTO row : rows) {
            Row r = sheet.createRow(rowIndex++);
            CellStyle rowStyle = resolveXlsxRowStyle(row, bodyStyle, issueStyles);
            setXlsxCell(r, 0, row.getRang() != null ? String.valueOf(row.getRang()) : "0", rowStyle);
            setXlsxCell(r, 1, row.getCodeEmploye() + " - " + safe(row.getNomEmploye()), rowStyle);
            setXlsxCell(r, 2, formatDateTime(row.getDateDebut(), row.getHeureDebut()), rowStyle);
            setXlsxCell(r, 3, formatDateTime(row.getDateFin(), row.getHeureFin()), rowStyle);
            setXlsxCell(r, 4, safe(row.getNuit()), rowStyle);
            setXlsxCell(r, 5, safe(row.getHoraireSpecialNuit()), rowStyle);
            setXlsxCell(r, 6, safe(row.getOff()), rowStyle);
            setXlsxCell(r, 7, safe(row.getFerie()), rowStyle);
            setXlsxCell(r, 8, safe(row.getConge()), rowStyle);
            setXlsxCell(r, 9, safe(row.getAbsence()), rowStyle);
            setXlsxCell(r, 10, row.getNbHeures() != null ? row.getNbHeures().toString() : "", rowStyle);
            setXlsxCell(r, 11, row.getNbPresencesJour() != null ? String.valueOf(row.getNbPresencesJour()) : "0", rowStyle);
        }
        return rowIndex;
    }

    private void writeXlsxStats(Sheet sheet, PresenceReportStatsDTO stats, PresenceReportHeaderDTO header, int rowIndex, short fontSize) {
        Workbook workbook = sheet.getWorkbook();
        Font statsFont = workbook.createFont();
        statsFont.setFontHeightInPoints((short) Math.max(4, fontSize));
        CellStyle statsStyle = workbook.createCellStyle();
        statsStyle.setFont(statsFont);

        Row row = sheet.createRow(rowIndex++);
        setXlsxCell(row, 0, "TOTAL", statsStyle);
        setXlsxCell(row, 1, String.valueOf(stats.getTotal()), statsStyle);
        setXlsxCell(row, 2, "PRESENCES", statsStyle);
        setXlsxCell(row, 3, String.valueOf(stats.getPresences()), statsStyle);
        setXlsxCell(row, 4, "OFFS", statsStyle);
        setXlsxCell(row, 5, String.valueOf(stats.getOffs()), statsStyle);
        setXlsxCell(row, 6, "CONGES", statsStyle);
        setXlsxCell(row, 7, String.valueOf(stats.getConges()), statsStyle);
        Row row2 = sheet.createRow(rowIndex++);
        setXlsxCell(row2, 0, "FERIES", statsStyle);
        setXlsxCell(row2, 1, String.valueOf(stats.getFeries()), statsStyle);
        setXlsxCell(row2, 2, "ABSENCES", statsStyle);
        setXlsxCell(row2, 3, String.valueOf(stats.getAbsences()), statsStyle);
        setXlsxCell(row2, 4, "% ABSENCE", statsStyle);
        setXlsxCell(row2, 5, stats.getTauxAbsence() != null ? stats.getTauxAbsence().toString() : "0", statsStyle);
        Row row3 = sheet.createRow(rowIndex++);
        setXlsxCell(row3, 0, "TOTAL SUPPLEMENTAIRE (MINUTES)", statsStyle);
        setXlsxCell(row3, 1, stats.getTotalSupplementaireMinutes() != null ? stats.getTotalSupplementaireMinutes().toString() : "0", statsStyle);
        if (header.getCriteriaSummary() != null && !header.getCriteriaSummary().isBlank()) {
            Row rowCriteria = sheet.createRow(rowIndex);
            setXlsxCell(rowCriteria, 0, "CRITERES", statsStyle);
            setXlsxCell(rowCriteria, 1, header.getCriteriaSummary(), statsStyle);
        }
    }

    private void addDocxHeader(XWPFDocument document, PresenceReportHeaderDTO header, LocalDate dateDebut, LocalDate dateFin, int fontSize) {
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
        addDocxCenteredIfNotBlank(left, "RAPPORT DE PRESENCES", fontSize + 1);

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

    private void addDocxTable(XWPFDocument document, List<PresenceReportRowDTO> rows, int fontSize) {
        XWPFTable table = document.createTable(rows.size() + 1, 12);
        String[] headers = {"", "EMPLOYES", "DEBUT", "FIN", "NUIT", "HS NUIT", "OFF", "FERIE", "CONGE", "ABS", "NB HE", "DOUBLON"};
        for (int i = 0; i < headers.length; i++) {
            table.getRow(0).getCell(i).setText(headers[i]);
        }
        for (int i = 0; i < rows.size(); i++) {
            PresenceReportRowDTO row = rows.get(i);
            int r = i + 1;
            table.getRow(r).getCell(0).setText(row.getRang() != null ? row.getRang().toString() : "");
            table.getRow(r).getCell(1).setText(row.getCodeEmploye() + " - " + safe(row.getNomEmploye()));
            table.getRow(r).getCell(2).setText(formatDateTime(row.getDateDebut(), row.getHeureDebut()));
            table.getRow(r).getCell(3).setText(formatDateTime(row.getDateFin(), row.getHeureFin()));
            table.getRow(r).getCell(4).setText(safe(row.getNuit()));
            table.getRow(r).getCell(5).setText(safe(row.getHoraireSpecialNuit()));
            table.getRow(r).getCell(6).setText(safe(row.getOff()));
            table.getRow(r).getCell(7).setText(safe(row.getFerie()));
            table.getRow(r).getCell(8).setText(safe(row.getConge()));
            table.getRow(r).getCell(9).setText(safe(row.getAbsence()));
            table.getRow(r).getCell(10).setText(row.getNbHeures() != null ? row.getNbHeures().toString() : "");
            table.getRow(r).getCell(11).setText(row.getNbPresencesJour() != null ? String.valueOf(row.getNbPresencesJour()) : "0");
            applyDocxRowBackground(table.getRow(r).getTableCells(), resolveIssueColorHex(row));
        }
        applyDocxTableFontSize(table, fontSize);
    }

    private void addDocxStats(XWPFDocument document, PresenceReportStatsDTO stats, PresenceReportHeaderDTO header, int fontSize) {
        XWPFParagraph p = document.createParagraph();
        XWPFRun run = p.createRun();
        run.setFontSize(fontSize);
        run.setText("TOTAL: " + stats.getTotal() + " | PRESENCES: " + stats.getPresences() +
                " | OFFS: " + stats.getOffs() + " | CONGES: " + stats.getConges() +
                " | FERIES: " + stats.getFeries() + " | ABSENCES: " + stats.getAbsences() +
                " | % ABSENCE: " + stats.getTauxAbsence());
        if (header.getCriteriaSummary() != null && !header.getCriteriaSummary().isBlank()) {
            XWPFParagraph criteriaParagraph = document.createParagraph();
            XWPFRun criteriaRun = criteriaParagraph.createRun();
            criteriaRun.setFontSize(fontSize);
            criteriaRun.setText("CRITERES: " + header.getCriteriaSummary());
        }
    }

    private void addPdfSectionSeparator(Document document) throws Exception {
        PdfPTable separatorTable = new PdfPTable(1);
        separatorTable.setWidthPercentage(100);
        PdfPCell separatorCell = new PdfPCell(new Phrase(""));
        separatorCell.setBorder(Rectangle.BOTTOM);
        separatorCell.setBorderWidthBottom(0.8f);
        separatorCell.setPaddingTop(0);
        separatorCell.setPaddingBottom(2);
        separatorCell.setBorderWidthTop(0);
        separatorCell.setBorderWidthLeft(0);
        separatorCell.setBorderWidthRight(0);
        separatorTable.addCell(separatorCell);
        document.add(separatorTable);
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

    private void addPdfHeaderCell(PdfPTable table, String value, float fontSize) {
        PdfPCell cell = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, Math.max(4, fontSize))));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        table.addCell(cell);
    }

    private PdfPCell buildCell(String value, float fontSize, Color backgroundColor) {
        PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "", FontFactory.getFont(FontFactory.HELVETICA, Math.max(4, fontSize - 1))));
        if (backgroundColor != null) {
            cell.setBackgroundColor(backgroundColor);
        }
        return cell;
    }

    private void autoSize(Sheet sheet, int cols) {
        for (int i = 0; i < cols; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void addXlsxLogo(Sheet sheet, String logoUrl, int col, int row) {
        byte[] logoBytes = resolveLogoBytes(logoUrl);
        if (logoBytes == null) {
            return;
        }
        try {
            int pictureIdx = sheet.getWorkbook().addPicture(logoBytes, detectWorkbookPictureType(logoUrl, logoBytes));
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            ClientAnchor anchor = sheet.getWorkbook().getCreationHelper().createClientAnchor();
            anchor.setCol1(col);
            anchor.setRow1(row);
            Picture pict = drawing.createPicture(anchor, pictureIdx);
            pict.resize(1.2);
        } catch (Exception ignored) {
        }
    }

    private Paragraph centeredParagraph(String text, com.lowagie.text.Font font) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        return paragraph;
    }

    private int writeCenteredMergedRow(Sheet sheet, int rowIndex, String text, CellStyle style) {
        Row row = sheet.createRow(rowIndex);
        Cell cell = row.createCell(0);
        cell.setCellValue(text);
        cell.setCellStyle(style);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIndex, rowIndex, 0, 11));
        return rowIndex + 1;
    }

    private Map<String, Integer> countPresencesByEmployeDate(List<PresenceEmploye> presences) {
        Map<String, Integer> counts = new HashMap<>();
        if (presences == null || presences.isEmpty()) {
            return counts;
        }
        for (PresenceEmploye presence : presences) {
            if (presence == null || presence.getEmploye() == null || presence.getEmploye().getId() == null || presence.getDateJour() == null) {
                continue;
            }
            String key = buildEmployeDateKey(presence.getEmploye().getId(), presence.getDateJour());
            counts.merge(key, 1, Integer::sum);
        }
        return counts;
    }

    private Integer resolvePresenceCount(Employe employe, LocalDate dateJour, Map<String, Integer> counts) {
        if (employe == null || employe.getId() == null || dateJour == null || counts == null || counts.isEmpty()) {
            return 0;
        }
        return counts.getOrDefault(buildEmployeDateKey(employe.getId(), dateJour), 0);
    }

    private String buildEmployeDateKey(Long employeId, LocalDate dateJour) {
        return employeId + "|" + dateJour;
    }

    private String resolveIssueLevel(PresenceReportRowDTO row, PresenceEmploye presence) {
        if (row == null) {
            return "NONE";
        }
        if (hasErrorSignal(presence)) {
            return "ERROR";
        }
        if (isYes(row.getAbsence())) {
            return "ABSENCE";
        }
        if (row.getNbPresencesJour() != null && row.getNbPresencesJour() > 1) {
            return "DUPLICATE";
        }
        if (hasWarnSignal(presence)) {
            return "WARN";
        }
        return "NONE";
    }

    private String resolveIssueColorHex(PresenceReportRowDTO row) {
        if (row == null || row.getIssueLevel() == null) {
            return null;
        }
        String level = row.getIssueLevel().toUpperCase(Locale.ROOT);
        return switch (level) {
            case "ABSENCE" -> "FFF3F3";   // very pale red
            case "DUPLICATE" -> "FFF9EF"; // very pale orange
            case "WARN" -> "FFFEF5";      // very pale yellow
            case "ERROR" -> "F5F4FF";     // very pale violet
            default -> null;
        };
    }

    private Color resolveIssueColor(PresenceReportRowDTO row) {
        String hex = resolveIssueColorHex(row);
        if (hex == null) {
            return null;
        }
        try {
            return Color.decode("#" + hex);
        } catch (Exception ex) {
            return null;
        }
    }

    private Map<String, CellStyle> buildXlsxIssueStyles(Workbook workbook, CellStyle baseStyle, Font bodyFont) {
        Map<String, CellStyle> styles = new HashMap<>();
        styles.put("ABSENCE", createXlsxIssueStyle(workbook, baseStyle, bodyFont, "FFF3F3"));
        styles.put("DUPLICATE", createXlsxIssueStyle(workbook, baseStyle, bodyFont, "FFF9EF"));
        styles.put("WARN", createXlsxIssueStyle(workbook, baseStyle, bodyFont, "FFFEF5"));
        styles.put("ERROR", createXlsxIssueStyle(workbook, baseStyle, bodyFont, "F5F4FF"));
        return styles;
    }

    private CellStyle resolveXlsxRowStyle(PresenceReportRowDTO row, CellStyle defaultStyle, Map<String, CellStyle> issueStyles) {
        if (row == null || row.getIssueLevel() == null || issueStyles == null) {
            return defaultStyle;
        }
        return issueStyles.getOrDefault(row.getIssueLevel().toUpperCase(Locale.ROOT), defaultStyle);
    }

    private CellStyle createXlsxIssueStyle(Workbook workbook, CellStyle baseStyle, Font bodyFont, String hexColor) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(baseStyle);
        style.setFont(bodyFont);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        if (style instanceof XSSFCellStyle xssfStyle) {
            xssfStyle.setFillForegroundColor(new XSSFColor(Color.decode("#" + hexColor), null));
        } else {
            xssfStyleFallback(style, hexColor);
        }
        return style;
    }

    private void xssfStyleFallback(CellStyle style, String hexColor) {
        if ("FFF3F3".equalsIgnoreCase(hexColor)) {
            style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        } else if ("FFF9EF".equalsIgnoreCase(hexColor)) {
            style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        } else if ("FFFEF5".equalsIgnoreCase(hexColor)) {
            style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        } else if ("F5F4FF".equalsIgnoreCase(hexColor)) {
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        }
    }

    private void applyDocxRowBackground(List<XWPFTableCell> cells, String hexColor) {
        if (cells == null || cells.isEmpty() || hexColor == null || hexColor.isBlank()) {
            return;
        }
        for (XWPFTableCell cell : cells) {
            CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
            CTShd shd = tcPr.isSetShd() ? tcPr.getShd() : tcPr.addNewShd();
            shd.setVal(STShd.CLEAR);
            shd.setColor("auto");
            shd.setFill(hexColor);
        }
    }

    private boolean hasWarnSignal(PresenceEmploye presence) {
        return hasDetailSeverity(presence, "WARN") || hasWarnTypeErreur(presence);
    }

    private boolean hasErrorSignal(PresenceEmploye presence) {
        return hasDetailSeverity(presence, "ERROR") || hasProblemTypeErreur(presence);
    }

    @SuppressWarnings("unchecked")
    private boolean hasDetailSeverity(PresenceEmploye presence, String severity) {
        if (presence == null || severity == null || severity.isBlank()) {
            return false;
        }
        Map<String, Object> details = parseDetails(presence.getDetails());
        Object detailsNode = details.get("details");
        if (!(detailsNode instanceof List<?> detailsList)) {
            return false;
        }
        for (Object item : detailsList) {
            if (!(item instanceof Map<?, ?> mapItem)) {
                continue;
            }
            Object severityValue = ((Map<String, Object>) mapItem).get("severity");
            if (severityValue != null && severity.equalsIgnoreCase(String.valueOf(severityValue))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasWarnTypeErreur(PresenceEmploye presence) {
        if (presence == null || presence.getTypeErreur() == null) {
            return false;
        }
        return presence.getTypeErreur() == PresenceEmploye.TypeErreur.DUREE_TROP_COURTE
                || presence.getTypeErreur() == PresenceEmploye.TypeErreur.HORS_PLAGE
                || presence.getTypeErreur() == PresenceEmploye.TypeErreur.AMBIGU_18H
                || presence.getTypeErreur() == PresenceEmploye.TypeErreur.AMBIGU_6H;
    }

    private boolean hasProblemTypeErreur(PresenceEmploye presence) {
        if (presence == null || presence.getTypeErreur() == null) {
            return false;
        }
        PresenceEmploye.TypeErreur typeErreur = presence.getTypeErreur();
        if (typeErreur == PresenceEmploye.TypeErreur.VALIDE) {
            return false;
        }
        return !hasWarnTypeErreur(presence);
    }

    private int writeCenteredMergedRowIfNotBlank(Sheet sheet, int rowIndex, String text, CellStyle style) {
        if (text == null || text.isBlank()) {
            return rowIndex;
        }
        return writeCenteredMergedRow(sheet, rowIndex, text, style);
    }

    private void addIfNotBlank(PdfPCell cell, Paragraph paragraph) {
        if (paragraph == null) {
            return;
        }
        if (paragraph.getContent() != null && !paragraph.getContent().isBlank()) {
            cell.addElement(paragraph);
        }
    }

    private void addDocxCenteredIfNotBlank(XWPFParagraph paragraph, String text, int fontSize) {
        if (text == null || text.isBlank()) {
            return;
        }
        XWPFRun run = paragraph.createRun();
        run.addBreak();
        run.setFontSize(fontSize);
        run.setText(text);
    }

    private void applyDocxTableFontSize(XWPFTable table, int fontSize) {
        for (int r = 0; r < table.getNumberOfRows(); r++) {
            for (int c = 0; c < table.getRow(r).getTableCells().size(); c++) {
                XWPFParagraph paragraph = table.getRow(r).getCell(c).getParagraphArray(0);
                if (paragraph == null) {
                    continue;
                }
                for (XWPFRun run : paragraph.getRuns()) {
                    run.setFontSize(fontSize);
                }
            }
        }
    }

    private void setXlsxCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private static class PlannedSchedule {
        private final String heureDebut;
        private final String heureFin;
        private final boolean isNight;
        private final boolean isFromSpecial;

        private PlannedSchedule(String heureDebut, String heureFin, boolean isNight, boolean isFromSpecial) {
            this.heureDebut = heureDebut;
            this.heureFin = heureFin;
            this.isNight = isNight;
            this.isFromSpecial = isFromSpecial;
        }

        private static PlannedSchedule empty() {
            return new PlannedSchedule(null, null, false, false);
        }
    }

    private float resolveExportFontSize(Integer textSize) {
        if (textSize == null) {
            return 10f;
        }
        int size = Math.max(4, Math.min(17, textSize));
        return (float) size;
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

    private boolean isGif(byte[] bytes) {
        return bytes != null
                && bytes.length > 3
                && bytes[0] == 'G'
                && bytes[1] == 'I'
                && bytes[2] == 'F';
    }

    private String buildCriteriaSummary(LocalDate dateDebut,
                                        LocalDate dateFin,
                                        Long entrepriseId,
                                        String nuit,
                                        List<Long> uniteOrganisationnelleIds,
                                        List<Long> typeEmployeIds,
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
        if (uniteOrganisationnelleIds != null && !uniteOrganisationnelleIds.isEmpty()) {
            List<String> unites = new ArrayList<>();
            for (Long id : uniteOrganisationnelleIds) {
                UniteOrganisationnelle unite = uniteOrganisationnelleRepository.findById(id).orElse(null);
                unites.add(unite != null ? (safe(unite.getCode()) + " - " + safe(unite.getNom())) : String.valueOf(id));
            }
            parts.add("Unite: " + String.join(", ", unites));
        }
        if (typeEmployeIds != null && !typeEmployeIds.isEmpty()) {
            List<String> types = new ArrayList<>();
            for (Long id : typeEmployeIds) {
                TypeEmploye type = typeEmployeRepository.findById(id).orElse(null);
                types.add(type != null ? safe(type.getDescription()) : String.valueOf(id));
            }
            parts.add("Type employe: " + String.join(", ", types));
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

    private LocalDate resolvePlannedDateFin(PresenceEmploye presence) {
        if (presence.getDateDepart() != null) {
            return presence.getDateDepart();
        }
        if ("Y".equalsIgnoreCase(safe(presence.getNuitPlanifiee()))) {
            return presence.getDateJour() != null ? presence.getDateJour().plusDays(1) : null;
        }
        return presence.getDateJour();
    }

    private String firstNotBlank(String... values) {
        if (values == null || values.length == 0) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private boolean isYes(String value) {
        return "OUI".equalsIgnoreCase(safe(value)) || "Y".equalsIgnoreCase(safe(value));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
