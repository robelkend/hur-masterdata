ALTER TABLE type_revenu
    ADD COLUMN IF NOT EXISTS rubrique_paie_id BIGINT;

-- Set existing records to SALAIRE_BASE rubrique before NOT NULL
UPDATE type_revenu
SET rubrique_paie_id = (
    SELECT id FROM rubrique_paie
    WHERE code_rubrique = 'SALAIRE_BASE'
    ORDER BY id
    LIMIT 1
)
WHERE rubrique_paie_id IS NULL;

ALTER TABLE type_revenu
    ALTER COLUMN rubrique_paie_id SET NOT NULL;

ALTER TABLE type_revenu
    DROP CONSTRAINT IF EXISTS chk_type_revenu_categorie;

ALTER TABLE type_revenu
    DROP COLUMN IF EXISTS categorie;

ALTER TABLE type_revenu
    ADD CONSTRAINT fk_type_revenu_rubrique_paie
        FOREIGN KEY (rubrique_paie_id) REFERENCES rubrique_paie(id);
