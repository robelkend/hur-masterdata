ALTER TABLE payroll_employe_agregat
    ADD COLUMN IF NOT EXISTS no_periode INTEGER;

UPDATE payroll_employe_agregat pea
SET no_periode = COALESCE(
        (
            SELECT p.periode_paie
            FROM payroll p
            JOIN payroll_employe pe ON pe.payroll_id = p.id
            WHERE pe.employe_id = pea.employe_id
            ORDER BY p.date_fin DESC
            LIMIT 1
        ),
        1
    )
WHERE pea.no_periode IS NULL;

ALTER TABLE payroll_employe_agregat
    ALTER COLUMN no_periode SET NOT NULL;
