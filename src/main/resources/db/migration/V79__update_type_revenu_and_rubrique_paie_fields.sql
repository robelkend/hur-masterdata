ALTER TABLE type_revenu
    DROP COLUMN IF EXISTS imposable,
    DROP COLUMN IF EXISTS soumis_cotisations,
    DROP COLUMN IF EXISTS ajouter_au_boni,
    DROP COLUMN IF EXISTS ajouter_au_preavis;

ALTER TABLE rubrique_paie
    ADD COLUMN IF NOT EXISTS preavis CHAR(1) NOT NULL DEFAULT 'N',
    ADD COLUMN IF NOT EXISTS taxes_speciaux CHAR(1) NOT NULL DEFAULT 'N',
    ADD COLUMN IF NOT EXISTS soumis_cotisations CHAR(1) NOT NULL DEFAULT 'N';
