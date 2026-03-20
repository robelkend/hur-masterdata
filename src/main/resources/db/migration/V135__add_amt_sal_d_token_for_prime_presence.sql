INSERT INTO ref_formule_token (code_element, type_element, symbole, libelle, created_by, rowscn)
SELECT 'VAR_DAILY_SALARY', 'VARIABLE', '${amt.sal.d}', 'Salaire journalier calcule selon regime de paie'
     , 'system', 1
WHERE NOT EXISTS (SELECT 1 FROM ref_formule_token WHERE code_element = 'VAR_DAILY_SALARY');
