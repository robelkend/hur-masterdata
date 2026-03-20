ALTER TABLE payroll_employe_agregat
    ADD COLUMN IF NOT EXISTS nb_paie INTEGER;

UPDATE payroll_employe_agregat
SET nb_paie = 1
WHERE nb_paie IS NULL;

ALTER TABLE payroll_employe_agregat
    ALTER COLUMN nb_paie SET NOT NULL;
