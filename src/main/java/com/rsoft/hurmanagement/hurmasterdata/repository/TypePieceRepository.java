package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.TypePiece;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TypePieceRepository extends JpaRepository<TypePiece, Long> {
    boolean existsByEntrepriseIdAndCodePiece(Long entrepriseId, String codePiece);

    @Query(
            value = "SELECT * FROM type_piece tp WHERE " +
                    "(:codePiece IS NULL OR CAST(tp.code_piece AS TEXT) ILIKE CONCAT('%', :codePiece, '%')) AND " +
                    "(:entrepriseId IS NULL OR tp.entreprise_id = :entrepriseId)",
            countQuery = "SELECT COUNT(*) FROM type_piece tp WHERE " +
                    "(:codePiece IS NULL OR CAST(tp.code_piece AS TEXT) ILIKE CONCAT('%', :codePiece, '%')) AND " +
                    "(:entrepriseId IS NULL OR tp.entreprise_id = :entrepriseId)",
            nativeQuery = true
    )
    Page<TypePiece> findAllWithFilters(@Param("codePiece") String codePiece,
                                       @Param("entrepriseId") Long entrepriseId,
                                       Pageable pageable);
}
