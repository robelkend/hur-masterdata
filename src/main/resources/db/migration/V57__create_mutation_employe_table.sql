-- Create mutation_employe table for managing employee mutations
CREATE TABLE IF NOT EXISTS mutation_employe (
    id BIGSERIAL PRIMARY KEY,
    
    -- Référence entreprise
    entreprise_id BIGINT REFERENCES entreprise(id),
    
    -- Référence métier
    employe_id BIGINT NOT NULL REFERENCES employe(id),
    
    -- Type de mutation
    type_mutation VARCHAR(50) NOT NULL CHECK (type_mutation IN (
        'CHG_POSTE', 'CHG_UNITE', 'PROMOTION', 'REVISION_SALAIRE',
        'DEMISSION', 'LICENCIEMENT', 'FIN_CONTRAT', 'ABANDON_POSTE',
        'SUSPENSION', 'REINTEGRATION'
    )),
    
    -- Dates
    date_effet DATE NOT NULL,
    date_saisie DATE NOT NULL DEFAULT CURRENT_DATE,
    
    -- Statut du processus
    statut VARCHAR(50) NOT NULL DEFAULT 'BROUILLON' CHECK (statut IN (
        'BROUILLON', 'SOUMIS', 'REJETE', 'APPROUVE', 'APPLIQUE', 'ANNULE'
    )),
    
    -- Informations complémentaires
    motif TEXT,
    reference VARCHAR(255),
    
    -- Snapshots métier (ne s'affichent pas à l'écran)
    avant JSONB NOT NULL DEFAULT '{}'::jsonb,
    apres JSONB NOT NULL DEFAULT '{}'::jsonb,
    
    -- Audit
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1,
    
    -- Foreign keys
    CONSTRAINT fk_mutation_employe_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id),
    CONSTRAINT fk_mutation_employe_employe FOREIGN KEY (employe_id) REFERENCES employe(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_mutation_employe_employe ON mutation_employe(employe_id);
CREATE INDEX IF NOT EXISTS idx_mutation_employe_statut ON mutation_employe(statut);
CREATE INDEX IF NOT EXISTS idx_mutation_employe_type ON mutation_employe(type_mutation);
CREATE INDEX IF NOT EXISTS idx_mutation_employe_date_effet ON mutation_employe(date_effet);

-- Add comments
COMMENT ON TABLE mutation_employe IS 'Table for storing employee mutations/movements';
COMMENT ON COLUMN mutation_employe.type_mutation IS 'Type of mutation: CHG_POSTE, CHG_UNITE, PROMOTION, REVISION_SALAIRE, DEMISSION, LICENCIEMENT, FIN_CONTRAT, ABANDON_POSTE, SUSPENSION, REINTEGRATION';
COMMENT ON COLUMN mutation_employe.statut IS 'Status: BROUILLON, SOUMIS, REJETE, APPROUVE, APPLIQUE, ANNULE';
COMMENT ON COLUMN mutation_employe.avant IS 'JSON snapshot of values BEFORE mutation (not displayed on screen)';
COMMENT ON COLUMN mutation_employe.apres IS 'JSON snapshot of values AFTER mutation (not displayed on screen)';
