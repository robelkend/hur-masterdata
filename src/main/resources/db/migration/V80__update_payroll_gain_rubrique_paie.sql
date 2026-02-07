ALTER TABLE payroll_gain
    ADD COLUMN IF NOT EXISTS rubrique_paie_id BIGINT;

UPDATE payroll_gain pg
SET rubrique_paie_id = rp.id
FROM rubrique_paie rp
WHERE pg.rubrique_paie_id IS NULL
  AND (rp.hardcoded = pg.categorie OR rp.code_rubrique = pg.categorie);

UPDATE payroll_gain pg
SET rubrique_paie_id = (
    SELECT id FROM rubrique_paie
    WHERE hardcoded = 'AUTRE_REVENU'
    ORDER BY id
    LIMIT 1
)
WHERE pg.rubrique_paie_id IS NULL;

ALTER TABLE payroll_gain
    ALTER COLUMN rubrique_paie_id SET NOT NULL;

ALTER TABLE payroll_gain
    DROP COLUMN IF EXISTS categorie;

ALTER TABLE payroll_gain
    ADD CONSTRAINT fk_payroll_gain_rubrique_paie
        FOREIGN KEY (rubrique_paie_id) REFERENCES rubrique_paie(id);
