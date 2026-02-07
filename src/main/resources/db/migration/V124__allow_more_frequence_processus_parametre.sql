ALTER TABLE processus_parametre DROP CONSTRAINT IF EXISTS processus_parametre_frequence_check;
ALTER TABLE processus_parametre
    ADD CONSTRAINT processus_parametre_frequence_check
    CHECK (frequence IN ('MINUTE', 'HEURE', 'JOUR', 'SEMAINE', 'MOIS', 'ANNEE'));
