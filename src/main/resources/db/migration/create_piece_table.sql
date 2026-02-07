-- Create piece table
CREATE TABLE IF NOT EXISTS piece (
    id BIGSERIAL PRIMARY KEY,
    code_piece VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    code_devise VARCHAR(50) NOT NULL,
    montant NUMERIC(15, 2) NOT NULL CHECK (montant > 0),
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1,
    
    -- Foreign key constraint to devise table
    CONSTRAINT fk_piece_devise 
        FOREIGN KEY (code_devise) 
        REFERENCES devise(code_devise) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE
);

-- Create index on code_piece for faster lookups
CREATE INDEX IF NOT EXISTS idx_piece_code_piece ON piece(code_piece);

-- Create index on code_devise for faster foreign key lookups
CREATE INDEX IF NOT EXISTS idx_piece_code_devise ON piece(code_devise);

-- Add comment to table
COMMENT ON TABLE piece IS 'Table for storing piece definitions with currency references';
