-- Create type_employe table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS type_employe (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    pause_debut VARCHAR(5),
    pause_fin VARCHAR(5),
    payer_absence CHAR(1) DEFAULT 'Y',
    payer_absence_motivee CHAR(1) DEFAULT 'Y',
    devise_id BIGINT,
    salaire_minimum NUMERIC(15,2) DEFAULT 0,
    salaire_maximum NUMERIC(15,2) DEFAULT 0,
    ajouter_bonus_apres_nb_minute_presence INTEGER,
    pourcentage_jour_bonus NUMERIC(5,2),
    generer_prestation CHAR(1) DEFAULT 'Y',
    base_calcul_boni INTEGER CHECK (base_calcul_boni >= 1 AND base_calcul_boni <= 12),
    supplementaire CHAR(1) NOT NULL DEFAULT 'Y',
    base_calcul_supplementaire VARCHAR(50) CHECK (base_calcul_supplementaire IN ('JOURNALIER', 'HEBDOMADAIRE', 'QUINZAINE', 'MENSUEL')),
    calculer_supplementaire_apres INTEGER,
    probation CHAR(1) DEFAULT 'Y',
    statut_management VARCHAR(50) DEFAULT 'NON_MANAGER' CHECK (statut_management IN ('MANAGER', 'NON_MANAGER')),
    famille_metier_id BIGINT,
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1,
    
    -- Foreign keys
    CONSTRAINT fk_type_employe_devise FOREIGN KEY (devise_id) REFERENCES devise(id),
    CONSTRAINT fk_type_employe_famille_metier FOREIGN KEY (famille_metier_id) REFERENCES famille_metier(id),
    
    -- Constraints
    CONSTRAINT chk_payer_absence CHECK (payer_absence IS NULL OR payer_absence IN ('Y', 'N')),
    CONSTRAINT chk_payer_absence_motivee CHECK (payer_absence_motivee IS NULL OR payer_absence_motivee IN ('Y', 'N')),
    CONSTRAINT chk_generer_prestation CHECK (generer_prestation IS NULL OR generer_prestation IN ('Y', 'N')),
    CONSTRAINT chk_supplementaire CHECK (supplementaire IN ('Y', 'N')),
    CONSTRAINT chk_probation CHECK (probation IS NULL OR probation IN ('Y', 'N')),
    CONSTRAINT chk_salaire_max_min CHECK (salaire_maximum IS NULL OR salaire_minimum IS NULL OR salaire_maximum >= salaire_minimum)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_type_employe_description ON type_employe(description);
CREATE INDEX IF NOT EXISTS idx_type_employe_devise ON type_employe(devise_id);
CREATE INDEX IF NOT EXISTS idx_type_employe_famille_metier ON type_employe(famille_metier_id);
CREATE INDEX IF NOT EXISTS idx_type_employe_statut_management ON type_employe(statut_management);

-- Add comment to table
COMMENT ON TABLE type_employe IS 'Table for storing employee types with comprehensive configuration.';
