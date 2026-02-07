ALTER TABLE pret_remboursement
    ADD COLUMN IF NOT EXISTS statut VARCHAR(20) NOT NULL DEFAULT 'BROUILLON';

ALTER TABLE pret_remboursement
    DROP CONSTRAINT IF EXISTS chk_pret_remboursement_statut;

ALTER TABLE pret_remboursement
    ADD CONSTRAINT chk_pret_remboursement_statut
        CHECK (statut IN ('BROUILLON', 'PAYE'));
