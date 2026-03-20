ALTER TABLE payroll
    ADD COLUMN IF NOT EXISTS periode_paie INTEGER;

UPDATE payroll p
SET periode_paie = COALESCE(
        (SELECT rp.periode_paie_courante
         FROM regime_paie rp
         WHERE rp.id = p.regime_paie_id),
        1
    )
WHERE p.periode_paie IS NULL;

ALTER TABLE payroll
    ALTER COLUMN periode_paie SET NOT NULL;
