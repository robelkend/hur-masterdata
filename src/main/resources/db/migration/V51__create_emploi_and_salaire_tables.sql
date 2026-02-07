-- Create emploi_employe table
CREATE TABLE IF NOT EXISTS emploi_employe (
    id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE,
    motif_fin TEXT,
    statut_emploi VARCHAR(20) CHECK (statut_emploi IN ('ACTIF', 'SUSPENDU', 'TERMINE')),
    date_fin_statut DATE,
    type_contrat VARCHAR(50) CHECK (type_contrat IN ('PERMANENT', 'TEMPORAIRE', 'STAGE', 'CONSULTANT')),
    temps_travail VARCHAR(20) CHECK (temps_travail IN ('TEMPS_PLEIN', 'TEMPS_PARTIEL')),
    type_employe_id BIGINT NOT NULL,
    unite_organisationnelle_id BIGINT NOT NULL,
    poste_id BIGINT,
    horaire_id BIGINT,
    taux_supplementaire NUMERIC(18,2) NOT NULL DEFAULT 0,
    fonction_id BIGINT,
    gestionnaire_id BIGINT,
    jour_off_1 INTEGER CHECK (jour_off_1 BETWEEN 1 AND 7),
    jour_off_2 INTEGER CHECK (jour_off_2 BETWEEN 1 AND 7),
    jour_off_3 INTEGER CHECK (jour_off_3 BETWEEN 1 AND 7),
    en_conge CHAR(1) NOT NULL DEFAULT 'N' CHECK (en_conge IN ('Y', 'N')),
    en_probation CHAR(1) NOT NULL DEFAULT 'N' CHECK (en_probation IN ('Y', 'N')),
    principal CHAR(1) NOT NULL DEFAULT 'N' CHECK (principal IN ('Y', 'N')),
    actif CHAR(1) NOT NULL DEFAULT 'N' CHECK (actif IN ('Y', 'N')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_emploi_employe FOREIGN KEY (employe_id) REFERENCES employe(id) ON DELETE CASCADE,
    CONSTRAINT fk_emploi_type_employe FOREIGN KEY (type_employe_id) REFERENCES type_employe(id),
    CONSTRAINT fk_emploi_unite_org FOREIGN KEY (unite_organisationnelle_id) REFERENCES unite_organisationnelle(id),
    CONSTRAINT fk_emploi_poste FOREIGN KEY (poste_id) REFERENCES poste(id),
    CONSTRAINT fk_emploi_horaire FOREIGN KEY (horaire_id) REFERENCES horaire(id),
    CONSTRAINT fk_emploi_fonction FOREIGN KEY (fonction_id) REFERENCES fonction(id),
    CONSTRAINT fk_emploi_gestionnaire FOREIGN KEY (gestionnaire_id) REFERENCES employe(id),
    CONSTRAINT chk_emploi_date_range CHECK (date_fin IS NULL OR date_debut IS NULL OR date_fin >= date_debut)
);

CREATE INDEX IF NOT EXISTS idx_emploi_employe ON emploi_employe(employe_id);
CREATE INDEX IF NOT EXISTS idx_emploi_type_employe ON emploi_employe(type_employe_id);
CREATE INDEX IF NOT EXISTS idx_emploi_unite_org ON emploi_employe(unite_organisationnelle_id);
CREATE INDEX IF NOT EXISTS idx_emploi_principal ON emploi_employe(employe_id, principal) WHERE principal = 'Y';
CREATE INDEX IF NOT EXISTS idx_emploi_actif ON emploi_employe(actif);

-- Create unique constraint: only one principal employment per employee
CREATE UNIQUE INDEX IF NOT EXISTS idx_emploi_principal_unique ON emploi_employe(employe_id, principal) WHERE principal = 'Y';

-- Create employe_salaire table
CREATE TABLE IF NOT EXISTS employe_salaire (
    id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL,
    emploi_id BIGINT NOT NULL,
    regime_paie_id BIGINT NOT NULL,
    montant NUMERIC(18,2) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE,
    principal CHAR(1) NOT NULL DEFAULT 'N' CHECK (principal IN ('Y', 'N')),
    actif CHAR(1) NOT NULL DEFAULT 'N' CHECK (actif IN ('Y', 'N')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_salaire_employe FOREIGN KEY (employe_id) REFERENCES employe(id) ON DELETE CASCADE,
    CONSTRAINT fk_salaire_emploi FOREIGN KEY (emploi_id) REFERENCES emploi_employe(id) ON DELETE CASCADE,
    CONSTRAINT fk_salaire_regime_paie FOREIGN KEY (regime_paie_id) REFERENCES regime_paie(id),
    CONSTRAINT chk_salaire_date_range CHECK (date_fin IS NULL OR date_debut IS NULL OR date_fin >= date_debut)
);

CREATE INDEX IF NOT EXISTS idx_salaire_employe ON employe_salaire(employe_id);
CREATE INDEX IF NOT EXISTS idx_salaire_emploi ON employe_salaire(emploi_id);
CREATE INDEX IF NOT EXISTS idx_salaire_regime_paie ON employe_salaire(regime_paie_id);
CREATE INDEX IF NOT EXISTS idx_salaire_principal ON employe_salaire(employe_id, principal) WHERE principal = 'Y';
CREATE INDEX IF NOT EXISTS idx_salaire_actif ON employe_salaire(actif);

-- Create unique constraint: only one principal salary per employee
CREATE UNIQUE INDEX IF NOT EXISTS idx_salaire_principal_unique ON employe_salaire(employe_id, principal) WHERE principal = 'Y';

-- Add comments
COMMENT ON TABLE emploi_employe IS 'Employee job positions - can have multiple but only one principal';
COMMENT ON COLUMN emploi_employe.principal IS 'Y if this is the principal employment (only one per employee)';
COMMENT ON COLUMN emploi_employe.actif IS 'Active status - activated during nomination, not editable on screen';
COMMENT ON COLUMN emploi_employe.jour_off_1 IS 'First day off (1=Monday, 7=Sunday)';
COMMENT ON COLUMN emploi_employe.jour_off_2 IS 'Second day off (1=Monday, 7=Sunday)';
COMMENT ON COLUMN emploi_employe.jour_off_3 IS 'Third day off (1=Monday, 7=Sunday)';

COMMENT ON TABLE employe_salaire IS 'Employee salaries - can have multiple but only one principal linked to principal employment';
COMMENT ON COLUMN employe_salaire.principal IS 'Y if this is the principal salary (only one per employee, must be linked to principal employment)';
COMMENT ON COLUMN employe_salaire.actif IS 'Active status - activated during nomination, not editable on screen';
