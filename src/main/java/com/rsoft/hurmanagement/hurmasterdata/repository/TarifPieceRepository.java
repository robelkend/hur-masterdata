package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.TarifPiece;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface TarifPieceRepository extends JpaRepository<TarifPiece, Long> {
    Page<TarifPiece> findByTypePieceId(Long typePieceId, Pageable pageable);

    @Query("SELECT COUNT(tp) > 0 FROM TarifPiece tp WHERE " +
           "tp.typePiece.id = :typePieceId AND " +
           "tp.actif = 'Y' AND " +
           "(:excludeId IS NULL OR tp.id <> :excludeId) AND " +
           "tp.dateEffectif <= :newEnd AND " +
           "COALESCE(tp.dateFin, :newEnd) >= :newStart")
    boolean existsActiveOverlap(@Param("typePieceId") Long typePieceId,
                                @Param("newStart") LocalDate newStart,
                                @Param("newEnd") LocalDate newEnd,
                                @Param("excludeId") Long excludeId);

    @Query("SELECT tp FROM TarifPiece tp WHERE tp.typePiece.id = :typePieceId AND tp.actif = 'Y' " +
           "AND tp.dateEffectif <= :dateJour AND (tp.dateFin IS NULL OR tp.dateFin >= :dateJour) " +
           "ORDER BY tp.dateEffectif DESC")
    Page<TarifPiece> findActiveForDate(@Param("typePieceId") Long typePieceId,
                                       @Param("dateJour") LocalDate dateJour,
                                       Pageable pageable);
}
