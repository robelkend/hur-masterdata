package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.dto.*;
import com.rsoft.hurmanagement.hurmasterdata.entity.Formule;
import com.rsoft.hurmanagement.hurmasterdata.repository.FormuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormuleService {
    
    private final FormuleRepository repository;
    private final FormulaExpressionEvaluator formulaEvaluator = new FormulaExpressionEvaluator();
    private static final Set<String> RESERVED_IDENTIFIERS = new HashSet<>(Arrays.asList(
            "true", "false", "y", "n",
            "if", "coalesce", "min", "max", "abs", "round", "ceil", "floor", "pow",
            "today", "now", "datediffdays", "dateadddays", "dateaddmonths", "dateaddyears",
            "dayofweek", "timediffhours"
    ));
    
    @Transactional(readOnly = true)
    public Page<FormuleDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public FormuleDTO findById(Long id) {
        Formule entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formule not found with id: " + id));
        return toDTO(entity);
    }
    
    @Transactional(readOnly = true)
    public List<FormuleDTO> findAllForDropdown() {
        return repository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    @Transactional
    public FormuleDTO create(FormuleCreateDTO dto, String username) {
        if (repository.existsByCodeVariable(dto.getCodeVariable())) {
            throw new RuntimeException("Formule with code variable " + dto.getCodeVariable() + " already exists");
        }
        
        Formule entity = new Formule();
        entity.setCodeVariable(dto.getCodeVariable());
        entity.setValeurDefaut(dto.getValeurDefaut());
        entity.setActif(dto.getActif() != null ? dto.getActif() : "Y");
        entity.setDateEffectif(dto.getDateEffectif());
        entity.setDateFin(dto.getDateFin());
        entity.setDescription(dto.getDescription());
        entity.setExpression(dto.getExpression());
        entity.setCreatedBy(username);
        entity.setCreatedOn(OffsetDateTime.now());
        entity.setRowscn(1);
        
        Formule savedEntity = repository.save(entity);
        return toDTO(savedEntity);
    }
    
    @Transactional
    public FormuleDTO update(FormuleUpdateDTO dto, String username) {
        Formule entity = repository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Formule not found with id: " + dto.getId()));
        
        if (!entity.getRowscn().equals(dto.getRowscn())) {
            throw new RuntimeException("Record has been modified by another user. Please refresh before saving.");
        }
        
        if (!entity.getCodeVariable().equals(dto.getCodeVariable()) && repository.existsByCodeVariable(dto.getCodeVariable())) {
            throw new RuntimeException("Formule with code variable " + dto.getCodeVariable() + " already exists");
        }
        
        entity.setCodeVariable(dto.getCodeVariable());
        entity.setValeurDefaut(dto.getValeurDefaut());
        entity.setActif(dto.getActif());
        entity.setDateEffectif(dto.getDateEffectif());
        entity.setDateFin(dto.getDateFin());
        entity.setDescription(dto.getDescription());
        entity.setExpression(dto.getExpression());
        entity.setUpdatedBy(username);
        entity.setUpdatedOn(OffsetDateTime.now());
        entity.setRowscn(entity.getRowscn() + 1);
        
        Formule savedEntity = repository.save(entity);
        return toDTO(savedEntity);
    }
    
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Formule not found with id: " + id);
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public FormuleTestResultDTO testExpression(FormuleTestRequestDTO dto) {
        String expression = normalizeExpression(dto.getExpression());
        if (expression == null || expression.trim().isEmpty()) {
            throw new RuntimeException("Expression is required");
        }
        Map<String, BigDecimal> variables = buildRandomVariables(expression);
        BigDecimal result = formulaEvaluator.evaluate(expression, new HashMap<>(variables), false);
        if (result == null) {
            throw new RuntimeException("Expression evaluation failed");
        }
        return new FormuleTestResultDTO(result, variables);
    }
    
    private FormuleDTO toDTO(Formule entity) {
        FormuleDTO dto = new FormuleDTO();
        dto.setId(entity.getId());
        dto.setCodeVariable(entity.getCodeVariable());
        dto.setValeurDefaut(entity.getValeurDefaut());
        dto.setActif(entity.getActif());
        dto.setDateEffectif(entity.getDateEffectif());
        dto.setDateFin(entity.getDateFin());
        dto.setDescription(entity.getDescription());
        dto.setExpression(entity.getExpression());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setRowscn(entity.getRowscn());
        
        return dto;
    }

    private String normalizeExpression(String expression) {
        if (expression == null) {
            return null;
        }
        return expression.replaceAll("\\$\\{([^}]+)\\}", "$1");
    }

    private Map<String, BigDecimal> buildRandomVariables(String expression) {
        List<String> extracted = formulaEvaluator.extractIdentifiers(expression);
        Set<String> identifiers = new LinkedHashSet<>();
        for (String name : extracted) {
            if (name == null || name.isBlank()) {
                continue;
            }
            String lower = name.toLowerCase(Locale.ROOT);
            if (RESERVED_IDENTIFIERS.contains(lower)) {
                continue;
            }
            identifiers.add(name);
        }
        Map<String, BigDecimal> values = new LinkedHashMap<>();
        Random random = new Random();
        for (String id : identifiers) {
            BigDecimal value = BigDecimal.valueOf(10 + (random.nextDouble() * 1000))
                    .setScale(2, RoundingMode.HALF_UP);
            values.put(id, value);
        }
        return values;
    }
    
}
