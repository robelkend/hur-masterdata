-- Create type_unite_organisationnelle table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS type_unite_organisationnelle (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    libelle VARCHAR(255) NOT NULL,
    niveau_hierarchique INTEGER NOT NULL CHECK (niveau_hierarchique >= 0),
    created_by TEXT NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by TEXT,
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_type_unite_organisationnelle_code ON type_unite_organisationnelle(code);
CREATE INDEX IF NOT EXISTS idx_type_unite_organisationnelle_niveau ON type_unite_organisationnelle(niveau_hierarchique);
