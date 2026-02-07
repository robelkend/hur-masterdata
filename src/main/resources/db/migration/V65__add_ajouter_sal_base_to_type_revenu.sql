ALTER TABLE type_revenu
    ADD COLUMN IF NOT EXISTS ajouter_sal_base CHAR(1) DEFAULT 'N' NOT NULL;

ALTER TABLE type_revenu
    ADD CONSTRAINT chk_type_revenu_ajouter_sal_base
    CHECK (ajouter_sal_base IN ('Y', 'N'));
