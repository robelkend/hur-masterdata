-- Create unite_organisationnelle table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS unite_organisationnelle (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    nom VARCHAR(255) NOT NULL,
    
    -- Foreign keys
    type_unite_organisationnelle_id BIGINT NOT NULL,
    unite_parente_id BIGINT,
    responsable_employe_id BIGINT,
    
    -- Contact fields
    email VARCHAR(255),
    telephone_1 VARCHAR(50),
    telephone_2 VARCHAR(50),
    extension_telephone VARCHAR(20),
    
    -- Status
    actif CHAR(1) NOT NULL DEFAULT 'Y' CHECK (actif IN ('Y', 'N')),
    
    -- Dates
    date_debut_effet DATE DEFAULT CURRENT_DATE,
    date_fin_effet DATE,
    
    -- Audit fields
    created_by TEXT NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by TEXT,
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1,
    
    -- Foreign key constraints
    CONSTRAINT fk_unite_type_unite_org FOREIGN KEY (type_unite_organisationnelle_id) 
        REFERENCES type_unite_organisationnelle(id),
    CONSTRAINT fk_unite_parente FOREIGN KEY (unite_parente_id) 
        REFERENCES unite_organisationnelle(id),
    CONSTRAINT fk_unite_responsable FOREIGN KEY (responsable_employe_id) 
        REFERENCES employe(id),
    
    -- Validation constraints
    CONSTRAINT chk_date_range CHECK (date_fin_effet IS NULL OR date_debut_effet IS NULL OR date_fin_effet >= date_debut_effet)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_unite_organisationnelle_code ON unite_organisationnelle(code);
CREATE INDEX IF NOT EXISTS idx_unite_organisationnelle_type ON unite_organisationnelle(type_unite_organisationnelle_id);
CREATE INDEX IF NOT EXISTS idx_unite_organisationnelle_parente ON unite_organisationnelle(unite_parente_id);
CREATE INDEX IF NOT EXISTS idx_unite_organisationnelle_responsable ON unite_organisationnelle(responsable_employe_id);
CREATE INDEX IF NOT EXISTS idx_unite_organisationnelle_actif ON unite_organisationnelle(actif);

-- Add comment to table
COMMENT ON TABLE unite_organisationnelle IS 'Table for storing organizational units with hierarchical structure.';
