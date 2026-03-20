package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.PrestationDepartDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PrestationDepartDeductionDTO;
import com.rsoft.hurmanagement.hurmasterdata.dto.PrestationDepartDetailDTO;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrestationDepartService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal DAYS_DEFAULT = BigDecimal.valueOf(30);

    private final PrestationDepartRepository prestationDepartRepository;
    private final MutationEmployeRepository mutationEmployeRepository;
    private final EmployeSalaireRepository employeSalaireRepository;
    private final BalanceCongeRepository balanceCongeRepository;
    private final PretEmployeRepository pretEmployeRepository;
    private final PayrollEmployeRepository payrollEmployeRepository;

    @Transactional(readOnly = true)
    public Page<PrestationDepartDTO> findAll(Long employeId, String statut, Pageable pageable) {
        StatutPrestationDepart statutEnum = null;
        if (statut != null && !statut.isBlank()) {
            try {
                statutEnum = StatutPrestationDepart.valueOf(statut.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                statutEnum = null;
            }
        }
        return prestationDepartRepository.findAllWithFilters(employeId, statutEnum, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public PrestationDepartDTO findById(Long id) {
        PrestationDepart entity = prestationDepartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PrestationDepart not found with id: " + id));
        return toDTO(entity);
    }

    @Transactional
    public PrestationDepartDTO calculateFromMutation(Long mutationId, String username) {
        MutationEmploye mutation = mutationEmployeRepository.findById(mutationId)
                .orElseThrow(() -> new RuntimeException("MutationEmploye not found with id: " + mutationId));

        Employe employe = mutation.getEmploye();
        if (employe == null) {
            throw new RuntimeException("MutationEmploye has no employe");
        }

        EmployeSalaire salaireActif = employeSalaireRepository.findFirstByEmployeIdAndActifOrderByPrincipalDescIdDesc(employe.getId(), "Y")
                .orElseThrow(() -> new RuntimeException("No active employe_salaire found for employe id: " + employe.getId()));

        RegimePaie regimePaie = salaireActif.getRegimePaie();
        LocalDate dateDepart = mutation.getDateEffet();
        if (dateDepart == null) {
            throw new RuntimeException("MutationEmploye has no date_effet");
        }

        PrestationDepart prestation = new PrestationDepart();
        prestation.setEmploye(employe);
        prestation.setRegimePaie(regimePaie);
        prestation.setMutationEmploye(mutation);
        prestation.setTypeDepart(mutation.getTypeMutation() != null ? mutation.getTypeMutation().name() : "INCONNU");
        prestation.setDateDepart(dateDepart);
        prestation.setDateCalcul(LocalDateTime.now());
        prestation.setStatut(StatutPrestationDepart.CALCULE);
        prestation.setCreatedBy(username);
        prestation.setCreatedOn(OffsetDateTime.now());
        prestation.setRowscn(1);

        Optional<PayrollEmploye> latestPayrollOpt = payrollEmployeRepository
                .findFirstByEmployeIdAndPayrollRegimePaieIdAndPayrollStatutInAndPayrollDateFinLessThanEqualOrderByPayrollDateFinDesc(
                        employe.getId(),
                        regimePaie.getId(),
                        List.of(Payroll.StatutPayroll.CALCULE, Payroll.StatutPayroll.VALIDE, Payroll.StatutPayroll.FINALISE),
                        dateDepart
                );
        LocalDate lastPayrollDateFin = latestPayrollOpt.map(p -> p.getPayroll().getDateFin()).orElse(null);

        List<PrestationDepartDetail> details = new ArrayList<>();
        List<PrestationDepartDeduction> deductions = new ArrayList<>();

        PrestationDepartDetail salaireRestant = buildSalaireRestant(prestation, salaireActif, dateDepart, lastPayrollDateFin, username);
        details.add(salaireRestant);

        PrestationDepartDetail congesNonPris = buildCongesNonPris(prestation, salaireActif, employe.getId(), username);
        details.add(congesNonPris);

        PrestationDepartDetail remboursementPret = buildRemboursementPret(
                prestation,
                employe.getId(),
                latestPayrollOpt.orElse(null),
                username,
                deductions
        );
        details.add(remboursementPret);

        prestation.setDetails(details);
        prestation.setDeductions(deductions);

        recalcTotals(prestation);
        return toDTO(prestationDepartRepository.save(prestation));
    }

    @Transactional
    public PrestationDepartDTO validate(Long id, String username) {
        PrestationDepart prestation = prestationDepartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PrestationDepart not found with id: " + id));

        if (prestation.getStatut() != StatutPrestationDepart.CALCULE) {
            throw new RuntimeException("Only CALCULE prestations can be validated.");
        }

        prestation.setStatut(StatutPrestationDepart.VALIDE);
        prestation.setUpdatedBy(username);
        prestation.setUpdatedOn(OffsetDateTime.now());
        return toDTO(prestationDepartRepository.save(prestation));
    }

    private PrestationDepartDetail buildSalaireRestant(
            PrestationDepart prestation,
            EmployeSalaire salaireActif,
            LocalDate dateDepart,
            LocalDate lastPayrollDateFin,
            String username
    ) {
        BigDecimal monthlySalary = nullable(salaireActif.getMontant());
        BigDecimal dailySalary = monthlySalary.divide(resolveDaysInPeriod(dateDepart), 6, RoundingMode.HALF_UP);

        long daysDue;
        if (lastPayrollDateFin == null || lastPayrollDateFin.isBefore(dateDepart.withDayOfMonth(1))) {
            daysDue = dateDepart.getDayOfMonth();
        } else {
            LocalDate start = lastPayrollDateFin.plusDays(1);
            if (start.isAfter(dateDepart)) {
                daysDue = 0;
            } else {
                daysDue = ChronoUnit.DAYS.between(start, dateDepart.plusDays(1));
            }
        }

        BigDecimal amount = dailySalary.multiply(BigDecimal.valueOf(Math.max(0, daysDue))).setScale(2, RoundingMode.HALF_UP);
        return buildDetail(
                prestation,
                "SALAIRE_RESTANT",
                "Salaire restant",
                CategoriePrestationDepart.GAIN,
                monthlySalary.setScale(2, RoundingMode.HALF_UP),
                BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP),
                amount,
                1,
                username
        );
    }

    private PrestationDepartDetail buildCongesNonPris(
            PrestationDepart prestation,
            EmployeSalaire salaireActif,
            Long employeId,
            String username
    ) {
        BigDecimal joursNonPris = nullable(balanceCongeRepository.sumSoldeDisponibleByEmploye(employeId));
        BigDecimal dailySalary = nullable(salaireActif.getMontant()).divide(DAYS_DEFAULT, 6, RoundingMode.HALF_UP);
        BigDecimal amount = dailySalary.multiply(joursNonPris).setScale(2, RoundingMode.HALF_UP);

        return buildDetail(
                prestation,
                "CONGES_NON_PRIS",
                "Conges non pris",
                CategoriePrestationDepart.GAIN,
                joursNonPris.setScale(2, RoundingMode.HALF_UP),
                dailySalary.setScale(4, RoundingMode.HALF_UP),
                amount,
                2,
                username
        );
    }

    private PrestationDepartDetail buildRemboursementPret(
            PrestationDepart prestation,
            Long employeId,
            PayrollEmploye payrollEmploye,
            String username,
            List<PrestationDepartDeduction> deductions
    ) {
        List<PretEmploye> prets = pretEmployeRepository.findByEmployeIdAndStatutIn(
                employeId,
                List.of(PretEmploye.StatutPret.EN_COURS, PretEmploye.StatutPret.SUSPENDU)
        );

        BigDecimal total = ZERO;
        for (PretEmploye pret : prets) {
            BigDecimal solde = nullable(pret.getMontantPret()).subtract(nullable(pret.getMontantVerse()));
            if (solde.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal montant = solde.setScale(2, RoundingMode.HALF_UP);
            total = total.add(montant);

            if (payrollEmploye != null) {
                PrestationDepartDeduction deduction = new PrestationDepartDeduction();
                deduction.setPrestationDepart(prestation);
                deduction.setPayrollEmploye(payrollEmploye);
                deduction.setCodeDeduction("PRET_" + pret.getId());
                deduction.setLibelle(pret.getLibelle() != null && !pret.getLibelle().isBlank() ? pret.getLibelle() : "Remboursement pret");
                deduction.setCategorie("AUTRE");
                deduction.setBaseMontant(montant);
                deduction.setTaux(nullable(pret.getTauxInteret()).setScale(4, RoundingMode.HALF_UP));
                deduction.setMontant(montant);
                deduction.setReferenceExterne("PRET:" + pret.getId());
                deduction.setMontantCouvert(ZERO);
                deduction.setCreatedBy(username);
                deduction.setCreatedOn(OffsetDateTime.now());
                deduction.setRowscn(1);
                deductions.add(deduction);
            }
        }

        return buildDetail(
                prestation,
                "REMBOURSEMENT_PRET",
                "Remboursement pret",
                CategoriePrestationDepart.DEDUCTION,
                total,
                BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP),
                total,
                3,
                username
        );
    }

    private PrestationDepartDetail buildDetail(
            PrestationDepart prestation,
            String codeRubrique,
            String libelle,
            CategoriePrestationDepart categorie,
            BigDecimal base,
            BigDecimal taux,
            BigDecimal montant,
            Integer ordre,
            String username
    ) {
        PrestationDepartDetail detail = new PrestationDepartDetail();
        detail.setPrestationDepart(prestation);
        detail.setRubriquePrestation(codeRubrique);
        detail.setLibelle(libelle);
        detail.setCategorie(categorie);
        detail.setMontantBase(nullable(base).setScale(2, RoundingMode.HALF_UP));
        detail.setTaux(nullable(taux).setScale(4, RoundingMode.HALF_UP));
        detail.setMontant(nullable(montant).setScale(2, RoundingMode.HALF_UP));
        detail.setOrdreAffichage(ordre);
        detail.setCreatedBy(username);
        detail.setCreatedOn(OffsetDateTime.now());
        detail.setRowscn(1);
        return detail;
    }

    private void recalcTotals(PrestationDepart prestation) {
        BigDecimal gains = ZERO;
        BigDecimal deductions = ZERO;

        for (PrestationDepartDetail detail : prestation.getDetails()) {
            if (detail.getCategorie() == CategoriePrestationDepart.DEDUCTION) {
                deductions = deductions.add(nullable(detail.getMontant()));
            } else {
                gains = gains.add(nullable(detail.getMontant()));
            }
        }

        prestation.setTotalGains(gains.setScale(2, RoundingMode.HALF_UP));
        prestation.setTotalDeductions(deductions.setScale(2, RoundingMode.HALF_UP));
        prestation.setMontantNet(gains.subtract(deductions).setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal resolveDaysInPeriod(LocalDate date) {
        int days = date != null ? date.lengthOfMonth() : 30;
        return BigDecimal.valueOf(Math.max(1, days));
    }

    private BigDecimal nullable(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private PrestationDepartDTO toDTO(PrestationDepart entity) {
        PrestationDepartDTO dto = new PrestationDepartDTO();
        dto.setId(entity.getId());

        if (entity.getEmploye() != null) {
            dto.setEmployeId(entity.getEmploye().getId());
            dto.setEmployeCode(entity.getEmploye().getCodeEmploye());
            dto.setEmployeNom(entity.getEmploye().getNom());
            dto.setEmployePrenom(entity.getEmploye().getPrenom());
        }

        if (entity.getRegimePaie() != null) {
            dto.setRegimePaieId(entity.getRegimePaie().getId());
            dto.setRegimePaieCode(entity.getRegimePaie().getCodeRegimePaie());
            dto.setRegimePaieDescription(entity.getRegimePaie().getDescription());
        }

        if (entity.getMutationEmploye() != null) {
            dto.setMutationEmployeId(entity.getMutationEmploye().getId());
        }

        dto.setTypeDepart(entity.getTypeDepart());
        dto.setDateDepart(entity.getDateDepart());
        dto.setDateCalcul(entity.getDateCalcul());
        dto.setTotalGains(entity.getTotalGains());
        dto.setTotalDeductions(entity.getTotalDeductions());
        dto.setMontantNet(entity.getMontantNet());
        dto.setStatut(entity.getStatut() != null ? entity.getStatut().name() : null);
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());

        List<PrestationDepartDetailDTO> details = new ArrayList<>();
        for (PrestationDepartDetail detail : entity.getDetails()) {
            PrestationDepartDetailDTO d = new PrestationDepartDetailDTO();
            d.setId(detail.getId());
            d.setRubriquePrestation(detail.getRubriquePrestation());
            d.setLibelle(detail.getLibelle());
            d.setCategorie(detail.getCategorie() != null ? detail.getCategorie().name() : null);
            d.setMontantBase(detail.getMontantBase());
            d.setTaux(detail.getTaux());
            d.setMontant(detail.getMontant());
            d.setOrdreAffichage(detail.getOrdreAffichage());
            details.add(d);
        }
        dto.setDetails(details);

        List<PrestationDepartDeductionDTO> deductions = new ArrayList<>();
        for (PrestationDepartDeduction deduction : entity.getDeductions()) {
            PrestationDepartDeductionDTO d = new PrestationDepartDeductionDTO();
            d.setId(deduction.getId());
            d.setPayrollEmployeId(deduction.getPayrollEmploye() != null ? deduction.getPayrollEmploye().getId() : null);
            d.setCodeDeduction(deduction.getCodeDeduction());
            d.setLibelle(deduction.getLibelle());
            d.setCategorie(deduction.getCategorie());
            d.setBaseMontant(deduction.getBaseMontant());
            d.setTaux(deduction.getTaux());
            d.setMontant(deduction.getMontant());
            d.setReferenceExterne(deduction.getReferenceExterne());
            d.setMontantCouvert(deduction.getMontantCouvert());
            deductions.add(d);
        }
        dto.setDeductions(deductions);

        return dto;
    }
}
