package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.entity.*;
import com.rsoft.hurmanagement.hurmasterdata.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeService {
    
    private final EmployeRepository employeRepository;
    private final EmployeCodeGenerationService codeGenerationService;
    private final EntrepriseRepository entrepriseRepository;
    
    // Repositories for related entities
    private final EmployeAdresseRepository employeAdresseRepository;
    private final EmployeIdentiteRepository employeIdentiteRepository;
    private final EmployeContactRepository employeContactRepository;
    private final EmployeNoteRepository employeNoteRepository;
    private final CoordonneeBancaireEmployeRepository coordonneeBancaireEmployeRepository;
    private final AssuranceEmployeRepository assuranceEmployeRepository;
    private final EmploiEmployeRepository emploiEmployeRepository;
    private final EmployeSalaireRepository employeSalaireRepository;
    private final EmployeDocumentRepository employeDocumentRepository;
    
    // Repositories for related entity references
    private final InstitutionTierseRepository institutionTierseRepository;
    private final PlanAssuranceRepository planAssuranceRepository;
    private final TypeEmployeRepository typeEmployeRepository;
    private final UniteOrganisationnelleRepository uniteOrganisationnelleRepository;
    private final PosteRepository posteRepository;
    private final HoraireRepository horaireRepository;
    private final FonctionRepository fonctionRepository;
    private final RegimePaieRepository regimePaieRepository;
    private final TypeCongeRepository typeCongeRepository;
    private final RubriquePaieRepository rubriquePaieRepository;
    
    @Transactional(readOnly = true)
    public Page<EmployeDTO> findAll(Pageable pageable) {
        return employeRepository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<EmployeDTO> search(String search, Pageable pageable) {
        return employeRepository.search(search, pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<EmployeDTO> searchAdvanced(
            String code, String nom, String nomPattern, String prenom, String prenomPattern,
            Long typeEmployeId, Long uniteOrganisationnelleId, Long horaireId,
            Long fonctionId, Long regimePaieId, Long entrepriseId, Long gestionnaireId,
            Pageable pageable) {
        // Convert empty strings to null for proper query handling
        String codeParam = (code != null && !code.trim().isEmpty()) ? code.trim() : null;
        String nomParam = (nom != null && !nom.trim().isEmpty()) ? nom.trim() : null;
        String prenomParam = (prenom != null && !prenom.trim().isEmpty()) ? prenom.trim() : null;
        String nomPatternParam = (nomPattern != null && !nomPattern.trim().isEmpty()) ? nomPattern.trim() : "CONTAINS";
        String prenomPatternParam = (prenomPattern != null && !prenomPattern.trim().isEmpty()) ? prenomPattern.trim() : "CONTAINS";
        
        return employeRepository.searchAdvanced(
                codeParam, nomParam, nomPatternParam, prenomParam, prenomPatternParam,
                typeEmployeId, uniteOrganisationnelleId, horaireId,
                fonctionId, regimePaieId, entrepriseId, gestionnaireId,
                pageable
        ).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public EmployeDTO findById(Long id) {
        Employe entity = employeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional
    public EmployeDTO create(EmployeCreateDTO dto, String username) {
        // Générer le code si nécessaire
        String codeEmploye = dto.getCodeEmploye();
        if (codeEmploye == null || codeEmploye.trim().isEmpty()) {
            codeEmploye = codeGenerationService.generateCode(
                dto.getEntrepriseId(), 
                null, // typeEmployeId sera défini plus tard dans emploi
                dto.getNom(), 
                dto.getPrenom(),
                dto.getDateNaissance()
            );
            if (codeEmploye == null) {
                throw new RuntimeException("Code employé is required and no automatic generation parameter found");
            }
        }
        
        // Vérifier l'unicité
        if (employeRepository.existsByCodeEmploye(codeEmploye)) {
            throw new RuntimeException("Employe with code " + codeEmploye + " already exists");
        }
        
        Employe entity = new Employe();
        if (dto.getEntrepriseId() != null) {
            Entreprise entreprise = entrepriseRepository.findById(dto.getEntrepriseId())
                    .orElseThrow(() -> new RuntimeException("Entreprise not found"));
            entity.setEntreprise(entreprise);
        }
        entity.setCodeEmploye(codeEmploye);
        entity.setMatriculeInterne(dto.getMatriculeInterne());
        entity.setNom(dto.getNom());
        entity.setPrenom(dto.getPrenom());
        entity.setNomme(dto.getNomme() != null ? dto.getNomme() : "N");
        entity.setDateNaissance(dto.getDateNaissance());
        entity.setPaysNaissance(dto.getPaysNaissance());
        entity.setPaysHabitation(dto.getPaysHabitation());
        entity.setSexe(dto.getSexe());
        entity.setEtatCivil(dto.getEtatCivil());
        entity.setNationalite(dto.getNationalite());
        entity.setLangue(dto.getLangue());
        entity.setCourriel(dto.getCourriel());
        entity.setTelephone1(dto.getTelephone1());
        entity.setTelephone2(dto.getTelephone2());
        entity.setPhoto(dto.getPhoto());
        entity.setActif("N"); // Auto-managed
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return toDTO(employeRepository.save(entity));
    }
    
    @Transactional
    public EmployeDTO update(Long id, EmployeUpdateDTO dto, String username) {
        Employe entity = employeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + id));
        
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setMatriculeInterne(dto.getMatriculeInterne());
        entity.setNom(dto.getNom());
        entity.setPrenom(dto.getPrenom());
        if (dto.getNomme() != null) {
            entity.setNomme(dto.getNomme());
        }
        entity.setDateNaissance(dto.getDateNaissance());
        entity.setPaysNaissance(dto.getPaysNaissance());
        entity.setPaysHabitation(dto.getPaysHabitation());
        entity.setSexe(dto.getSexe());
        entity.setEtatCivil(dto.getEtatCivil());
        entity.setNationalite(dto.getNationalite());
        entity.setLangue(dto.getLangue());
        entity.setCourriel(dto.getCourriel());
        entity.setTelephone1(dto.getTelephone1());
        entity.setTelephone2(dto.getTelephone2());
        entity.setPhoto(dto.getPhoto());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(employeRepository.save(entity));
    }
    
    @Transactional
    public void delete(Long id, Integer rowscn) {
        Employe entity = employeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + id));
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        employeRepository.delete(entity);
    }
    
    @Transactional
    public EmployeDTO uploadPhoto(Long id, String photoBase64, String username) {
        Employe entity = employeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + id));
        
        // Valider que la photo est en base64
        if (photoBase64 != null && !photoBase64.trim().isEmpty()) {
            // Si c'est déjà un data URL, on le garde tel quel, sinon on ajoute le préfixe
            if (!photoBase64.startsWith("data:")) {
                // Déterminer le type MIME depuis les premiers bytes
                String mimeType = "image/jpeg"; // par défaut
                if (photoBase64.startsWith("/9j/") || photoBase64.startsWith("iVBORw0KGgo")) {
                    mimeType = photoBase64.startsWith("/9j/") ? "image/jpeg" : "image/png";
                }
                photoBase64 = "data:" + mimeType + ";base64," + photoBase64;
            }
        }
        
        entity.setPhoto(photoBase64);
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return toDTO(employeRepository.save(entity));
    }
    
    private EmployeDTO toDTO(Employe entity) {
        EmployeDTO dto = new EmployeDTO();
        dto.setId(entity.getId());
        if (entity.getEntreprise() != null) {
            dto.setEntrepriseId(entity.getEntreprise().getId());
            dto.setEntrepriseCode(entity.getEntreprise().getCodeEntreprise());
            dto.setEntrepriseNom(entity.getEntreprise().getNomEntreprise());
        }
        dto.setCodeEmploye(entity.getCodeEmploye());
        dto.setMatriculeInterne(entity.getMatriculeInterne());
        dto.setNom(entity.getNom());
        dto.setPrenom(entity.getPrenom());
        dto.setNomme(entity.getNomme());
        dto.setNominationEligible(isNominationEligible(entity));
        dto.setDateNaissance(entity.getDateNaissance());
        dto.setPaysNaissance(entity.getPaysNaissance());
        dto.setPaysHabitation(entity.getPaysHabitation());
        dto.setSexe(entity.getSexe());
        dto.setEtatCivil(entity.getEtatCivil());
        dto.setNationalite(entity.getNationalite());
        dto.setLangue(entity.getLangue());
        dto.setCourriel(entity.getCourriel());
        dto.setTelephone1(entity.getTelephone1());
        dto.setTelephone2(entity.getTelephone2());
        dto.setPhoto(entity.getPhoto());
        dto.setDateEmbauche(entity.getDateEmbauche());
        dto.setDatePremiereEmbauche(entity.getDatePremiereEmbauche());
        dto.setActif(entity.getActif());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        
        // Lazy load related data only if needed
        if (entity.getAdresses() != null) {
            // Load adresses if needed
        }
        
        return dto;
    }

    private boolean isNominationEligible(Employe employe) {
        if (employe == null || employe.getId() == null) {
            return false;
        }
        if ("Y".equalsIgnoreCase(employe.getNomme()) || "Y".equalsIgnoreCase(employe.getActif())) {
            return false;
        }
        boolean hasNouveauEmploi = emploiEmployeRepository.existsByEmployeIdAndStatutEmploi(
                employe.getId(), EmploiEmploye.StatutEmploi.NOUVEAU);
        if (!hasNouveauEmploi) {
            return false;
        }
        return employeSalaireRepository.existsByEmployeIdAndActif(employe.getId(), "N");
    }
    
    // ========== Adresses Methods ==========
    @Transactional(readOnly = true)
    public List<EmployeAdresseDTO> getAdresses(Long employeId) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
        return employeAdresseRepository.findByEmployeId(employeId).stream()
                .map(this::adresseToDTO)
                .collect(Collectors.toList());
    }
    
    // Overload that accepts DTO directly (for frontend compatibility)
    @Transactional
    public EmployeAdresseDTO createAdresse(Long employeId, EmployeAdresseDTO dto, String username) {
        EmployeAdresseCreateDTO createDto = new EmployeAdresseCreateDTO();
        createDto.setTypeAdresse(dto.getTypeAdresse());
        createDto.setLigne1(dto.getLigne1());
        createDto.setLigne2(dto.getLigne2());
        createDto.setVille(dto.getVille());
        createDto.setEtat(dto.getEtat());
        createDto.setCodePostal(dto.getCodePostal());
        createDto.setPays(dto.getPays());
        createDto.setDateDebut(dto.getDateDebut());
        createDto.setDateFin(dto.getDateFin());
        createDto.setActif(dto.getActif() != null ? dto.getActif() : "Y");
        return createAdresse(employeId, createDto, username);
    }
    
    @Transactional
    public EmployeAdresseDTO createAdresse(Long employeId, EmployeAdresseCreateDTO dto, String username) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
        
        EmployeAdresse entity = new EmployeAdresse();
        entity.setEmploye(employe);
        entity.setTypeAdresse(EmployeAdresse.TypeAdresse.valueOf(dto.getTypeAdresse()));
        entity.setLigne1(dto.getLigne1());
        entity.setLigne2(dto.getLigne2());
        entity.setVille(dto.getVille());
        entity.setEtat(dto.getEtat());
        entity.setCodePostal(dto.getCodePostal());
        entity.setPays(dto.getPays());
        entity.setDateDebut(dto.getDateDebut());
        entity.setDateFin(dto.getDateFin());
        entity.setActif(dto.getActif() != null ? dto.getActif() : "Y");
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return adresseToDTO(employeAdresseRepository.save(entity));
    }
    
    // Overload that accepts DTO directly (for frontend compatibility)
    @Transactional
    public EmployeAdresseDTO updateAdresse(Long employeId, Long adresseId, EmployeAdresseDTO dto, String username) {
        if (dto.getId() == null || !dto.getId().equals(adresseId)) {
            dto.setId(adresseId);
        }
        if (dto.getRowscn() == null) {
            throw new RuntimeException("Rowscn is required for update");
        }
        
        EmployeAdresseUpdateDTO updateDto = new EmployeAdresseUpdateDTO();
        updateDto.setId(dto.getId());
        updateDto.setTypeAdresse(dto.getTypeAdresse());
        updateDto.setLigne1(dto.getLigne1());
        updateDto.setLigne2(dto.getLigne2());
        updateDto.setVille(dto.getVille());
        updateDto.setEtat(dto.getEtat());
        updateDto.setCodePostal(dto.getCodePostal());
        updateDto.setPays(dto.getPays());
        updateDto.setDateDebut(dto.getDateDebut());
        updateDto.setDateFin(dto.getDateFin());
        updateDto.setActif(dto.getActif());
        updateDto.setRowscn(dto.getRowscn());
        return updateAdresse(employeId, adresseId, updateDto, username);
    }
    
    @Transactional
    public EmployeAdresseDTO updateAdresse(Long employeId, Long adresseId, EmployeAdresseUpdateDTO dto, String username) {
        EmployeAdresse entity = employeAdresseRepository.findById(adresseId)
                .orElseThrow(() -> new RuntimeException("EmployeAdresse not found with id: " + adresseId));
        
        if (!entity.getEmploye().getId().equals(employeId)) {
            throw new RuntimeException("Adresse does not belong to this employe");
        }
        
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setTypeAdresse(EmployeAdresse.TypeAdresse.valueOf(dto.getTypeAdresse()));
        entity.setLigne1(dto.getLigne1());
        entity.setLigne2(dto.getLigne2());
        entity.setVille(dto.getVille());
        entity.setEtat(dto.getEtat());
        entity.setCodePostal(dto.getCodePostal());
        entity.setPays(dto.getPays());
        entity.setDateDebut(dto.getDateDebut());
        entity.setDateFin(dto.getDateFin());
        entity.setActif(dto.getActif());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return adresseToDTO(employeAdresseRepository.save(entity));
    }
    
    @Transactional
    public void deleteAdresse(Long employeId, Long adresseId, Integer rowscn) {
        EmployeAdresse entity = employeAdresseRepository.findById(adresseId)
                .orElseThrow(() -> new RuntimeException("EmployeAdresse not found with id: " + adresseId));
        
        if (!entity.getEmploye().getId().equals(employeId)) {
            throw new RuntimeException("Adresse does not belong to this employe");
        }
        
        if (!entity.getRowscn().equals(rowscn)) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        employeAdresseRepository.delete(entity);
    }
    
    private EmployeAdresseDTO adresseToDTO(EmployeAdresse entity) {
        EmployeAdresseDTO dto = new EmployeAdresseDTO();
        dto.setId(entity.getId());
        dto.setEmployeId(entity.getEmploye().getId());
        dto.setTypeAdresse(entity.getTypeAdresse() != null ? entity.getTypeAdresse().name() : null);
        dto.setLigne1(entity.getLigne1());
        dto.setLigne2(entity.getLigne2());
        dto.setVille(entity.getVille());
        dto.setEtat(entity.getEtat());
        dto.setCodePostal(entity.getCodePostal());
        dto.setPays(entity.getPays());
        dto.setDateDebut(entity.getDateDebut());
        dto.setDateFin(entity.getDateFin());
        dto.setActif(entity.getActif());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
    
    // ========== Identites Methods ==========
    @Transactional(readOnly = true)
    public List<EmployeIdentiteDTO> getIdentites(Long employeId) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
        return employeIdentiteRepository.findByEmployeId(employeId).stream()
                .map(this::identiteToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public EmployeIdentiteDTO createIdentite(Long employeId, EmployeIdentiteDTO dto, String username) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
        
        EmployeIdentite entity = new EmployeIdentite();
        entity.setEmploye(employe);
        entity.setTypePiece(EmployeIdentite.TypePiece.valueOf(dto.getTypePiece()));
        entity.setNumeroPiece(dto.getNumeroPiece());
        entity.setDateEmission(dto.getDateEmission());
        entity.setDateExpiration(dto.getDateExpiration());
        entity.setPaysEmission(dto.getPaysEmission());
        entity.setActif(dto.getActif() != null ? dto.getActif() : "Y");
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return identiteToDTO(employeIdentiteRepository.save(entity));
    }
    
    @Transactional
    public EmployeIdentiteDTO updateIdentite(Long employeId, Long identiteId, EmployeIdentiteDTO dto, String username) {
        EmployeIdentite entity = employeIdentiteRepository.findById(identiteId)
                .orElseThrow(() -> new RuntimeException("EmployeIdentite not found with id: " + identiteId));
        
        if (!entity.getEmploye().getId().equals(employeId)) {
            throw new RuntimeException("Identite does not belong to this employe");
        }
        
        if (dto.getRowscn() != null && !entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setTypePiece(EmployeIdentite.TypePiece.valueOf(dto.getTypePiece()));
        entity.setNumeroPiece(dto.getNumeroPiece());
        entity.setDateEmission(dto.getDateEmission());
        entity.setDateExpiration(dto.getDateExpiration());
        entity.setPaysEmission(dto.getPaysEmission());
        entity.setActif(dto.getActif() != null ? dto.getActif() : "Y");
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return identiteToDTO(employeIdentiteRepository.save(entity));
    }
    
    @Transactional
    public void deleteIdentite(Long employeId, Long identiteId) {
        EmployeIdentite entity = employeIdentiteRepository.findById(identiteId)
                .orElseThrow(() -> new RuntimeException("EmployeIdentite not found with id: " + identiteId));
        
        if (!entity.getEmploye().getId().equals(employeId)) {
            throw new RuntimeException("Identite does not belong to this employe");
        }
        
        employeIdentiteRepository.delete(entity);
    }
    
    private EmployeIdentiteDTO identiteToDTO(EmployeIdentite entity) {
        EmployeIdentiteDTO dto = new EmployeIdentiteDTO();
        dto.setId(entity.getId());
        dto.setEmployeId(entity.getEmploye().getId());
        dto.setTypePiece(entity.getTypePiece() != null ? entity.getTypePiece().name() : null);
        dto.setNumeroPiece(entity.getNumeroPiece());
        dto.setDateEmission(entity.getDateEmission());
        dto.setDateExpiration(entity.getDateExpiration());
        dto.setPaysEmission(entity.getPaysEmission());
        dto.setActif(entity.getActif());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
    
    // ========== Contacts Methods ==========
    @Transactional(readOnly = true)
    public List<EmployeContactDTO> getContacts(Long employeId) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
        return employeContactRepository.findByEmployeId(employeId).stream()
                .map(this::contactToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public EmployeContactDTO createContact(Long employeId, EmployeContactDTO dto, String username) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
        
        EmployeContact entity = new EmployeContact();
        entity.setEmploye(employe);
        entity.setNom(dto.getNom());
        entity.setPrenom(dto.getPrenom());
        entity.setLien(dto.getLien());
        entity.setTelephone1(dto.getTelephone1());
        entity.setTelephone2(dto.getTelephone2());
        entity.setCourriel(dto.getCourriel());
        entity.setPriorite(dto.getPriorite() != null ? dto.getPriorite() : 1);
        entity.setActif(dto.getActif() != null ? dto.getActif() : "Y");
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return contactToDTO(employeContactRepository.save(entity));
    }
    
    @Transactional
    public EmployeContactDTO updateContact(Long employeId, Long contactId, EmployeContactDTO dto, String username) {
        EmployeContact entity = employeContactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("EmployeContact not found with id: " + contactId));
        
        if (!entity.getEmploye().getId().equals(employeId)) {
            throw new RuntimeException("Contact does not belong to this employe");
        }
        
        if (dto.getRowscn() != null && !entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setNom(dto.getNom());
        entity.setPrenom(dto.getPrenom());
        entity.setLien(dto.getLien());
        entity.setTelephone1(dto.getTelephone1());
        entity.setTelephone2(dto.getTelephone2());
        entity.setCourriel(dto.getCourriel());
        entity.setPriorite(dto.getPriorite() != null ? dto.getPriorite() : 1);
        entity.setActif(dto.getActif() != null ? dto.getActif() : "Y");
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return contactToDTO(employeContactRepository.save(entity));
    }
    
    @Transactional
    public void deleteContact(Long employeId, Long contactId) {
        EmployeContact entity = employeContactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("EmployeContact not found with id: " + contactId));
        
        if (!entity.getEmploye().getId().equals(employeId)) {
            throw new RuntimeException("Contact does not belong to this employe");
        }
        
        employeContactRepository.delete(entity);
    }
    
    private EmployeContactDTO contactToDTO(EmployeContact entity) {
        EmployeContactDTO dto = new EmployeContactDTO();
        dto.setId(entity.getId());
        dto.setEmployeId(entity.getEmploye().getId());
        dto.setNom(entity.getNom());
        dto.setPrenom(entity.getPrenom());
        dto.setLien(entity.getLien());
        dto.setTelephone1(entity.getTelephone1());
        dto.setTelephone2(entity.getTelephone2());
        dto.setCourriel(entity.getCourriel());
        dto.setPriorite(entity.getPriorite());
        dto.setActif(entity.getActif());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
    
    // ========== Notes Methods ==========
    @Transactional(readOnly = true)
    public List<EmployeNoteDTO> getNotes(Long employeId) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
        return employeNoteRepository.findByEmployeId(employeId).stream()
                .map(this::noteToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public EmployeNoteDTO createNote(Long employeId, EmployeNoteDTO dto, String username) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
        
        EmployeNote entity = new EmployeNote();
        entity.setEmploye(employe);
        entity.setTypeNote(EmployeNote.TypeNote.valueOf(dto.getTypeNote()));
        entity.setTitre(dto.getTitre());
        entity.setNote(dto.getNote());
        entity.setConfidentiel(dto.getConfidentiel() != null ? dto.getConfidentiel() : "N");
        entity.setEnvoye(dto.getEnvoye() != null ? dto.getEnvoye() : "N");
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return noteToDTO(employeNoteRepository.save(entity));
    }
    
    @Transactional
    public EmployeNoteDTO updateNote(Long employeId, Long noteId, EmployeNoteDTO dto, String username) {
        EmployeNote entity = employeNoteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("EmployeNote not found with id: " + noteId));
        
        if (!entity.getEmploye().getId().equals(employeId)) {
            throw new RuntimeException("Note does not belong to this employe");
        }
        
        if (dto.getRowscn() != null && !entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setTypeNote(EmployeNote.TypeNote.valueOf(dto.getTypeNote()));
        entity.setTitre(dto.getTitre());
        entity.setNote(dto.getNote());
        entity.setConfidentiel(dto.getConfidentiel() != null ? dto.getConfidentiel() : "N");
        entity.setEnvoye(dto.getEnvoye() != null ? dto.getEnvoye() : "N");
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return noteToDTO(employeNoteRepository.save(entity));
    }
    
    @Transactional
    public void deleteNote(Long employeId, Long noteId) {
        EmployeNote entity = employeNoteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("EmployeNote not found with id: " + noteId));
        
        if (!entity.getEmploye().getId().equals(employeId)) {
            throw new RuntimeException("Note does not belong to this employe");
        }
        
        employeNoteRepository.delete(entity);
    }
    
    private EmployeNoteDTO noteToDTO(EmployeNote entity) {
        EmployeNoteDTO dto = new EmployeNoteDTO();
        dto.setId(entity.getId());
        dto.setEmployeId(entity.getEmploye().getId());
        dto.setTypeNote(entity.getTypeNote() != null ? entity.getTypeNote().name() : null);
        dto.setTitre(entity.getTitre());
        dto.setNote(entity.getNote());
        dto.setConfidentiel(entity.getConfidentiel());
        dto.setEnvoye(entity.getEnvoye());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
    
    // ========== Documents Methods ==========
    @Transactional(readOnly = true)
    public List<EmployeDocumentDTO> getDocuments(Long employeId) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
        return employeDocumentRepository.findByEmployeId(employeId).stream()
                .map(this::documentToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public EmployeDocumentDTO uploadDocument(Long employeId, String nomFichier, String mimeType, 
                                             Long tailleOctets, String storageRef, String hashSha256,
                                             String typeDocument, String commentaire, String username) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
        
        EmployeDocument entity = new EmployeDocument();
        entity.setEmploye(employe);
        entity.setTypeDocument(EmployeDocument.TypeDocument.valueOf(typeDocument));
        entity.setNomFichier(nomFichier);
        entity.setMimeType(mimeType);
        entity.setTailleOctets(tailleOctets);
        entity.setStorageRef(storageRef);
        entity.setHashSha256(hashSha256);
        entity.setDateDocument(java.time.LocalDate.now());
        entity.setCommentaire(commentaire);
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return documentToDTO(employeDocumentRepository.save(entity));
    }
    
    @Transactional
    public void deleteDocument(Long employeId, Long documentId) {
        EmployeDocument entity = employeDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("EmployeDocument not found with id: " + documentId));
        
        if (!entity.getEmploye().getId().equals(employeId)) {
            throw new RuntimeException("Document does not belong to this employe");
        }
        
        employeDocumentRepository.delete(entity);
    }
    
    private EmployeDocumentDTO documentToDTO(EmployeDocument entity) {
        EmployeDocumentDTO dto = new EmployeDocumentDTO();
        dto.setId(entity.getId());
        dto.setEmployeId(entity.getEmploye().getId());
        dto.setTypeDocument(entity.getTypeDocument() != null ? entity.getTypeDocument().name() : null);
        dto.setNomFichier(entity.getNomFichier());
        dto.setMimeType(entity.getMimeType());
        dto.setTailleOctets(entity.getTailleOctets());
        dto.setStorageRef(entity.getStorageRef());
        dto.setHashSha256(entity.getHashSha256());
        dto.setDateDocument(entity.getDateDocument());
        dto.setCommentaire(entity.getCommentaire());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
    
    // ========== Coordonnees Bancaires Methods ==========
    @Transactional(readOnly = true)
    public List<CoordonneeBancaireEmployeDTO> getCoordonneesBancaires(Long employeId) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
        return coordonneeBancaireEmployeRepository.findByEmployeId(employeId).stream()
                .map(this::coordonneeBancaireToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CoordonneeBancaireEmployeDTO createCoordonneeBancaire(Long employeId, CoordonneeBancaireEmployeDTO dto, String username) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
        
        InstitutionTierse banque = institutionTierseRepository.findById(dto.getBanqueId())
                .orElseThrow(() -> new RuntimeException("InstitutionTierse (banque) not found with id: " + dto.getBanqueId()));
        
        CoordonneeBancaireEmploye entity = new CoordonneeBancaireEmploye();
        entity.setEmploye(employe);
        entity.setBanque(banque);
        entity.setNumeroCompte(dto.getNumeroCompte());
        entity.setCategorie(CoordonneeBancaireEmploye.CategorieCompte.valueOf(dto.getCategorie()));
        entity.setActif(dto.getActif() != null ? dto.getActif() : "N");
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return coordonneeBancaireToDTO(coordonneeBancaireEmployeRepository.save(entity));
    }
    
    @Transactional
    public CoordonneeBancaireEmployeDTO updateCoordonneeBancaire(Long employeId, Long coordId, CoordonneeBancaireEmployeDTO dto, String username) {
        CoordonneeBancaireEmploye entity = coordonneeBancaireEmployeRepository.findById(coordId)
                .orElseThrow(() -> new RuntimeException("CoordonneeBancaireEmploye not found with id: " + coordId));
        
        if (!entity.getEmploye().getId().equals(employeId)) {
            throw new RuntimeException("CoordonneeBancaire does not belong to this employe");
        }
        
        if (dto.getRowscn() != null && !entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        if (dto.getBanqueId() != null) {
            InstitutionTierse banque = institutionTierseRepository.findById(dto.getBanqueId())
                    .orElseThrow(() -> new RuntimeException("InstitutionTierse (banque) not found with id: " + dto.getBanqueId()));
            entity.setBanque(banque);
        }
        
        entity.setNumeroCompte(dto.getNumeroCompte());
        entity.setCategorie(CoordonneeBancaireEmploye.CategorieCompte.valueOf(dto.getCategorie()));
        entity.setActif(dto.getActif() != null ? dto.getActif() : "N");
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return coordonneeBancaireToDTO(coordonneeBancaireEmployeRepository.save(entity));
    }
    
    @Transactional
    public void deleteCoordonneeBancaire(Long employeId, Long coordId) {
        CoordonneeBancaireEmploye entity = coordonneeBancaireEmployeRepository.findById(coordId)
                .orElseThrow(() -> new RuntimeException("CoordonneeBancaireEmploye not found with id: " + coordId));
        
        if (!entity.getEmploye().getId().equals(employeId)) {
            throw new RuntimeException("CoordonneeBancaire does not belong to this employe");
        }
        
        coordonneeBancaireEmployeRepository.delete(entity);
    }
    
    private CoordonneeBancaireEmployeDTO coordonneeBancaireToDTO(CoordonneeBancaireEmploye entity) {
        CoordonneeBancaireEmployeDTO dto = new CoordonneeBancaireEmployeDTO();
        dto.setId(entity.getId());
        dto.setEmployeId(entity.getEmploye().getId());
        if (entity.getBanque() != null) {
            dto.setBanqueId(entity.getBanque().getId());
            dto.setBanqueCode(entity.getBanque().getCodeInstitution());
            dto.setBanqueNom(entity.getBanque().getNom());
        }
        dto.setNumeroCompte(entity.getNumeroCompte());
        dto.setCategorie(entity.getCategorie() != null ? entity.getCategorie().name() : null);
        dto.setActif(entity.getActif());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
    
    // ========== Assurances Methods ==========
    @Transactional(readOnly = true)
    public List<AssuranceEmployeDTO> getAssurances(Long employeId) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
        return assuranceEmployeRepository.findByEmployeId(employeId).stream()
                .map(this::assuranceToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public AssuranceEmployeDTO createAssurance(Long employeId, AssuranceEmployeDTO dto, String username) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
        
        PlanAssurance planAssurance = planAssuranceRepository.findById(dto.getPlanAssuranceId())
                .orElseThrow(() -> new RuntimeException("PlanAssurance not found with id: " + dto.getPlanAssuranceId()));
        
        AssuranceEmploye entity = new AssuranceEmploye();
        entity.setEmploye(employe);
        entity.setPlanAssurance(planAssurance);
        entity.setNoAssurance(dto.getNoAssurance());
        entity.setActif(dto.getActif() != null ? dto.getActif() : "N");
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return assuranceToDTO(assuranceEmployeRepository.save(entity));
    }
    
    @Transactional
    public AssuranceEmployeDTO updateAssurance(Long employeId, Long assuranceId, AssuranceEmployeDTO dto, String username) {
        AssuranceEmploye entity = assuranceEmployeRepository.findById(assuranceId)
                .orElseThrow(() -> new RuntimeException("AssuranceEmploye not found with id: " + assuranceId));
        
        if (!entity.getEmploye().getId().equals(employeId)) {
            throw new RuntimeException("Assurance does not belong to this employe");
        }
        
        if (dto.getRowscn() != null && !entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        if (dto.getPlanAssuranceId() != null) {
            PlanAssurance planAssurance = planAssuranceRepository.findById(dto.getPlanAssuranceId())
                    .orElseThrow(() -> new RuntimeException("PlanAssurance not found with id: " + dto.getPlanAssuranceId()));
            entity.setPlanAssurance(planAssurance);
        }
        
        entity.setNoAssurance(dto.getNoAssurance());
        entity.setActif(dto.getActif() != null ? dto.getActif() : "N");
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return assuranceToDTO(assuranceEmployeRepository.save(entity));
    }
    
    @Transactional
    public void deleteAssurance(Long employeId, Long assuranceId) {
        AssuranceEmploye entity = assuranceEmployeRepository.findById(assuranceId)
                .orElseThrow(() -> new RuntimeException("AssuranceEmploye not found with id: " + assuranceId));
        
        if (!entity.getEmploye().getId().equals(employeId)) {
            throw new RuntimeException("Assurance does not belong to this employe");
        }
        
        assuranceEmployeRepository.delete(entity);
    }
    
    private AssuranceEmployeDTO assuranceToDTO(AssuranceEmploye entity) {
        AssuranceEmployeDTO dto = new AssuranceEmployeDTO();
        dto.setId(entity.getId());
        dto.setEmployeId(entity.getEmploye().getId());
        if (entity.getPlanAssurance() != null) {
            dto.setPlanAssuranceId(entity.getPlanAssurance().getId());
            dto.setPlanAssuranceCode(entity.getPlanAssurance().getCodePlan());
            dto.setPlanAssuranceDescription(entity.getPlanAssurance().getDescription());
        }
        dto.setNoAssurance(entity.getNoAssurance());
        dto.setActif(entity.getActif());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
    
    // ========== Emplois Methods ==========
    @Transactional(readOnly = true)
    public List<EmploiEmployeDTO> getEmplois(Long employeId) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
        return emploiEmployeRepository.findByEmployeId(employeId).stream()
                .map(this::emploiToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public EmploiEmployeDTO createEmploi(Long employeId, EmploiEmployeDTO dto, String username) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
        
        EmploiEmploye entity = new EmploiEmploye();
        entity.setEmploye(employe);
        entity.setDateDebut(dto.getDateDebut());
        entity.setDateFin(dto.getDateFin());
        entity.setMotifFin(dto.getMotifFin());
        entity.setStatutEmploi(EmploiEmploye.StatutEmploi.NOUVEAU);
        entity.setDateFinStatut(dto.getDateFinStatut());
        if (dto.getTypeContrat() != null) {
            entity.setTypeContrat(EmploiEmploye.TypeContrat.valueOf(dto.getTypeContrat()));
        }
        if (dto.getTempsTravail() != null) {
            entity.setTempsTravail(EmploiEmploye.TempsTravail.valueOf(dto.getTempsTravail()));
        }
        
        // Relations
        if (dto.getTypeEmployeId() != null) {
            TypeEmploye typeEmploye = typeEmployeRepository.findById(dto.getTypeEmployeId())
                    .orElseThrow(() -> new RuntimeException("TypeEmploye not found with id: " + dto.getTypeEmployeId()));
            entity.setTypeEmploye(typeEmploye);
        }
        
        if (dto.getUniteOrganisationnelleId() != null) {
            UniteOrganisationnelle unite = uniteOrganisationnelleRepository.findById(dto.getUniteOrganisationnelleId())
                    .orElseThrow(() -> new RuntimeException("UniteOrganisationnelle not found with id: " + dto.getUniteOrganisationnelleId()));
            entity.setUniteOrganisationnelle(unite);
        }
        
        if (dto.getPosteId() != null) {
            Poste poste = posteRepository.findById(dto.getPosteId())
                    .orElseThrow(() -> new RuntimeException("Poste not found with id: " + dto.getPosteId()));
            entity.setPoste(poste);
        }
        
        if (dto.getHoraireId() != null) {
            Horaire horaire = horaireRepository.findById(dto.getHoraireId())
                    .orElseThrow(() -> new RuntimeException("Horaire not found with id: " + dto.getHoraireId()));
            entity.setHoraire(horaire);
        }
        
        if (dto.getFonctionId() != null) {
            Fonction fonction = fonctionRepository.findById(dto.getFonctionId())
                    .orElseThrow(() -> new RuntimeException("Fonction not found with id: " + dto.getFonctionId()));
            entity.setFonction(fonction);
        }
        
        if (dto.getGestionnaireId() != null) {
            Employe gestionnaire = employeRepository.findById(dto.getGestionnaireId())
                    .orElseThrow(() -> new RuntimeException("Gestionnaire Employe not found with id: " + dto.getGestionnaireId()));
            entity.setGestionnaire(gestionnaire);
        }
        
        if (dto.getTypeCongeId() != null) {
            TypeConge typeConge = typeCongeRepository.findById(dto.getTypeCongeId())
                    .orElseThrow(() -> new RuntimeException("TypeConge not found with id: " + dto.getTypeCongeId()));
            entity.setTypeConge(typeConge);
        } else {
            entity.setTypeConge(null);
        }
        
        entity.setTauxSupplementaire(dto.getTauxSupplementaire() != null ? dto.getTauxSupplementaire() : java.math.BigDecimal.ZERO);
        entity.setJourOff1(dto.getJourOff1());
        entity.setJourOff2(dto.getJourOff2());
        entity.setJourOff3(dto.getJourOff3());
        entity.setEnConge(dto.getEnConge() != null ? dto.getEnConge() : "N");
        entity.setEnProbation(dto.getEnProbation() != null ? dto.getEnProbation() : "N");
        entity.setPrincipal(dto.getPrincipal() != null ? dto.getPrincipal() : "N");
        // StatutEmploi is always NOUVEAU for create
        entity.setStatutEmploi(EmploiEmploye.StatutEmploi.NOUVEAU);
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return emploiToDTO(emploiEmployeRepository.save(entity));
    }
    
    @Transactional
    public EmploiEmployeDTO updateEmploi(Long employeId, Long emploiId, EmploiEmployeDTO dto, String username) {
        EmploiEmploye entity = emploiEmployeRepository.findById(emploiId)
                .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiId));
        
        if (!entity.getEmploye().getId().equals(employeId)) {
            throw new RuntimeException("Emploi does not belong to this employe");
        }

        ensureNotNomme(entity.getEmploye());
        
        if (dto.getRowscn() != null && !entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        entity.setDateDebut(dto.getDateDebut());
        entity.setDateFin(dto.getDateFin());
        entity.setMotifFin(dto.getMotifFin());
        if (dto.getStatutEmploi() != null) {
            entity.setStatutEmploi(EmploiEmploye.StatutEmploi.valueOf(dto.getStatutEmploi()));
        }
        entity.setDateFinStatut(dto.getDateFinStatut());
        if (dto.getTypeContrat() != null) {
            entity.setTypeContrat(EmploiEmploye.TypeContrat.valueOf(dto.getTypeContrat()));
        }
        if (dto.getTempsTravail() != null) {
            entity.setTempsTravail(EmploiEmploye.TempsTravail.valueOf(dto.getTempsTravail()));
        }
        
        // Update relations
        if (dto.getTypeEmployeId() != null) {
            TypeEmploye typeEmploye = typeEmployeRepository.findById(dto.getTypeEmployeId())
                    .orElseThrow(() -> new RuntimeException("TypeEmploye not found with id: " + dto.getTypeEmployeId()));
            entity.setTypeEmploye(typeEmploye);
        }
        
        if (dto.getUniteOrganisationnelleId() != null) {
            UniteOrganisationnelle unite = uniteOrganisationnelleRepository.findById(dto.getUniteOrganisationnelleId())
                    .orElseThrow(() -> new RuntimeException("UniteOrganisationnelle not found with id: " + dto.getUniteOrganisationnelleId()));
            entity.setUniteOrganisationnelle(unite);
        }
        
        if (dto.getPosteId() != null) {
            Poste poste = posteRepository.findById(dto.getPosteId())
                    .orElseThrow(() -> new RuntimeException("Poste not found with id: " + dto.getPosteId()));
            entity.setPoste(poste);
        } else {
            entity.setPoste(null);
        }
        
        if (dto.getHoraireId() != null) {
            Horaire horaire = horaireRepository.findById(dto.getHoraireId())
                    .orElseThrow(() -> new RuntimeException("Horaire not found with id: " + dto.getHoraireId()));
            entity.setHoraire(horaire);
        } else {
            entity.setHoraire(null);
        }
        
        if (dto.getFonctionId() != null) {
            Fonction fonction = fonctionRepository.findById(dto.getFonctionId())
                    .orElseThrow(() -> new RuntimeException("Fonction not found with id: " + dto.getFonctionId()));
            entity.setFonction(fonction);
        } else {
            entity.setFonction(null);
        }
        
        if (dto.getGestionnaireId() != null) {
            Employe gestionnaire = employeRepository.findById(dto.getGestionnaireId())
                    .orElseThrow(() -> new RuntimeException("Gestionnaire Employe not found with id: " + dto.getGestionnaireId()));
            entity.setGestionnaire(gestionnaire);
        } else {
            entity.setGestionnaire(null);
        }
        
        if (dto.getTypeCongeId() != null) {
            TypeConge typeConge = typeCongeRepository.findById(dto.getTypeCongeId())
                    .orElseThrow(() -> new RuntimeException("TypeConge not found with id: " + dto.getTypeCongeId()));
            entity.setTypeConge(typeConge);
        } else {
            entity.setTypeConge(null);
        }
        
        if (dto.getTauxSupplementaire() != null) {
            entity.setTauxSupplementaire(dto.getTauxSupplementaire());
        } else {
            entity.setTauxSupplementaire(java.math.BigDecimal.ZERO);
        }
        entity.setJourOff1(dto.getJourOff1());
        entity.setJourOff2(dto.getJourOff2());
        entity.setJourOff3(dto.getJourOff3());
        entity.setEnConge(dto.getEnConge() != null ? dto.getEnConge() : "N");
        entity.setEnProbation(dto.getEnProbation() != null ? dto.getEnProbation() : "N");
        entity.setPrincipal(dto.getPrincipal() != null ? dto.getPrincipal() : "N");
        // StatutEmploi is always NOUVEAU for update
        entity.setStatutEmploi(EmploiEmploye.StatutEmploi.NOUVEAU);
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return emploiToDTO(emploiEmployeRepository.save(entity));
    }
    
    @Transactional
    public void deleteEmploi(Long employeId, Long emploiId) {
        EmploiEmploye entity = emploiEmployeRepository.findById(emploiId)
                .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + emploiId));
        
        if (!entity.getEmploye().getId().equals(employeId)) {
            throw new RuntimeException("Emploi does not belong to this employe");
        }

        ensureNotNomme(entity.getEmploye());
        
        emploiEmployeRepository.delete(entity);
    }
    
    private EmploiEmployeDTO emploiToDTO(EmploiEmploye entity) {
        EmploiEmployeDTO dto = new EmploiEmployeDTO();
        dto.setId(entity.getId());
        dto.setEmployeId(entity.getEmploye().getId());
        dto.setDateDebut(entity.getDateDebut());
        dto.setDateFin(entity.getDateFin());
        dto.setMotifFin(entity.getMotifFin());
        dto.setStatutEmploi(entity.getStatutEmploi() != null ? entity.getStatutEmploi().name() : null);
        dto.setDateFinStatut(entity.getDateFinStatut());
        dto.setTypeContrat(entity.getTypeContrat() != null ? entity.getTypeContrat().name() : null);
        dto.setTempsTravail(entity.getTempsTravail() != null ? entity.getTempsTravail().name() : null);
        
        if (entity.getTypeEmploye() != null) {
            dto.setTypeEmployeId(entity.getTypeEmploye().getId());
            dto.setTypeEmployeDescription(entity.getTypeEmploye().getDescription());
        }
        if (entity.getUniteOrganisationnelle() != null) {
            dto.setUniteOrganisationnelleId(entity.getUniteOrganisationnelle().getId());
            dto.setUniteOrganisationnelleCode(entity.getUniteOrganisationnelle().getCode());
            dto.setUniteOrganisationnelleNom(entity.getUniteOrganisationnelle().getNom());
        }
        if (entity.getPoste() != null) {
            dto.setPosteId(entity.getPoste().getId());
            dto.setPosteCode(entity.getPoste().getCodePoste());
            dto.setPosteDescription(entity.getPoste().getDescription());
        }
        if (entity.getHoraire() != null) {
            dto.setHoraireId(entity.getHoraire().getId());
            dto.setHoraireCode(entity.getHoraire().getCodeHoraire());
            dto.setHoraireDescription(entity.getHoraire().getDescription());
            dto.setHoraireGenererAbsence(entity.getHoraire().getGenererAbsence());
        }
        dto.setTauxSupplementaire(entity.getTauxSupplementaire());
        if (entity.getFonction() != null) {
            dto.setFonctionId(entity.getFonction().getId());
            dto.setFonctionCode(entity.getFonction().getCodeFonction());
            dto.setFonctionDescription(entity.getFonction().getDescription());
        }
        if (entity.getGestionnaire() != null) {
            dto.setGestionnaireId(entity.getGestionnaire().getId());
            dto.setGestionnaireCode(entity.getGestionnaire().getCodeEmploye());
            dto.setGestionnaireNom(entity.getGestionnaire().getNom());
            dto.setGestionnairePrenom(entity.getGestionnaire().getPrenom());
        }
        if (entity.getTypeConge() != null) {
            dto.setTypeCongeId(entity.getTypeConge().getId());
            dto.setTypeCongeCode(entity.getTypeConge().getCodeConge());
            dto.setTypeCongeDescription(entity.getTypeConge().getDescription());
        }
        dto.setJourOff1(entity.getJourOff1());
        dto.setJourOff2(entity.getJourOff2());
        dto.setJourOff3(entity.getJourOff3());
        dto.setEnConge(entity.getEnConge());
        dto.setEnProbation(entity.getEnProbation());
        dto.setPrincipal(entity.getPrincipal());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }
    
    // ========== Salaires Methods ==========
    @Transactional(readOnly = true)
    public List<EmployeSalaireDTO> getSalaires(Long employeId) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
        return employeSalaireRepository.findByEmployeId(employeId).stream()
                .map(this::salaireToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public EmployeSalaireDTO createSalaire(Long employeId, EmployeSalaireDTO dto, String username) {
        if (employeId == null) {
            throw new RuntimeException("EmployeId is required");
        }
        
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new RuntimeException("Employe not found with id: " + employeId));
        
        if (dto.getEmploiId() == null) {
            throw new RuntimeException("EmploiId is required for salaire creation");
        }
        
        EmploiEmploye emploi = emploiEmployeRepository.findById(dto.getEmploiId())
                .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + dto.getEmploiId()));
        
        if (dto.getRegimePaieId() == null) {
            throw new RuntimeException("RegimePaieId is required for salaire creation");
        }
        
        RegimePaie regimePaie = regimePaieRepository.findById(dto.getRegimePaieId())
                .orElseThrow(() -> new RuntimeException("RegimePaie not found with id: " + dto.getRegimePaieId()));
        
        if (dto.getMontant() == null) {
            throw new RuntimeException("Montant is required for salaire creation");
        }
        
        if (dto.getDateDebut() == null) {
            throw new RuntimeException("DateDebut is required for salaire creation");
        }
        
        EmployeSalaire entity = new EmployeSalaire();
        entity.setEmploye(employe);
        entity.setEmploi(emploi);
        entity.setRegimePaie(regimePaie);
        entity.setMontant(dto.getMontant());
        entity.setDateDebut(dto.getDateDebut());
        entity.setDateFin(dto.getDateFin());
        entity.setPrincipal(dto.getPrincipal() != null ? dto.getPrincipal() : "N");
        entity.setActif("N");
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        return salaireToDTO(employeSalaireRepository.save(entity));
    }
    
    @Transactional
    public EmployeSalaireDTO updateSalaire(Long employeId, Long salaireId, EmployeSalaireDTO dto, String username) {
        EmployeSalaire entity = employeSalaireRepository.findById(salaireId)
                .orElseThrow(() -> new RuntimeException("EmployeSalaire not found with id: " + salaireId));
        
        if (!entity.getEmploye().getId().equals(employeId)) {
            throw new RuntimeException("Salaire does not belong to this employe");
        }

        ensureNotNomme(entity.getEmploye());
        
        if (dto.getRowscn() != null && !entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        // Update emploi - required field
        if (dto.getEmploiId() == null) {
            throw new RuntimeException("EmploiId is required for salaire update");
        }
        EmploiEmploye emploi = emploiEmployeRepository.findById(dto.getEmploiId())
                .orElseThrow(() -> new RuntimeException("EmploiEmploye not found with id: " + dto.getEmploiId()));
        entity.setEmploi(emploi);
        
        // Update regimePaie - required field
        if (dto.getRegimePaieId() == null) {
            throw new RuntimeException("RegimePaieId is required for salaire update");
        }
        RegimePaie regimePaie = regimePaieRepository.findById(dto.getRegimePaieId())
                .orElseThrow(() -> new RuntimeException("RegimePaie not found with id: " + dto.getRegimePaieId()));
        entity.setRegimePaie(regimePaie);
        
        // Update montant - required field
        if (dto.getMontant() == null) {
            throw new RuntimeException("Montant is required for salaire update");
        }
        entity.setMontant(dto.getMontant());
        
        // Update dateDebut - required field
        if (dto.getDateDebut() == null) {
            throw new RuntimeException("DateDebut is required for salaire update");
        }
        entity.setDateDebut(dto.getDateDebut());
        entity.setDateFin(dto.getDateFin());
        entity.setPrincipal(dto.getPrincipal() != null ? dto.getPrincipal() : entity.getPrincipal()); // Keep existing if not provided
        // Actif is always N for updates
        entity.setActif("N");
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        return salaireToDTO(employeSalaireRepository.save(entity));
    }
    
    @Transactional
    public void deleteSalaire(Long employeId, Long salaireId) {
        EmployeSalaire entity = employeSalaireRepository.findById(salaireId)
                .orElseThrow(() -> new RuntimeException("EmployeSalaire not found with id: " + salaireId));
        
        if (!entity.getEmploye().getId().equals(employeId)) {
            throw new RuntimeException("Salaire does not belong to this employe");
        }

        ensureNotNomme(entity.getEmploye());
        
        employeSalaireRepository.delete(entity);
    }
    
    private EmployeSalaireDTO salaireToDTO(EmployeSalaire entity) {
        EmployeSalaireDTO dto = new EmployeSalaireDTO();
        dto.setId(entity.getId());
        dto.setEmployeId(entity.getEmploye().getId());
        if (entity.getEmploi() != null) {
            dto.setEmploiId(entity.getEmploi().getId());
        }
        if (entity.getRegimePaie() != null) {
            dto.setRegimePaieId(entity.getRegimePaie().getId());
            dto.setRegimePaieCode(entity.getRegimePaie().getCodeRegimePaie());
            dto.setRegimePaieDescription(entity.getRegimePaie().getDescription());
        }
        dto.setMontant(entity.getMontant());
        dto.setDateDebut(entity.getDateDebut());
        dto.setDateFin(entity.getDateFin());
        dto.setPrincipal(entity.getPrincipal());
        dto.setActif(entity.getActif());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        return dto;
    }

    private void ensureNotNomme(Employe employe) {
        if (employe != null && "Y".equalsIgnoreCase(employe.getNomme())) {
            throw new RuntimeException("Operation not allowed: employe is nomme");
        }
    }
}
