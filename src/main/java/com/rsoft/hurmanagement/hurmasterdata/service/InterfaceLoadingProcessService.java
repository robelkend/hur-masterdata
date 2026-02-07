package com.rsoft.hurmanagement.hurmasterdata.service;

import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceLoading;
import com.rsoft.hurmanagement.hurmasterdata.entity.InterfaceLoadingChamp;
import com.rsoft.hurmanagement.hurmasterdata.repository.InterfaceLoadingRepository;
import com.rsoft.hurmanagement.hurmasterdata.repository.InterfaceLoadingChampRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterfaceLoadingProcessService {
    
    private final InterfaceLoadingRepository loadingRepository;
    private final InterfaceLoadingChampRepository champRepository;
    private final JdbcTemplate jdbcTemplate;
    
    @Transactional
    public Map<String, Object> loadCsv(Long loadingId, MultipartFile file, String username) {
        InterfaceLoading loading = loadingRepository.findById(loadingId)
                .orElseThrow(() -> new RuntimeException("InterfaceLoading not found with id: " + loadingId));
        
        if (loading.getSource() != InterfaceLoading.Source.FILE) {
            throw new RuntimeException("This loading configuration is not for FILE source");
        }
        
        List<InterfaceLoadingChamp> champs = champRepository.findByLoadingId(loadingId);
        if (champs.isEmpty()) {
            throw new RuntimeException("No field mappings configured for this loading");
        }
        validateTargetTableColumns(loading.getTableCible(), champs);
        
        // Sort champs by position (excluding position -1 which are fixed values)
        List<InterfaceLoadingChamp> sortedChamps = champs.stream()
                .filter(c -> c.getPosition() > 0)
                .sorted(Comparator.comparing(InterfaceLoadingChamp::getPosition))
                .collect(Collectors.toList());
        
        List<InterfaceLoadingChamp> fixedValueChamps = champs.stream()
                .filter(c -> c.getPosition() == -1)
                .collect(Collectors.toList());
        
        // Parse CSV
        String separator = loading.getSeparateurChamp() != null ? loading.getSeparateurChamp() : ",";
        String delimiter = loading.getDelimiteurChamp() != null ? loading.getDelimiteurChamp() : "\"";
        int skipLines = loading.getExclusLignes() != null ? loading.getExclusLignes() : 0;
        boolean excludeLastLine = "Y".equalsIgnoreCase(loading.getExclusDerniereLigne());
        
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int processedRows = 0;
        int successRows = 0;
        int errorRows = 0;
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            int lineNumber = 0;
            List<String> allLines = new ArrayList<>();
            
            // Read all lines first
            while ((line = reader.readLine()) != null) {
                allLines.add(line);
            }
            
            // Exclude last line if needed
            if (excludeLastLine && !allLines.isEmpty()) {
                allLines.remove(allLines.size() - 1);
            }
            
            // Process each line
            for (String csvLine : allLines) {
                lineNumber++;
                
                // Skip header lines
                if (lineNumber <= skipLines) {
                    continue;
                }
                
                if (csvLine.trim().isEmpty()) {
                    continue;
                }
                
                processedRows++;
                
                try {
                    // Parse CSV line
                    List<String> values = parseCsvLine(csvLine, separator, delimiter);
                    
                    // Map values to target columns
                    Map<String, Object> rowData = new LinkedHashMap<>();
                    
                    // Add fixed values first
                    for (InterfaceLoadingChamp champ : fixedValueChamps) {
                        Object value = getFixedValue(champ, username);
                        rowData.put(champ.getNomCible(), value);
                    }
                    
                    // Add mapped values
                    for (int i = 0; i < sortedChamps.size() && i < values.size(); i++) {
                        InterfaceLoadingChamp champ = sortedChamps.get(i);
                        String rawValue = values.get(i);
                        
                        // Validate required fields
                        if ("Y".equalsIgnoreCase(champ.getObligatoire()) && (rawValue == null || rawValue.trim().isEmpty())) {
                            throw new RuntimeException("Required field " + champ.getNomCible() + " is empty at line " + lineNumber);
                        }
                        
                        Object convertedValue = convertValue(rawValue, champ);
                        rowData.put(champ.getNomCible(), convertedValue);
                    }
                    
                    // Insert into target table
                    insertIntoTable(loading.getTableCible(), rowData);
                    successRows++;
                    rows.add(rowData);
                    
                } catch (Exception e) {
                    errorRows++;
                    errors.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error processing CSV file: " + e.getMessage(), e);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("processedRows", processedRows);
        result.put("successRows", successRows);
        result.put("errorRows", errorRows);
        result.put("errors", errors);
        result.put("message", "Processed " + successRows + " rows successfully, " + errorRows + " errors");
        
        return result;
    }
    
    @Transactional
    public Map<String, Object> loadFromDatabase(Long loadingId, String username) {
        InterfaceLoading loading = loadingRepository.findById(loadingId)
                .orElseThrow(() -> new RuntimeException("InterfaceLoading not found with id: " + loadingId));
        
        if (loading.getSource() != InterfaceLoading.Source.RDB) {
            throw new RuntimeException("This loading configuration is not for RDB source");
        }
        if ((loading.getRdbUrl() == null || loading.getRdbUrl().trim().isEmpty())
                || (loading.getRdbDriver() == null || loading.getRdbDriver().trim().isEmpty())
                || (loading.getRdbUsername() == null || loading.getRdbUsername().trim().isEmpty())
                || (loading.getRdbPassword() == null || loading.getRdbPassword().trim().isEmpty())) {
            throw new RuntimeException("RDB connection is not fully configured (url/driver/username/password)");
        }
        boolean hasQuery = loading.getRdbQuery() != null && !loading.getRdbQuery().trim().isEmpty();
        boolean hasTableSource = loading.getTableSource() != null && !loading.getTableSource().trim().isEmpty();
        if (!hasQuery && !hasTableSource) {
            throw new RuntimeException("Table source is not configured for this loading");
        }
        
        List<InterfaceLoadingChamp> champs = champRepository.findByLoadingId(loadingId);
        if (champs.isEmpty()) {
            throw new RuntimeException("No field mappings configured for this loading");
        }
        
        // Build SQL query
        String sql;
        if (hasQuery) {
            sql = loading.getRdbQuery().trim();
        } else {
            List<String> selectFields = new ArrayList<>();
            for (InterfaceLoadingChamp champ : champs) {
                if (champ.getPosition() > 0 && champ.getNomSource() != null && !champ.getNomSource().trim().isEmpty()) {
                    String nomSource = champ.getNomSource().trim();
                    if (nomSource.contains(".")) {
                        String alias = nomSource.replace(".", "_");
                        selectFields.add(nomSource + " AS " + alias);
                    } else {
                        selectFields.add(nomSource);
                    }
                }
            }
            if (selectFields.isEmpty()) {
                throw new RuntimeException("No source fields configured for RDB mapping");
            }
            String tableSource = loading.getTableSource().trim();
            String schema = loading.getRdbSchema() != null ? loading.getRdbSchema().trim() : "";
            if (!schema.isEmpty() && !tableSource.contains(",") && !tableSource.toLowerCase().contains(" join ")) {
                if (!tableSource.contains(".")) {
                    tableSource = schema + "." + tableSource;
                } else if (!tableSource.startsWith(schema + ".")) {
                    int lastDot = tableSource.lastIndexOf('.');
                    String rawTable = lastDot >= 0 ? tableSource.substring(lastDot + 1) : tableSource;
                    tableSource = schema + "." + rawTable;
                }
            }
            sql = "SELECT " + String.join(", ", selectFields) + " FROM " + tableSource;
            if (loading.getExtraClause() != null && !loading.getExtraClause().trim().isEmpty()) {
                sql += " " + loading.getExtraClause();
            }
        }
        
        // Execute query against external DB
        JdbcTemplate externalJdbcTemplate = resolveRdbJdbcTemplate(loading);
        validateMysqlTableExists(externalJdbcTemplate, loading, sql);
        List<Map<String, Object>> sourceRows;
        try {
            sourceRows = externalJdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            String hint = buildRdbHint(e);
            throw new RuntimeException("RDB query failed: " + e.getMessage() + " SQL=[" + sql + "]" + hint, e);
        }
        
        List<String> errors = new ArrayList<>();
        int successRows = 0;
        int errorRows = 0;
        
        Map<String, String> aliasToTable = parseTableAliases(loading.getTableSource());
        for (Map<String, Object> sourceRow : sourceRows) {
            try {
                Map<String, Object> rowData = new LinkedHashMap<>();

                // Map source fields to target fields
                for (InterfaceLoadingChamp champ : champs) {
                    if (champ.getPosition() == -1) {
                        // Fixed value
                        Object value = getFixedValue(champ, username);
                        rowData.put(champ.getNomCible(), value);
                    } else if (champ.getNomSource() != null && !champ.getNomSource().trim().isEmpty()) {
                        Object sourceValue = getSourceValue(sourceRow, champ.getNomSource());
                        if (sourceValue != null) {
                            Object convertedValue = convertValue(sourceValue.toString(), champ);
                            rowData.put(champ.getNomCible(), convertedValue);
                        }
                    }
                }

                if (rowData.isEmpty()) {
                    throw new RuntimeException("No mapped values found for target insert");
                }

                // Update source if configured (must succeed before insert)
                applySourceUpdates(champs, sourceRow, aliasToTable, externalJdbcTemplate, username);

                // Insert into target table
                insertIntoTable(loading.getTableCible(), rowData);
                successRows++;

            } catch (Exception e) {
				e.printStackTrace(System.out);
                errorRows++;
                errors.add("Row error: " + e.getMessage());
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("processedRows", sourceRows.size());
        result.put("successRows", successRows);
        result.put("errorRows", errorRows);
        result.put("errors", errors);
        result.put("message", "Processed " + successRows + " rows successfully, " + errorRows + " errors");
        
        return result;
    }

    @Transactional
    public Map<String, Object> loadFromDatabaseWithParams(Long loadingId, String username, Map<String, String> params) {
        InterfaceLoading loading = loadingRepository.findById(loadingId)
                .orElseThrow(() -> new RuntimeException("InterfaceLoading not found with id: " + loadingId));

        if (loading.getSource() != InterfaceLoading.Source.RDB) {
            throw new RuntimeException("This loading configuration is not for RDB source");
        }
        if ((loading.getRdbUrl() == null || loading.getRdbUrl().trim().isEmpty())
                || (loading.getRdbDriver() == null || loading.getRdbDriver().trim().isEmpty())
                || (loading.getRdbUsername() == null || loading.getRdbUsername().trim().isEmpty())
                || (loading.getRdbPassword() == null || loading.getRdbPassword().trim().isEmpty())) {
            throw new RuntimeException("RDB connection is not fully configured (url/driver/username/password)");
        }
        boolean hasQuery = loading.getRdbQuery() != null && !loading.getRdbQuery().trim().isEmpty();
        boolean hasTableSource = loading.getTableSource() != null && !loading.getTableSource().trim().isEmpty();
        if (!hasQuery && !hasTableSource) {
            throw new RuntimeException("Table source is not configured for this loading");
        }

        List<InterfaceLoadingChamp> champs = champRepository.findByLoadingId(loadingId);
        if (champs.isEmpty()) {
            throw new RuntimeException("No field mappings configured for this loading");
        }

        String sql;
        if (hasQuery) {
            sql = applyTemplate(loading.getRdbQuery().trim(), params);
        } else {
            List<String> selectFields = new ArrayList<>();
            for (InterfaceLoadingChamp champ : champs) {
                if (champ.getPosition() > 0 && champ.getNomSource() != null && !champ.getNomSource().trim().isEmpty()) {
                    String nomSource = champ.getNomSource().trim();
                    if (nomSource.contains(".")) {
                        String alias = nomSource.replace(".", "_");
                        selectFields.add(nomSource + " AS " + alias);
                    } else {
                        selectFields.add(nomSource);
                    }
                }
            }
            if (selectFields.isEmpty()) {
                throw new RuntimeException("No source fields configured for RDB mapping");
            }
            String tableSource = loading.getTableSource().trim();
            String schema = loading.getRdbSchema() != null ? loading.getRdbSchema().trim() : "";
            if (!schema.isEmpty() && !tableSource.contains(",") && !tableSource.toLowerCase().contains(" join ")) {
                if (!tableSource.contains(".")) {
                    tableSource = schema + "." + tableSource;
                } else if (!tableSource.startsWith(schema + ".")) {
                    int lastDot = tableSource.lastIndexOf('.');
                    String rawTable = lastDot >= 0 ? tableSource.substring(lastDot + 1) : tableSource;
                    tableSource = schema + "." + rawTable;
                }
            }
            sql = "SELECT " + String.join(", ", selectFields) + " FROM " + tableSource;
            if (loading.getExtraClause() != null && !loading.getExtraClause().trim().isEmpty()) {
                sql += " " + applyTemplate(loading.getExtraClause().trim(), params);
            }
        }

        JdbcTemplate externalJdbcTemplate = resolveRdbJdbcTemplate(loading);
        validateMysqlTableExists(externalJdbcTemplate, loading, sql);
        List<Map<String, Object>> sourceRows;
        try {
            sourceRows = externalJdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            String hint = buildRdbHint(e);
            throw new RuntimeException("RDB query failed: " + e.getMessage() + " SQL=[" + sql + "]" + hint, e);
        }

        List<String> errors = new ArrayList<>();
        int successRows = 0;
        int errorRows = 0;

        Map<String, String> aliasToTable = parseTableAliases(loading.getTableSource());
        for (Map<String, Object> sourceRow : sourceRows) {
            try {
                Map<String, Object> rowData = new LinkedHashMap<>();

                for (InterfaceLoadingChamp champ : champs) {
                    if (champ.getPosition() == -1) {
                        Object value = getFixedValue(champ, username);
                        rowData.put(champ.getNomCible(), value);
                    } else if (champ.getNomSource() != null && !champ.getNomSource().trim().isEmpty()) {
                        Object sourceValue = getSourceValue(sourceRow, champ.getNomSource());
                        if (sourceValue != null) {
                            Object convertedValue = convertValue(sourceValue.toString(), champ);
                            rowData.put(champ.getNomCible(), convertedValue);
                        }
                    }
                }

                if (rowData.isEmpty()) {
                    throw new RuntimeException("No mapped values found for target insert");
                }

                applySourceUpdates(champs, sourceRow, aliasToTable, externalJdbcTemplate, username);
                insertIntoTable(loading.getTableCible(), rowData);
                successRows++;
            } catch (Exception e) {
                e.printStackTrace(System.out);
                errorRows++;
                errors.add("Row error: " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("processedRows", sourceRows.size());
        result.put("successRows", successRows);
        result.put("errorRows", errorRows);
        result.put("errors", errors);
        result.put("message", "Processed " + successRows + " rows successfully, " + errorRows + " errors");

        return result;
    }

    private JdbcTemplate resolveRdbJdbcTemplate(InterfaceLoading loading) {
        org.springframework.jdbc.datasource.DriverManagerDataSource dataSource =
                new org.springframework.jdbc.datasource.DriverManagerDataSource();
        dataSource.setUrl(loading.getRdbUrl());
        dataSource.setDriverClassName(loading.getRdbDriver());
        dataSource.setUsername(loading.getRdbUsername());
        dataSource.setPassword(loading.getRdbPassword());
        JdbcTemplate template = new JdbcTemplate(dataSource);
        if (loading.getRdbSchema() != null && !loading.getRdbSchema().trim().isEmpty()) {
            try {
                String schema = loading.getRdbSchema().trim();
                String url = loading.getRdbUrl() != null ? loading.getRdbUrl().toLowerCase() : "";
                String driver = loading.getRdbDriver() != null ? loading.getRdbDriver().toLowerCase() : "";
                if (url.contains("jdbc:mysql") || driver.contains("mysql")) {
                    template.execute("USE " + schema);
                } else {
                    template.execute("SET search_path TO " + schema);
                }
            } catch (Exception ignored) {
                // Ignore schema setup errors; query can include schema-qualified names instead.
            }
        }
        validateMysqlDatabaseProvided(loading);
        return template;
    }

    private void validateMysqlDatabaseProvided(InterfaceLoading loading) {
        String url = loading.getRdbUrl() != null ? loading.getRdbUrl().toLowerCase() : "";
        String driver = loading.getRdbDriver() != null ? loading.getRdbDriver().toLowerCase() : "";
        if (!(url.contains("jdbc:mysql") || driver.contains("mysql"))) {
            return;
        }
        // If URL has no database and no schema provided, fail fast.
        boolean hasSchema = loading.getRdbSchema() != null && !loading.getRdbSchema().trim().isEmpty();
        if (!hasSchema && !mysqlUrlHasDatabase(url)) {
            throw new RuntimeException("MySQL URL must include a database name or rdb_schema must be set");
        }
    }

    private boolean mysqlUrlHasDatabase(String url) {
        // jdbc:mysql://host:port/dbname?params
        int idx = url.indexOf("jdbc:mysql://");
        if (idx < 0) {
            return false;
        }
        int slash = url.indexOf('/', idx + "jdbc:mysql://".length());
        if (slash < 0) {
            return false;
        }
        int q = url.indexOf('?', slash + 1);
        String dbPart = q > 0 ? url.substring(slash + 1, q) : url.substring(slash + 1);
        return dbPart != null && !dbPart.trim().isEmpty();
    }

    private void validateMysqlTableExists(JdbcTemplate template, InterfaceLoading loading, String sql) {
        String url = loading.getRdbUrl() != null ? loading.getRdbUrl().toLowerCase() : "";
        String driver = loading.getRdbDriver() != null ? loading.getRdbDriver().toLowerCase() : "";
        if (!(url.contains("jdbc:mysql") || driver.contains("mysql"))) {
            return;
        }
        String schema = resolveMysqlSchema(loading, template);
        String tableName = extractTableName(sql);
        if (tableName != null && (tableName.contains(",") || sql.toLowerCase().contains(" join "))) {
            return;
        }
        if (schema == null || schema.isBlank() || tableName == null || tableName.isBlank()) {
            return;
        }
        Integer count = template.queryForObject(
                "SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?",
                Integer.class,
                schema,
                tableName
        );
        if (count == null || count == 0) {
            throw new RuntimeException("Table '" + schema + "." + tableName + "' not found in MySQL");
        }
    }

    private String resolveMysqlSchema(InterfaceLoading loading, JdbcTemplate template) {
        if (loading.getRdbSchema() != null && !loading.getRdbSchema().trim().isEmpty()) {
            return loading.getRdbSchema().trim();
        }
        String url = loading.getRdbUrl() != null ? loading.getRdbUrl() : "";
        String parsed = parseMysqlDatabaseFromUrl(url);
        if (parsed != null && !parsed.isBlank()) {
            return parsed;
        }
        try {
            String current = template.queryForObject("SELECT DATABASE()", String.class);
            return current != null ? current.trim() : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    private String parseMysqlDatabaseFromUrl(String url) {
        String lower = url.toLowerCase();
        int idx = lower.indexOf("jdbc:mysql://");
        if (idx < 0) {
            return null;
        }
        int slash = url.indexOf('/', idx + "jdbc:mysql://".length());
        if (slash < 0) {
            return null;
        }
        int q = url.indexOf('?', slash + 1);
        String dbPart = q > 0 ? url.substring(slash + 1, q) : url.substring(slash + 1);
        return dbPart != null ? dbPart.trim() : null;
    }

    private String extractTableName(String sql) {
        String lower = sql.toLowerCase();
        int idx = lower.indexOf(" from ");
        if (idx < 0) {
            return null;
        }
        String after = sql.substring(idx + 6).trim();
        int space = after.indexOf(' ');
        String table = space >= 0 ? after.substring(0, space) : after;
        int dot = table.lastIndexOf('.');
        return dot >= 0 ? table.substring(dot + 1) : table;
    }

    private String buildRdbHint(Exception e) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        String message = cause.getMessage() != null ? cause.getMessage() : "";
        if (message.contains("doesn't exist") && message.contains("Table")) {
            String missing = extractMissingTable(message);
            if (missing != null) {
                return " Hint: table not found in DB '" + missing + "'. Set rdb_schema or include the correct database in rdb_url or table_source.";
            }
            return " Hint: table not found. Set rdb_schema or include the correct database in rdb_url.";
        }
        return "";
    }

    private String applyTemplate(String input, Map<String, String> params) {
        if (input == null || params == null || params.isEmpty()) {
            return input;
        }
        String result = input;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(key, value);
        }
        return result;
    }

    private String extractMissingTable(String message) {
        int start = message.indexOf("Table '");
        if (start < 0) {
            return null;
        }
        int from = start + "Table '".length();
        int end = message.indexOf("'", from);
        if (end < 0) {
            return null;
        }
        return message.substring(from, end);
    }

    private Map<String, String> parseTableAliases(String tableSource) {
        Map<String, String> map = new HashMap<>();
        if (tableSource == null) {
            return map;
        }
        String cleaned = tableSource.replaceAll("(?i)\\s+join\\s+", ",");
        String[] parts = cleaned.split(",");
        for (String part : parts) {
            String[] tokens = part.trim().split("\\s+");
            if (tokens.length == 0) {
                continue;
            }
            String table = tokens[0].trim();
            String alias = null;
            if (tokens.length >= 2) {
                if ("as".equalsIgnoreCase(tokens[1]) && tokens.length >= 3) {
                    alias = tokens[2].trim();
                } else {
                    alias = tokens[1].trim();
                }
            }
            if (alias != null && !alias.isEmpty()) {
                map.put(alias, table);
            }
            String tableName = table;
            if (tableName.contains(".")) {
                String[] t = tableName.split("\\.");
                tableName = t[t.length - 1];
            }
            map.put(tableName, table);
        }
        return map;
    }

    private void applySourceUpdates(List<InterfaceLoadingChamp> champs,
                                    Map<String, Object> sourceRow,
                                    Map<String, String> aliasToTable,
                                    JdbcTemplate externalJdbcTemplate,
                                    String username) {
        for (InterfaceLoadingChamp champ : champs) {
            if (champ.getUpdateChamp() == null || champ.getUpdateChamp().trim().isEmpty()) {
                continue;
            }
            String updateChamp = champ.getUpdateChamp().trim();
            String updateValeur = champ.getUpdateValeur();
            if (updateValeur == null) {
                continue;
            }
            String alias = null;
            String column = updateChamp;
            if (updateChamp.contains(".")) {
                String[] parts = updateChamp.split("\\.");
                alias = parts[0].trim();
                column = parts[1].trim();
            }
            String table = alias != null ? aliasToTable.get(alias) : aliasToTable.getOrDefault(column, null);
            if (table == null) {
                // fallback: use alias as table if no mapping
                table = alias != null ? alias : null;
            }
            if (table == null || table.isEmpty()) {
                continue;
            }

            String where = champ.getUpdateCondition();
            List<Object> params = new ArrayList<>();
            String updateSql;
            if (where != null && !where.trim().isEmpty()) {
                String condition = where.trim();
                if (condition.contains(":value")) {
                    Object sourceValue = getSourceValue(sourceRow, champ.getNomSource());
                    condition = condition.replace(":value", "?");
                    params.add(sourceValue);
                }
                updateSql = "UPDATE " + table + (alias != null ? " " + alias : "") +
                        " SET " + (alias != null ? alias + "." : "") + column + " = ?" +
                        " WHERE " + condition;
            } else {
                Object sourceValue = getSourceValue(sourceRow, champ.getNomSource());
                if (sourceValue == null) {
                    continue;
                }
                String whereColumn = champ.getNomSource() != null ? champ.getNomSource().trim() : null;
                if (whereColumn == null || whereColumn.isEmpty()) {
                    continue;
                }
                updateSql = "UPDATE " + table + (alias != null ? " " + alias : "") +
                        " SET " + (alias != null ? alias + "." : "") + column + " = ?" +
                        " WHERE " + whereColumn + " = ?";
                params.add(sourceValue);
            }
            Object updateValue = resolveUpdateValue(updateValeur, username);
            List<Object> allParams = new ArrayList<>();
            allParams.add(updateValue);
            allParams.addAll(params);
            externalJdbcTemplate.update(updateSql, allParams.toArray());
        }
    }

    private Object getSourceValue(Map<String, Object> sourceRow, String nomSource) {
        if (nomSource == null || nomSource.trim().isEmpty()) {
            return null;
        }
        Object value = sourceRow.get(nomSource);
        if (value != null) {
            return value;
        }
        // Try without alias
        if (nomSource.contains(".")) {
            String[] parts = nomSource.split("\\.");
            String col = parts[parts.length - 1];
            Object byCol = sourceRow.get(col);
            if (byCol != null) {
                return byCol;
            }
            String alias = nomSource.replace(".", "_");
            return sourceRow.get(alias);
        }
        return null;
    }

    private Object resolveUpdateValue(String rawValue, String username) {
        if (rawValue == null) {
            return null;
        }
        String value = rawValue.trim();
        if ("USERNAME".equalsIgnoreCase(value)) {
            return username;
        }
        if ("now()".equalsIgnoreCase(value) || "current_timestamp".equalsIgnoreCase(value)) {
            return LocalDateTime.now();
        }
        if (value.matches("^-?\\d+(\\.\\d+)?$")) {
            return new BigDecimal(value);
        }
        return value;
    }

    private void validateTargetTableColumns(String tableCible, List<InterfaceLoadingChamp> champs) {
        if (tableCible == null || tableCible.trim().isEmpty()) {
            throw new RuntimeException("Target table is not configured");
        }
        String schema = "public";
        String table = tableCible.trim();
        if (table.contains(".")) {
            String[] parts = table.split("\\.");
            if (parts.length >= 2) {
                schema = parts[0].trim();
                table = parts[1].trim();
            }
        }

        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT column_name, is_nullable, column_default FROM information_schema.columns " +
                "WHERE table_schema = ? AND table_name = ?",
                schema.toLowerCase(), table.toLowerCase()
        );

        if (columns.isEmpty()) {
            throw new RuntimeException("Target table '" + schema + "." + table + "' not found");
        }

        List<String> targetCols = columns.stream()
                .map(c -> String.valueOf(c.get("column_name")).toLowerCase())
                .collect(Collectors.toList());

        List<String> missing = champs.stream()
                .map(InterfaceLoadingChamp::getNomCible)
                .filter(n -> n != null && !n.trim().isEmpty())
                .map(n -> n.trim().toLowerCase())
                .filter(n -> !targetCols.contains(n))
                .distinct()
                .collect(Collectors.toList());

        if (!missing.isEmpty()) {
            throw new RuntimeException("Target table is missing columns: " + String.join(", ", missing));
        }

        List<String> provided = champs.stream()
                .map(InterfaceLoadingChamp::getNomCible)
                .filter(n -> n != null && !n.trim().isEmpty())
                .map(n -> n.trim().toLowerCase())
                .collect(Collectors.toList());

        List<String> requiredMissing = new ArrayList<>();
        for (Map<String, Object> col : columns) {
            String colName = String.valueOf(col.get("column_name")).toLowerCase();
            String isNullable = String.valueOf(col.get("is_nullable"));
            Object colDefault = col.get("column_default");
            if ("NO".equalsIgnoreCase(isNullable) && colDefault == null && !provided.contains(colName)) {
                requiredMissing.add(colName);
            }
        }
        if (!requiredMissing.isEmpty()) {
            throw new RuntimeException("Missing required target columns (NOT NULL without default): " +
                    String.join(", ", requiredMissing));
        }
    }
    
    @Transactional
    public Map<String, Object> loadFromApi(Long loadingId, String username) {
        // TODO: Implement API loading
        throw new UnsupportedOperationException("API loading not yet implemented");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> testRdbConnection(Long loadingId) {
        InterfaceLoading loading = loadingRepository.findById(loadingId)
                .orElseThrow(() -> new RuntimeException("InterfaceLoading not found with id: " + loadingId));
        if (loading.getSource() != InterfaceLoading.Source.RDB) {
            throw new RuntimeException("This loading configuration is not for RDB source");
        }
        if ((loading.getRdbUrl() == null || loading.getRdbUrl().trim().isEmpty())
                || (loading.getRdbDriver() == null || loading.getRdbDriver().trim().isEmpty())
                || (loading.getRdbUsername() == null || loading.getRdbUsername().trim().isEmpty())
                || (loading.getRdbPassword() == null || loading.getRdbPassword().trim().isEmpty())) {
            throw new RuntimeException("RDB connection is not fully configured (url/driver/username/password)");
        }

        JdbcTemplate template = resolveRdbJdbcTemplate(loading);
        template.queryForObject("SELECT 1", Integer.class);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Connection OK");
        return result;
    }
    
    private List<String> parseCsvLine(String line, String separator, String delimiter) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentValue = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == delimiter.charAt(0)) {
                inQuotes = !inQuotes;
            } else if (!inQuotes && line.substring(i).startsWith(separator)) {
                values.add(currentValue.toString().trim());
                currentValue = new StringBuilder();
                i += separator.length() - 1;
            } else {
                currentValue.append(c);
            }
        }
        
        values.add(currentValue.toString().trim());
        return values;
    }
    
    private Object convertValue(String rawValue, InterfaceLoadingChamp champ) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return null;
        }
        
        rawValue = rawValue.trim();
        
        switch (champ.getTypeDonnee()) {
            case DATE:
                if (champ.getFormat() != null && !champ.getFormat().isEmpty()) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(champ.getFormat());
                        // Try datetime first, then date
                        try {
                            return LocalDateTime.parse(rawValue, formatter);
                        } catch (Exception ignored) {
                            return LocalDate.parse(rawValue, formatter);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid date format for value: " + rawValue);
                    }
                }
                // Auto-detect ISO date or datetime
                try {
                    return LocalDate.parse(rawValue);
                } catch (Exception ignored) {
                }
                try {
                    return LocalDateTime.parse(rawValue);
                } catch (Exception ignored) {
                }
                try {
                    return LocalDateTime.parse(rawValue.replace(" ", "T"));
                } catch (Exception ignored) {
                }
                throw new RuntimeException("Invalid date format for value: " + rawValue);
                
            case DOUBLE:
                try {
                    return Double.parseDouble(rawValue.replace(",", "."));
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Invalid number format for value: " + rawValue);
                }
                
            case FUNCTION:
                // Handle functions like now()
                if ("now()".equalsIgnoreCase(rawValue) || "current_timestamp".equalsIgnoreCase(rawValue)) {
                    return LocalDateTime.now();
                }
                return rawValue;
                
            case EXTRA:
                // Handle extra values like USERNAME
                if ("USERNAME".equalsIgnoreCase(rawValue)) {
                    // Will be replaced by getFixedValue
                    return rawValue;
                }
                return rawValue;
                
            case CHAR:
            default:
                return rawValue;
        }
    }
    
    private Object getFixedValue(InterfaceLoadingChamp champ, String username) {
        if (champ.getValeur() == null || champ.getValeur().trim().isEmpty()) {
            return null;
        }
        
        String value = champ.getValeur().trim();
        
        if (champ.getTypeDonnee() == InterfaceLoadingChamp.TypeDonnee.EXTRA) {
            if ("USERNAME".equalsIgnoreCase(value)) {
                return username;
            }
        } else if (champ.getTypeDonnee() == InterfaceLoadingChamp.TypeDonnee.FUNCTION) {
            if ("now()".equalsIgnoreCase(value) || "current_timestamp".equalsIgnoreCase(value)) {
                return LocalDateTime.now();
            }
        }
        
        return convertValue(value, champ);
    }
    
    private void insertIntoTable(String tableName, Map<String, Object> rowData) {
        if (rowData.isEmpty()) {
            return;
        }
        
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(tableName);
        sql.append(" (");
        sql.append(String.join(", ", rowData.keySet()));
        sql.append(") VALUES (");
        sql.append(rowData.keySet().stream().map(k -> "?").collect(Collectors.joining(", ")));
        sql.append(")");
        
        List<Object> values = new ArrayList<>(rowData.values());
        jdbcTemplate.update(sql.toString(), values.toArray());
    }
}
