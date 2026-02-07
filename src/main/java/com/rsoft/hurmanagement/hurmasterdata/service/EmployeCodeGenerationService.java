package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.entity.ParamGenerationCodeEmploye;
import com.rsoft.hurmanagement.hurmasterdata.entity.TypeEmploye;
import com.rsoft.hurmanagement.hurmasterdata.repository.ParamGenerationCodeEmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeCodeGenerationService {
    
    private final ParamGenerationCodeEmployeRepository paramRepository;
    
    /**
     * Génère automatiquement le code employé si un paramètre de génération existe
     * @param entrepriseId ID de l'entreprise (optionnel)
     * @param typeEmployeId ID du type d'employé (optionnel)
     * @param nom Nom de l'employé
     * @param prenom Prénom de l'employé
     * @param dateNaissance Date de naissance (optionnel, pour pattern)
     * @return Code généré ou null si aucun paramètre n'existe
     */
    @Transactional(readOnly = true)
    public String generateCode(Long entrepriseId, Long typeEmployeId, String nom, String prenom, LocalDate dateNaissance) {
        LocalDate today = LocalDate.now();
        
        // Chercher un paramètre de génération applicable
        List<ParamGenerationCodeEmploye> params = paramRepository.findByActifAndDateEffectifLessThanEqualAndDateFinGreaterThanEqualOrDateFinIsNull(
            "Y", today, today);
        
        Optional<ParamGenerationCodeEmploye> matchingParam = params.stream()
            .filter(p -> (p.getEntreprise() == null || (entrepriseId != null && p.getEntreprise().getId().equals(entrepriseId))))
            .filter(p -> (p.getTypeEmploye() == null || (typeEmployeId != null && p.getTypeEmploye().getId().equals(typeEmployeId))))
            .findFirst();
        
        if (matchingParam.isEmpty()) {
            return null; // Pas de génération automatique
        }
        
        ParamGenerationCodeEmploye param = matchingParam.get();
        
        if (param.getModeGeneration() == ParamGenerationCodeEmploye.ModeGeneration.SEQUENCE) {
            return generateSequenceCode(param);
        } else {
            return generatePatternCode(param, nom, prenom, dateNaissance);
        }
    }
    
    private String generateSequenceCode(ParamGenerationCodeEmploye param) {
        Integer currentValue = param.getValeurCourante() != null ? param.getValeurCourante() : param.getValeurDepart();
        if (currentValue == null) {
            currentValue = 1;
        }
        
        String code = "";
        if (param.getPrefixeFixe() != null) {
            code += param.getPrefixeFixe();
        }
        
        String sequence = String.valueOf(currentValue);
        if (param.getLongueurMin() != null && sequence.length() < param.getLongueurMin()) {
            sequence = String.format("%" + param.getLongueurMin() + "s", sequence)
                .replace(' ', param.getPaddingChar().charAt(0));
        }
        code += sequence;
        
        if (param.getSuffixeFixe() != null) {
            code += param.getSuffixeFixe();
        }
        
        // Mettre à jour la valeur courante (sera fait dans le service appelant)
        param.setValeurCourante(currentValue + param.getPasIncrementation());
        paramRepository.save(param);
        
        return param.getMajuscules().equals("Y") ? code.toUpperCase() : code;
    }
    
    private String generatePatternCode(ParamGenerationCodeEmploye param, String nom, String prenom, LocalDate dateNaissance) {
        // Implémentation simplifiée du pattern
        // Pour une implémentation complète, utiliser un parser de pattern
        String pattern = param.getPattern();
        if (pattern == null || pattern.isEmpty()) {
            return generateSequenceCode(param); // Fallback
        }
        
        String code = pattern;
        if (nom != null) {
            String nomProcessed = nom;
            if ("Y".equals(param.getEnleverAccents())) {
                nomProcessed = removeAccents(nomProcessed);
            }
            if ("Y".equals(param.getMajuscules())) {
                nomProcessed = nomProcessed.toUpperCase();
            }
            code = code.replace("{LN}", nomProcessed.substring(0, Math.min(3, nomProcessed.length())));
        }
        if (prenom != null) {
            String prenomProcessed = prenom;
            if ("Y".equals(param.getEnleverAccents())) {
                prenomProcessed = removeAccents(prenomProcessed);
            }
            if ("Y".equals(param.getMajuscules())) {
                prenomProcessed = prenomProcessed.toUpperCase();
            }
            code = code.replace("{FN}", prenomProcessed.substring(0, Math.min(1, prenomProcessed.length())));
        }
        if (dateNaissance != null) {
            code = code.replace("{DOB:yyyyMMdd}", dateNaissance.toString().replace("-", ""));
        }
        
        // Sequence dans pattern
        if (code.contains("{SEQ")) {
            Integer currentValue = param.getValeurCourante() != null ? param.getValeurCourante() : param.getValeurDepart();
            if (currentValue == null) currentValue = 1;
            String seq = String.format("%06d", currentValue);
            code = code.replaceAll("\\{SEQ:\\d+\\}", seq);
            param.setValeurCourante(currentValue + param.getPasIncrementation());
            paramRepository.save(param);
        }
        
        return code;
    }
    
    private String removeAccents(String text) {
        if (text == null) return null;
        return text.replaceAll("[àáâãäå]", "a")
                   .replaceAll("[èéêë]", "e")
                   .replaceAll("[ìíîï]", "i")
                   .replaceAll("[òóôõö]", "o")
                   .replaceAll("[ùúûü]", "u")
                   .replaceAll("[ýÿ]", "y")
                   .replaceAll("[ñ]", "n")
                   .replaceAll("[ç]", "c");
    }
}
