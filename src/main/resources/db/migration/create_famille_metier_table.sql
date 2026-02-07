-- Create famille_metier table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS famille_metier (
    id BIGSERIAL PRIMARY KEY,
    code_famille_metier VARCHAR(50) NOT NULL UNIQUE,
    libelle VARCHAR(255) NOT NULL,
    description TEXT,
    domaine_id BIGINT,
    niveau_qualification_id BIGINT,
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1,
    
    -- Foreign keys
    CONSTRAINT fk_famille_metier_domaine FOREIGN KEY (domaine_id) REFERENCES domaine(id),
    CONSTRAINT fk_famille_metier_niveau_qualification FOREIGN KEY (niveau_qualification_id) REFERENCES niveau_qualification(id)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_famille_metier_code ON famille_metier(code_famille_metier);
CREATE INDEX IF NOT EXISTS idx_famille_metier_libelle ON famille_metier(libelle);
CREATE INDEX IF NOT EXISTS idx_famille_metier_domaine ON famille_metier(domaine_id);
CREATE INDEX IF NOT EXISTS idx_famille_metier_niveau_qualification ON famille_metier(niveau_qualification_id);

-- Add comment to table
COMMENT ON TABLE famille_metier IS 'Table for storing job families with domain and qualification level references.';
