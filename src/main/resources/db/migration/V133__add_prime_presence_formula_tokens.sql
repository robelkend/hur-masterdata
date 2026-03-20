ALTER TABLE type_employe
    ADD COLUMN IF NOT EXISTS ajouter_bonus_apres_nb_minute_presence INTEGER;

ALTER TABLE type_employe
    ADD COLUMN IF NOT EXISTS pourcentage_jour_bonus NUMERIC(5,2);

ALTER TABLE type_employe
    ADD COLUMN IF NOT EXISTS calculer_supplementaire_apres INTEGER;

INSERT INTO ref_formule_token (code_element, type_element, symbole, libelle, created_by, rowscn)
SELECT 'VAR_WORK_HOURS_W', 'VARIABLE', '${h.work.w}', 'Heures travaillees semaine (details presence)'
     , 'system', 1
WHERE NOT EXISTS (SELECT 1 FROM ref_formule_token WHERE code_element = 'VAR_WORK_HOURS_W');

INSERT INTO ref_formule_token (code_element, type_element, symbole, libelle, created_by, rowscn)
SELECT 'VAR_FERIE_CONGE_HOURS_PP', 'VARIABLE', '${h.ferie.conge.pp}', 'Heures ferie/conge simulees sur la periode'
     , 'system', 1
WHERE NOT EXISTS (SELECT 1 FROM ref_formule_token WHERE code_element = 'VAR_FERIE_CONGE_HOURS_PP');

INSERT INTO ref_formule_token (code_element, type_element, symbole, libelle, created_by, rowscn)
SELECT 'VAR_GROSS_SALARY', 'VARIABLE', '${sal.brut}', 'Salaire brut employe'
     , 'system', 1
WHERE NOT EXISTS (SELECT 1 FROM ref_formule_token WHERE code_element = 'VAR_GROSS_SALARY');

INSERT INTO ref_formule_token (code_element, type_element, symbole, libelle, created_by, rowscn)
SELECT 'VAR_SUPPLEMENTAIRE_AMOUNT_PP', 'VARIABLE', '${amt.supp.pp}', 'Montant supplementaire valide sur la periode'
     , 'system', 1
WHERE NOT EXISTS (SELECT 1 FROM ref_formule_token WHERE code_element = 'VAR_SUPPLEMENTAIRE_AMOUNT_PP');

INSERT INTO ref_formule_token (code_element, type_element, symbole, libelle, created_by, rowscn)
SELECT 'VAR_HORAIRE_DAILY_HOURS', 'VARIABLE', '${h.plan.day}', 'Heures journalieres planifiees selon horaire'
     , 'system', 1
WHERE NOT EXISTS (SELECT 1 FROM ref_formule_token WHERE code_element = 'VAR_HORAIRE_DAILY_HOURS');

INSERT INTO ref_formule_token (code_element, type_element, symbole, libelle, created_by, rowscn)
SELECT 'VAR_TYPE_BONUS_MINUTES', 'VARIABLE', '${type.bonus.min.presence}', 'Minutes minimum presence pour bonus'
     , 'system', 1
WHERE NOT EXISTS (SELECT 1 FROM ref_formule_token WHERE code_element = 'VAR_TYPE_BONUS_MINUTES');

INSERT INTO ref_formule_token (code_element, type_element, symbole, libelle, created_by, rowscn)
SELECT 'VAR_TYPE_BONUS_PCT_DAY', 'VARIABLE', '${type.bonus.pct.jour}', 'Pourcentage journalier bonus type employe'
     , 'system', 1
WHERE NOT EXISTS (SELECT 1 FROM ref_formule_token WHERE code_element = 'VAR_TYPE_BONUS_PCT_DAY');

INSERT INTO ref_formule_token (code_element, type_element, symbole, libelle, created_by, rowscn)
SELECT 'VAR_TYPE_SUPP_AFTER_HOURS', 'VARIABLE', '${type.supp.apres}', 'Seuil heures supplementaire type employe'
     , 'system', 1
WHERE NOT EXISTS (SELECT 1 FROM ref_formule_token WHERE code_element = 'VAR_TYPE_SUPP_AFTER_HOURS');
