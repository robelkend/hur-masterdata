package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceExtraction;
import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceExtractionRequete;
import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceExtractionParam;
import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceExtractionLiaison;
import com.rsoft.hurmanagement.hurmasterdata.repository.InterfaceExtractionRequeteRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.InterfaceExtractionParamRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.InterfaceExtractionLiaisonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterfaceExtractionProcessService {
    
    private final InterfaceExtractionRequeteRepository requeteRepository;
    private final InterfaceExtractionParamRepository paramRepository;
    private final InterfaceExtractionLiaisonRepository liaisonRepository;
    private final JdbcTemplate jdbcTemplate;
    
    @Transactional(readOnly = true)
    public byte[] exportToCsv(InterfaceExtraction extraction, String username) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
            
            String separator = extraction.getSeparateur() != null ? extraction.getSeparateur() : ",";
            String delimiter = extraction.getEncadreur() != null ? extraction.getEncadreur() : "\"";
            
            // Get main queries (no parent)
            List<InterfaceExtractionRequete> mainRequetes = requeteRepository
                    .findByInterfaceExtractionIdAndParentIsNullOrderByOrdreExecutionAsc(extraction.getId())
                    .stream()
                    .filter(r -> "Y".equalsIgnoreCase(r.getActif()))
                    .collect(Collectors.toList());
            
            Map<String, List<Map<String, Object>>> queryResults = new HashMap<>();
            
            // Execute all queries
            for (InterfaceExtractionRequete requete : mainRequetes) {
                executeQueryHierarchy(requete, queryResults, new HashMap<>(), username);
            }
            
            // Write results to CSV
            boolean firstRow = true;
            for (InterfaceExtractionRequete requete : mainRequetes) {
                List<Map<String, Object>> rows = queryResults.get(requete.getId().toString());
                if (rows != null && !rows.isEmpty()) {
                    if (!firstRow) {
                        writer.write("\n");
                    }
                    // Write header
                    if (!rows.isEmpty()) {
                        String header = rows.get(0).keySet().stream()
                                .map(key -> delimiter + key + delimiter)
                                .collect(Collectors.joining(separator));
                        writer.write(header + "\n");
                    }
                    // Write data rows
                    for (Map<String, Object> row : rows) {
                        String csvRow = row.values().stream()
                                .map(value -> delimiter + (value != null ? escapeCsvValue(value.toString(), delimiter) : "") + delimiter)
                                .collect(Collectors.joining(separator));
                        writer.write(csvRow + "\n");
                    }
                    firstRow = false;
                }
            }
            
            writer.flush();
            writer.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error exporting to CSV: " + e.getMessage(), e);
        }
    }
    
    private void executeQueryHierarchy(InterfaceExtractionRequete requete, 
                                      Map<String, List<Map<String, Object>>> queryResults,
                                      Map<String, Object> parentRow,
                                      String username) {
        String sql = requete.getScriptSql();
        
        // Get parameters for this query
        List<InterfaceExtractionParam> params = paramRepository.findByRequeteIdOrderByPositionAsc(requete.getId());
        
        // Prepare parameter values
        List<Object> paramValues = new ArrayList<>();
        if (requete.getParent() != null) {
            // This is a child query, get values from parent row via liaisons
            List<InterfaceExtractionLiaison> liaisons = liaisonRepository
                    .findByRequeteFilleIdOrderByParamPositionAsc(requete.getId());
            
            for (InterfaceExtractionLiaison liaison : liaisons) {
                Object value = getLiaisonValue(liaison, parentRow, username);
                paramValues.add(value);
            }
        } else {
            // Main query, parameters should be provided externally or have default values
            // For now, we'll use null/default values - in real implementation, these would come from UI
            for (InterfaceExtractionParam param : params) {
                paramValues.add(getDefaultValue(param));
            }
        }
        
        // Execute query
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, paramValues.toArray());
        queryResults.put(requete.getId().toString(), rows);
        
        // Execute child queries for each row
        List<InterfaceExtractionRequete> enfants = requeteRepository
                .findByParentIdOrderByOrdreExecutionAsc(requete.getId())
                .stream()
                .filter(r -> "Y".equalsIgnoreCase(r.getActif()))
                .collect(Collectors.toList());
        
        for (Map<String, Object> row : rows) {
            for (InterfaceExtractionRequete enfant : enfants) {
                executeQueryHierarchy(enfant, queryResults, row, username);
            }
        }
    }
    
    private Object getLiaisonValue(InterfaceExtractionLiaison liaison, Map<String, Object> parentRow, String username) {
        switch (liaison.getSourceType()) {
            case PARENT_COL:
                return parentRow.get(liaison.getSourceValeur());
            case EXTERNAL:
                // In real implementation, this would come from external parameters
                return null;
            case CONSTANT:
                return liaison.getSourceValeur();
            default:
                return null;
        }
    }
    
    private Object getDefaultValue(InterfaceExtractionParam param) {
        // Return default values based on type - in real implementation, these would come from UI
        switch (param.getTypeParam()) {
            case INTEGER:
                return 0;
            case DECIMAL:
                return 0.0;
            case BOOLEAN:
                return false;
            case DATE:
                return new java.sql.Date(System.currentTimeMillis());
            default:
                return null;
        }
    }
    
    private String escapeCsvValue(String value, String delimiter) {
        if (value == null) {
            return "";
        }
        // Escape delimiter if present in value
        if (value.contains(delimiter)) {
            value = value.replace(delimiter, delimiter + delimiter);
        }
        // Replace newlines with space
        value = value.replace("\n", " ").replace("\r", " ");
        return value;
    }
}
