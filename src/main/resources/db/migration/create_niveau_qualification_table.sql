-- Create niveau_qualification table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS niveau_qualification (
    id BIGSERIAL PRIMARY KEY,
    code_niveau VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    niveau_hierarchique INTEGER NOT NULL CHECK (niveau_hierarchique >= 0),
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_niveau_qualification_code_niveau ON niveau_qualification(code_niveau);
CREATE INDEX IF NOT EXISTS idx_niveau_qualification_description ON niveau_qualification(description);
CREATE INDEX IF NOT EXISTS idx_niveau_qualification_niveau_hierarchique ON niveau_qualification(niveau_hierarchique);

-- Add comment to table
COMMENT ON TABLE niveau_qualification IS 'Table for storing qualification levels with hierarchical structure.';
