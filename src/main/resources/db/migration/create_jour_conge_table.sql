-- Create jour_ferie table
CREATE TABLE IF NOT EXISTS jour_ferie (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL CHECK (type IN ('FIXE', 'PAQUE', 'CARNAVAL', 'AUTRE')),
    date_conge DATE NOT NULL,
    description VARCHAR(255) NOT NULL,
    mi_journee VARCHAR(1) NOT NULL CHECK (mi_journee IN ('Y', 'N')),
    actif VARCHAR(1) NOT NULL CHECK (actif IN ('Y', 'N')),
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1
);

-- Create index on date_conge for faster lookups and sorting
CREATE INDEX IF NOT EXISTS idx_jour_ferie_date_conge ON jour_ferie(date_conge);

-- Create index on type for faster filtering
CREATE INDEX IF NOT EXISTS idx_jour_ferie_type ON jour_ferie(type);

-- Add comment to table
COMMENT ON TABLE jour_ferie IS 'Table for storing holiday/leave day definitions';
