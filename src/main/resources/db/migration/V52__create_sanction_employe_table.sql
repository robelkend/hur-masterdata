-- Migration pour créer la table sanction_employe
-- Cette table permet de gérer les sanctions appliquées aux employés

CREATE TABLE IF NOT EXISTS sanction_employe (
    id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL,
    date_sanction DATE,
    type_evenement VARCHAR(20) CHECK (type_evenement IN ('RETARD', 'ABSENCE', 'AUTRE')),
    valeur_mesuree NUMERIC(10, 2),
    unite_mesure VARCHAR(10) CHECK (unite_mesure IN ('MINUTE', 'HEURE', 'JOUR')),
    regle_id BIGINT,
    type_sanction VARCHAR(20) CHECK (type_sanction IN ('DEDUIRE_TEMPS', 'DEDUIRE_MONTANT', 'AVERTISSEMENT')),
    valeur_sanction NUMERIC(10, 2),
    unite_sanction VARCHAR(10) CHECK (unite_sanction IN ('MINUTE', 'HEURE', 'JOUR', 'MONTANT')),
    montant_calcule NUMERIC(10, 2),
    statut VARCHAR(20) NOT NULL DEFAULT 'BROUILLON' CHECK (statut IN ('BROUILLON', 'VALIDE')),
    motif TEXT,
    reference_externe VARCHAR(255),
    entreprise_id BIGINT,
    no_payroll INTEGER NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    
    CONSTRAINT fk_sanction_employe_employe FOREIGN KEY (employe_id) REFERENCES employe(id),
    CONSTRAINT fk_sanction_employe_regle FOREIGN KEY (regle_id) REFERENCES bareme_sanction(id),
    CONSTRAINT fk_sanction_employe_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id)
);

CREATE INDEX IF NOT EXISTS idx_sanction_employe_employe ON sanction_employe(employe_id);
CREATE INDEX IF NOT EXISTS idx_sanction_employe_date ON sanction_employe(date_sanction);
CREATE INDEX IF NOT EXISTS idx_sanction_employe_statut ON sanction_employe(statut);
CREATE INDEX IF NOT EXISTS idx_sanction_employe_type_evenement ON sanction_employe(type_evenement);
CREATE INDEX IF NOT EXISTS idx_sanction_employe_entreprise ON sanction_employe(entreprise_id);
CREATE INDEX IF NOT EXISTS idx_sanction_employe_regle ON sanction_employe(regle_id);
