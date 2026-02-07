-- Create domaine table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS domaine (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_domaine_nom ON domaine(nom);

-- Add comment to table
COMMENT ON TABLE domaine IS 'Table for storing domains.';
