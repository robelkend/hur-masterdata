ALTER TABLE regime_paie
    ADD COLUMN IF NOT EXISTS nb_periode_paie INTEGER,
    ADD COLUMN IF NOT EXISTS periode_paie_courante INTEGER;

UPDATE regime_paie
SET nb_periode_paie = CASE periodicite
    WHEN 'MENSUEL' THEN 12
    WHEN 'QUINZAINE' THEN 26
    WHEN 'QUINZOMADAIRE' THEN 24
    WHEN 'HEBDO' THEN 52
    WHEN 'JOURNALIER' THEN 365
    WHEN 'TRIMESTRIEL' THEN 4
    WHEN 'SEMESTRIEL' THEN 2
    WHEN 'ANNUEL' THEN 1
    ELSE 12
END
WHERE nb_periode_paie IS NULL;

UPDATE regime_paie
SET periode_paie_courante = 1
WHERE periode_paie_courante IS NULL;

ALTER TABLE regime_paie
    ALTER COLUMN nb_periode_paie SET NOT NULL,
    ALTER COLUMN periode_paie_courante SET NOT NULL;
