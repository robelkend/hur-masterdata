UPDATE ref_formule_token
SET symbole = '${t.bon.min}',
    libelle = 'Minutes minimum presence pour bonus'
WHERE code_element = 'VAR_TYPE_BONUS_MINUTES';

UPDATE ref_formule_token
SET symbole = '${t.bon.pct}',
    libelle = 'Pourcentage journalier bonus type employe'
WHERE code_element = 'VAR_TYPE_BONUS_PCT_DAY';

UPDATE ref_formule_token
SET symbole = '${t.sup.apr}',
    libelle = 'Seuil heures supplementaire type employe'
WHERE code_element = 'VAR_TYPE_SUPP_AFTER_HOURS';
