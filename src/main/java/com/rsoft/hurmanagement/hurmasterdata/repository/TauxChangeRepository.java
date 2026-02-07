package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.TauxChange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TauxChangeRepository extends JpaRepository<TauxChange, Long> {
    
    // Find all by devise code
    Page<TauxChange> findByDeviseCodeDeviseOrderByDateTauxDesc(String codeDevise, Pageable pageable);
    
    // Find all by devise code (without pagination, for integration)
    List<TauxChange> findByDeviseCodeDeviseOrderByDateTauxDesc(String codeDevise);
    
    // Find all by devise code with date range filter (both dates)
    @Query("SELECT t FROM TauxChange t WHERE t.devise.codeDevise = :codeDevise " +
           "AND t.dateTaux >= :dateFrom AND t.dateTaux <= :dateTo " +
           "ORDER BY t.dateTaux DESC")
    List<TauxChange> findByDeviseCodeDeviseAndDateRange(
        @Param("codeDevise") String codeDevise,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo
    );
    
    // Find all by devise code with date from filter only
    @Query("SELECT t FROM TauxChange t WHERE t.devise.codeDevise = :codeDevise " +
           "AND t.dateTaux >= :dateFrom " +
           "ORDER BY t.dateTaux DESC")
    List<TauxChange> findByDeviseCodeDeviseAndDateFrom(
        @Param("codeDevise") String codeDevise,
        @Param("dateFrom") LocalDate dateFrom
    );
    
    // Find all by devise code with date to filter only
    @Query("SELECT t FROM TauxChange t WHERE t.devise.codeDevise = :codeDevise " +
           "AND t.dateTaux <= :dateTo " +
           "ORDER BY t.dateTaux DESC")
    List<TauxChange> findByDeviseCodeDeviseAndDateTo(
        @Param("codeDevise") String codeDevise,
        @Param("dateTo") LocalDate dateTo
    );
    
    // Find by devise, date and institution
    Optional<TauxChange> findByDeviseCodeDeviseAndDateTauxAndInstitutionCodeInstitution(
        String codeDevise, 
        LocalDate dateTaux, 
        String codeInstitution
    );
    
    // Find by devise, date and null institution
    Optional<TauxChange> findByDeviseCodeDeviseAndDateTauxAndInstitutionIsNull(
        String codeDevise, 
        LocalDate dateTaux
    );
    
    // Check if rate exists for devise/date/institution combination
    boolean existsByDeviseCodeDeviseAndDateTauxAndInstitutionCodeInstitution(
        String codeDevise,
        LocalDate dateTaux,
        String codeInstitution
    );
    
    boolean existsByDeviseCodeDeviseAndDateTauxAndInstitutionIsNull(
        String codeDevise,
        LocalDate dateTaux
    );
    
    // Find all with sorting
    @Query("SELECT t FROM TauxChange t WHERE (:codeDevise IS NULL OR t.devise.codeDevise = :codeDevise) ORDER BY t.dateTaux DESC, t.devise.codeDevise")
    Page<TauxChange> findAllByCodeDeviseOptional(@Param("codeDevise") String codeDevise, Pageable pageable);
}
