INSERT INTO ref_formule_token (code_element, type_element, symbole, libelle, created_by, rowscn)
SELECT 'VAR_BONI_BASE_MONTHS', 'VARIABLE', '${n.bon.base}', 'Base calcul boni en mois (type employe.base_calcul_boni)'
     , 'system', 1
WHERE NOT EXISTS (SELECT 1 FROM ref_formule_token WHERE code_element = 'VAR_BONI_BASE_MONTHS');

INSERT INTO ref_formule_token (code_element, type_element, symbole, libelle, created_by, rowscn)
SELECT 'VAR_ANNUAL_SALARY_TARGET', 'VARIABLE', '${amt.sal.y}', 'Montant salaire annualise base sur base_calcul_boni'
     , 'system', 1
WHERE NOT EXISTS (SELECT 1 FROM ref_formule_token WHERE code_element = 'VAR_ANNUAL_SALARY_TARGET');

INSERT INTO ref_formule_token (code_element, type_element, symbole, libelle, created_by, rowscn)
SELECT 'VAR_ANNUAL_SALARY_REMAINING', 'VARIABLE', '${amt.sal.r}', 'Montant forfaitaire restant pour atteindre le salaire annualise'
     , 'system', 1
WHERE NOT EXISTS (SELECT 1 FROM ref_formule_token WHERE code_element = 'VAR_ANNUAL_SALARY_REMAINING');
