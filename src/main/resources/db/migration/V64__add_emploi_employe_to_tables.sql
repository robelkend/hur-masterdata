CREATE TABLE IF NOT EXISTS sanction_employe (
    id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL,
    emploi_employe_id BIGINT,
    date_sanction DATE,
    type_evenement VARCHAR(20) CHECK (type_evenement IN ('RETARD', 'ABSENCE', 'AUTRE')),
    valeur_mesuree NUMERIC(10, 2),
    unite_mesure VARCHAR(10) CHECK (unite_mesure IN ('MINUTE', 'HEURE', 'JOUR')),
    regle_id BIGINT,
    type_sanction VARCHAR(20) CHECK (type_sanction IN ('DEDUIRE_TEMPS', 'DEDUIRE_MONTANT', 'AVERTISSEMENT')),
    valeur_sanction NUMERIC(10, 2),
    unite_sanction VARCHAR(10) CHECK (unite_sanction IN ('MINUTE', 'HEURE', 'JOUR', 'MONTANT')),
    montant_calcule NUMERIC(10, 2),
    statut VARCHAR(20) NOT NULL DEFAULT 'NOUVEAU' CHECK (statut IN ('NOUVEAU', 'VALIDE')),
    motif TEXT,
    reference_externe VARCHAR(255),
    entreprise_id BIGINT,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_sanction_employe_employe FOREIGN KEY (employe_id) REFERENCES employe(id),
    CONSTRAINT fk_sanction_employe_emploi_employe FOREIGN KEY (emploi_employe_id) REFERENCES emploi_employe(id),
    CONSTRAINT fk_sanction_employe_regle FOREIGN KEY (regle_id) REFERENCES bareme_sanction(id),
    CONSTRAINT fk_sanction_employe_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id)
);

ALTER TABLE sanction_employe
    ADD COLUMN IF NOT EXISTS emploi_employe_id BIGINT;

ALTER TABLE conge_employe
    ADD COLUMN IF NOT EXISTS emploi_employe_id BIGINT;

ALTER TABLE horaire_special
    ADD COLUMN IF NOT EXISTS emploi_employe_id BIGINT;

ALTER TABLE supplementaire_employe
    ADD COLUMN IF NOT EXISTS emploi_employe_id BIGINT;

UPDATE sanction_employe
SET statut = 'NOUVEAU'
WHERE statut = 'BROUILLON';

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'sanction_employe_statut_check'
    ) THEN
        ALTER TABLE sanction_employe DROP CONSTRAINT sanction_employe_statut_check;
    END IF;
END $$;

ALTER TABLE sanction_employe
    ALTER COLUMN statut SET DEFAULT 'NOUVEAU';

ALTER TABLE sanction_employe
    ADD CONSTRAINT chk_sanction_employe_statut
    CHECK (statut IN ('NOUVEAU', 'VALIDE'));

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_sanction_employe_emploi_employe'
    ) THEN
        ALTER TABLE sanction_employe
            ADD CONSTRAINT fk_sanction_employe_emploi_employe
            FOREIGN KEY (emploi_employe_id) REFERENCES emploi_employe(id);
    END IF;
END $$;

ALTER TABLE conge_employe
    ADD CONSTRAINT fk_conge_employe_emploi_employe
    FOREIGN KEY (emploi_employe_id) REFERENCES emploi_employe(id);

ALTER TABLE horaire_special
    ADD CONSTRAINT fk_horaire_special_emploi_employe
    FOREIGN KEY (emploi_employe_id) REFERENCES emploi_employe(id);

ALTER TABLE supplementaire_employe
    ADD CONSTRAINT fk_supplementaire_employe_emploi_employe
    FOREIGN KEY (emploi_employe_id) REFERENCES emploi_employe(id);

CREATE INDEX IF NOT EXISTS idx_sanction_employe_emploi_employe ON sanction_employe(emploi_employe_id);
CREATE INDEX IF NOT EXISTS idx_conge_employe_emploi_employe ON conge_employe(emploi_employe_id);
CREATE INDEX IF NOT EXISTS idx_horaire_special_emploi_employe ON horaire_special(emploi_employe_id);
CREATE INDEX IF NOT EXISTS idx_supplementaire_employe_emploi_employe ON supplementaire_employe(emploi_employe_id);
