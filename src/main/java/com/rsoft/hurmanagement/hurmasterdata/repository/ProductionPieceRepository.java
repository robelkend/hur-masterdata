package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.ProductionPiece;
import com.rsoft.hurmanagement.hurmasterdata.entity.ProductionPiece.StatutProduction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;

@Repository
public interface ProductionPieceRepository extends JpaRepository<ProductionPiece, Long> {
    boolean existsByEntrepriseIdAndEmployeIdAndDateJourAndTypePieceId(Long entrepriseId, Long employeId, LocalDate dateJour, Long typePieceId);

    @Query("SELECT COUNT(pp) > 0 FROM ProductionPiece pp WHERE pp.entreprise.id = :entrepriseId " +
           "AND pp.employe.id = :employeId AND pp.dateJour = :dateJour AND pp.typePiece.id = :typePieceId AND pp.id <> :id")
    boolean existsDuplicate(@Param("entrepriseId") Long entrepriseId,
                            @Param("employeId") Long employeId,
                            @Param("dateJour") LocalDate dateJour,
                            @Param("typePieceId") Long typePieceId,
                            @Param("id") Long id);

    @Query("SELECT pp FROM ProductionPiece pp WHERE " +
           "pp.dateJour >= COALESCE(:dateDebut, pp.dateJour) AND " +
           "pp.dateJour <= COALESCE(:dateFin, pp.dateJour) AND " +
           "(:employeId IS NULL OR pp.employe.id = :employeId) AND " +
           "(:entrepriseId IS NULL OR pp.entreprise.id = :entrepriseId)")
    Page<ProductionPiece> findByFilters(@Param("dateDebut") LocalDate dateDebut,
                                        @Param("dateFin") LocalDate dateFin,
                                        @Param("employeId") Long employeId,
                                        @Param("entrepriseId") Long entrepriseId,
                                        Pageable pageable);

    @Query("SELECT COALESCE(SUM(pp.montantTotal), 0) FROM ProductionPiece pp WHERE pp.employe.id = :employeId " +
           "AND pp.dateJour >= :dateDebut AND pp.dateJour <= :dateFin AND pp.statut IN :statuts")
    BigDecimal sumMontantTotalByEmployeAndPeriod(@Param("employeId") Long employeId,
                                                @Param("dateDebut") LocalDate dateDebut,
                                                @Param("dateFin") LocalDate dateFin,
                                                @Param("statuts") Collection<ProductionPiece.StatutProduction> statuts);

    @Query("SELECT COUNT(DISTINCT pp.dateJour) FROM ProductionPiece pp WHERE pp.employe.id = :employeId " +
           "AND pp.dateJour >= :dateDebut AND pp.dateJour <= :dateFin AND pp.statut IN :statuts")
    long countDistinctDaysByEmployeAndPeriod(@Param("employeId") Long employeId,
                                             @Param("dateDebut") LocalDate dateDebut,
                                             @Param("dateFin") LocalDate dateFin,
                                             @Param("statuts") Collection<ProductionPiece.StatutProduction> statuts);

    @Query("SELECT COALESCE(SUM(pp.montantTotal), 0) FROM ProductionPiece pp WHERE pp.employe.id = :employeId " +
           "AND pp.dateJour >= :dateDebut AND pp.dateJour < :dateFin AND pp.statut IN :statuts")
    BigDecimal sumMontantTotalByEmployeAndPeriodExclusive(@Param("employeId") Long employeId,
                                                          @Param("dateDebut") LocalDate dateDebut,
                                                          @Param("dateFin") LocalDate dateFin,
                                                          @Param("statuts") Collection<ProductionPiece.StatutProduction> statuts);

    @Query("SELECT COUNT(DISTINCT pp.dateJour) FROM ProductionPiece pp WHERE pp.employe.id = :employeId " +
           "AND pp.dateJour >= :dateDebut AND pp.dateJour < :dateFin AND pp.statut IN :statuts")
    long countDistinctDaysByEmployeAndPeriodExclusive(@Param("employeId") Long employeId,
                                                      @Param("dateDebut") LocalDate dateDebut,
                                                      @Param("dateFin") LocalDate dateFin,
                                                      @Param("statuts") Collection<ProductionPiece.StatutProduction> statuts);

    @Modifying
    @Query("UPDATE ProductionPiece pp SET pp.payrollId = :payrollId, pp.updatedBy = :username, " +
           "pp.updatedOn = :updatedOn, pp.rowscn = pp.rowscn + 1 " +
           "WHERE pp.employe.id = :employeId AND pp.dateJour >= :dateDebut AND pp.dateJour <= :dateFin " +
           "AND pp.statut IN :statuts")
    int updatePayrollIdByEmployeAndPeriod(@Param("payrollId") Long payrollId,
                                          @Param("username") String username,
                                          @Param("updatedOn") OffsetDateTime updatedOn,
                                          @Param("employeId") Long employeId,
                                          @Param("dateDebut") LocalDate dateDebut,
                                          @Param("dateFin") LocalDate dateFin,
                                          @Param("statuts") Collection<ProductionPiece.StatutProduction> statuts);

    @Modifying
    @Query("UPDATE ProductionPiece pp SET pp.payrollId = :payrollId, pp.updatedBy = :username, " +
           "pp.updatedOn = :updatedOn, pp.rowscn = pp.rowscn + 1 " +
           "WHERE pp.employe.id = :employeId AND pp.dateJour >= :dateDebut AND pp.dateJour < :dateFin " +
           "AND pp.statut IN :statuts")
    int updatePayrollIdByEmployeAndPeriodExclusive(@Param("payrollId") Long payrollId,
                                                   @Param("username") String username,
                                                   @Param("updatedOn") OffsetDateTime updatedOn,
                                                   @Param("employeId") Long employeId,
                                                   @Param("dateDebut") LocalDate dateDebut,
                                                   @Param("dateFin") LocalDate dateFin,
                                                   @Param("statuts") Collection<ProductionPiece.StatutProduction> statuts);

    @Modifying
    @Query("UPDATE ProductionPiece pp SET pp.payrollId = NULL, pp.updatedBy = :username, " +
           "pp.updatedOn = :updatedOn, pp.rowscn = pp.rowscn + 1 " +
           "WHERE pp.payrollId = :payrollId")
    int clearPayrollId(@Param("payrollId") Long payrollId,
                       @Param("username") String username,
                       @Param("updatedOn") OffsetDateTime updatedOn);

    @Modifying
    @Query("UPDATE ProductionPiece pp SET pp.statut = :statut, pp.updatedBy = :username, pp.updatedOn = :updatedOn, pp.rowscn = pp.rowscn + 1 " +
           "WHERE pp.statut = :sourceStatut AND " +
           "pp.dateJour >= COALESCE(:dateDebut, pp.dateJour) AND " +
           "pp.dateJour <= COALESCE(:dateFin, pp.dateJour) AND " +
           "(:employeId IS NULL OR pp.employe.id = :employeId) AND " +
           "(:entrepriseId IS NULL OR pp.entreprise.id = :entrepriseId)")
    int updateStatusByFilters(@Param("statut") StatutProduction statut,
                              @Param("sourceStatut") StatutProduction sourceStatut,
                              @Param("username") String username,
                              @Param("updatedOn") java.time.OffsetDateTime updatedOn,
                              @Param("dateDebut") LocalDate dateDebut,
                              @Param("dateFin") LocalDate dateFin,
                              @Param("employeId") Long employeId,
                              @Param("entrepriseId") Long entrepriseId);
}
