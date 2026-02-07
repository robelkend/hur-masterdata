-- Create type_sanction table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS type_sanction (
    id BIGSERIAL PRIMARY KEY,
    code_sanction VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    gravite VARCHAR(20) NOT NULL CHECK (gravite IN ('GRAVE', 'MOYEN', 'AUCUN')),
    categorie VARCHAR(20) NOT NULL CHECK (categorie IN ('SANCTION', 'BLAME', 'RETARD', 'ABSENCE')),
    created_by TEXT NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by TEXT,
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_type_sanction_code ON type_sanction(code_sanction);
CREATE INDEX IF NOT EXISTS idx_type_sanction_categorie ON type_sanction(categorie);
