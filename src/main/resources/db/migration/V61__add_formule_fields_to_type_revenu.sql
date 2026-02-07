-- Add formule_id, ajouter_au_boni, ajouter_au_preavis to type_revenu table
ALTER TABLE type_revenu
    ADD COLUMN IF NOT EXISTS formule_id BIGINT,
    ADD COLUMN IF NOT EXISTS ajouter_au_boni CHAR(1) DEFAULT 'Y' NOT NULL,
    ADD COLUMN IF NOT EXISTS ajouter_au_preavis CHAR(1) DEFAULT 'Y' NOT NULL;

-- Add foreign key constraint for formule_id
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_type_revenu_formule'
    ) THEN
        ALTER TABLE type_revenu
            ADD CONSTRAINT fk_type_revenu_formule
            FOREIGN KEY (formule_id) REFERENCES formule(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Add CHECK constraints
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_type_revenu_ajouter_au_boni'
    ) THEN
        ALTER TABLE type_revenu
            ADD CONSTRAINT chk_type_revenu_ajouter_au_boni
            CHECK (ajouter_au_boni IN ('Y', 'N'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_type_revenu_ajouter_au_preavis'
    ) THEN
        ALTER TABLE type_revenu
            ADD CONSTRAINT chk_type_revenu_ajouter_au_preavis
            CHECK (ajouter_au_preavis IN ('Y', 'N'));
    END IF;
END $$;

-- Create index on formule_id for better query performance
CREATE INDEX IF NOT EXISTS idx_type_revenu_formule ON type_revenu(formule_id);
