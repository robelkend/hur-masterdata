package com.rsoft.hurmanagement.hurmasterdata.repository;

import com.rsoft.hurmanagement.hurmasterdata.entity.Employe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long> {
    Optional<Employe> findByCodeEmploye(String codeEmploye);
    Optional<Employe> findByMatriculeInterne(String matriculeInterne);
    Page<Employe> findAll(Pageable pageable);
    boolean existsByCodeEmploye(String codeEmploye);
    
    @Query("SELECT e FROM Employe e WHERE LOWER(e.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.codeEmploye) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Employe> search(@Param("search") String search, Pageable pageable);
    
    @Query(value = "SELECT DISTINCT e.* FROM employe e " +
           "LEFT JOIN emploi_employe emp ON emp.employe_id = e.id " +
           "LEFT JOIN type_employe te ON te.id = emp.type_employe_id " +
           "LEFT JOIN unite_organisationnelle uo ON uo.id = emp.unite_organisationnelle_id " +
           "LEFT JOIN horaire h ON h.id = emp.horaire_id " +
           "LEFT JOIN fonction f ON f.id = emp.fonction_id " +
           "LEFT JOIN employe_salaire s ON s.employe_id = e.id " +
           "LEFT JOIN regime_paie rp ON rp.id = s.regime_paie_id " +
           "WHERE (:code IS NULL OR LOWER(e.code_employe) LIKE LOWER(CONCAT('%', :code, '%'))) " +
           "AND (:nom IS NULL OR " +
           "  (:nomPattern = 'STARTS_WITH' AND LOWER(e.nom) LIKE LOWER(CONCAT(:nom, '%'))) OR " +
           "  (:nomPattern = 'ENDS_WITH' AND LOWER(e.nom) LIKE LOWER(CONCAT('%', :nom))) OR " +
           "  (:nomPattern != 'STARTS_WITH' AND :nomPattern != 'ENDS_WITH' AND LOWER(e.nom) LIKE LOWER(CONCAT('%', :nom, '%')))) " +
           "AND (:prenom IS NULL OR " +
           "  (:prenomPattern = 'STARTS_WITH' AND LOWER(e.prenom) LIKE LOWER(CONCAT(:prenom, '%'))) OR " +
           "  (:prenomPattern = 'ENDS_WITH' AND LOWER(e.prenom) LIKE LOWER(CONCAT('%', :prenom))) OR " +
           "  (:prenomPattern != 'STARTS_WITH' AND :prenomPattern != 'ENDS_WITH' AND LOWER(e.prenom) LIKE LOWER(CONCAT('%', :prenom, '%')))) " +
           "AND (:typeEmployeId IS NULL OR te.id = :typeEmployeId) " +
           "AND (:uniteOrganisationnelleId IS NULL OR uo.id = :uniteOrganisationnelleId) " +
           "AND (:horaireId IS NULL OR h.id = :horaireId) " +
           "AND (:fonctionId IS NULL OR f.id = :fonctionId) " +
           "AND (:regimePaieId IS NULL OR rp.id = :regimePaieId) " +
           "AND (:entrepriseId IS NULL OR e.entreprise_id = :entrepriseId) " +
           "AND (:gestionnaireId IS NULL OR emp.gestionnaire_id = :gestionnaireId)",
           nativeQuery = true,
           countQuery = "SELECT COUNT(DISTINCT e.id) FROM employe e " +
           "LEFT JOIN emploi_employe emp ON emp.employe_id = e.id " +
           "LEFT JOIN type_employe te ON te.id = emp.type_employe_id " +
           "LEFT JOIN unite_organisationnelle uo ON uo.id = emp.unite_organisationnelle_id " +
           "LEFT JOIN horaire h ON h.id = emp.horaire_id " +
           "LEFT JOIN fonction f ON f.id = emp.fonction_id " +
           "LEFT JOIN employe_salaire s ON s.employe_id = e.id " +
           "LEFT JOIN regime_paie rp ON rp.id = s.regime_paie_id " +
           "WHERE (:code IS NULL OR LOWER(e.code_employe) LIKE LOWER(CONCAT('%', :code, '%'))) " +
           "AND (:nom IS NULL OR " +
           "  (:nomPattern = 'STARTS_WITH' AND LOWER(e.nom) LIKE LOWER(CONCAT(:nom, '%'))) OR " +
           "  (:nomPattern = 'ENDS_WITH' AND LOWER(e.nom) LIKE LOWER(CONCAT('%', :nom))) OR " +
           "  (:nomPattern != 'STARTS_WITH' AND :nomPattern != 'ENDS_WITH' AND LOWER(e.nom) LIKE LOWER(CONCAT('%', :nom, '%')))) " +
           "AND (:prenom IS NULL OR " +
           "  (:prenomPattern = 'STARTS_WITH' AND LOWER(e.prenom) LIKE LOWER(CONCAT(:prenom, '%'))) OR " +
           "  (:prenomPattern = 'ENDS_WITH' AND LOWER(e.prenom) LIKE LOWER(CONCAT('%', :prenom))) OR " +
           "  (:prenomPattern != 'STARTS_WITH' AND :prenomPattern != 'ENDS_WITH' AND LOWER(e.prenom) LIKE LOWER(CONCAT('%', :prenom, '%')))) " +
           "AND (:typeEmployeId IS NULL OR te.id = :typeEmployeId) " +
           "AND (:uniteOrganisationnelleId IS NULL OR uo.id = :uniteOrganisationnelleId) " +
           "AND (:horaireId IS NULL OR h.id = :horaireId) " +
           "AND (:fonctionId IS NULL OR f.id = :fonctionId) " +
           "AND (:regimePaieId IS NULL OR rp.id = :regimePaieId) " +
           "AND (:entrepriseId IS NULL OR e.entreprise_id = :entrepriseId) " +
           "AND (:gestionnaireId IS NULL OR emp.gestionnaire_id = :gestionnaireId)")
    Page<Employe> searchAdvanced(
            @Param("code") String code,
            @Param("nom") String nom,
            @Param("nomPattern") String nomPattern,
            @Param("prenom") String prenom,
            @Param("prenomPattern") String prenomPattern,
            @Param("typeEmployeId") Long typeEmployeId,
            @Param("uniteOrganisationnelleId") Long uniteOrganisationnelleId,
            @Param("horaireId") Long horaireId,
            @Param("fonctionId") Long fonctionId,
            @Param("regimePaieId") Long regimePaieId,
            @Param("entrepriseId") Long entrepriseId,
            @Param("gestionnaireId") Long gestionnaireId,
            Pageable pageable);
}
