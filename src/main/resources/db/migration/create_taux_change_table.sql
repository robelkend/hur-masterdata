-- Create taux_change table
CREATE TABLE IF NOT EXISTS taux_change (
    id BIGSERIAL PRIMARY KEY,
    date_taux DATE NOT NULL,
    taux NUMERIC(18, 6) NOT NULL CHECK (taux > 0),
    taux_payroll NUMERIC(18, 6) NOT NULL DEFAULT 0 CHECK (taux_payroll >= 0),
    code_devise VARCHAR(50) NOT NULL,
    code_institution VARCHAR(50),
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1,
    
    -- Foreign key constraints
    CONSTRAINT fk_taux_change_devise 
        FOREIGN KEY (code_devise) 
        REFERENCES devise(code_devise) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    
    CONSTRAINT fk_taux_change_institution 
        FOREIGN KEY (code_institution) 
        REFERENCES institution_tierse(code_institution) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    
    -- Unique constraint: one rate per devise/date/institution combination
    CONSTRAINT uk_taux_change_devise_date_institution 
        UNIQUE (code_devise, date_taux, code_institution)
);

-- Create indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_taux_change_code_devise ON taux_change(code_devise);
CREATE INDEX IF NOT EXISTS idx_taux_change_date_taux ON taux_change(date_taux DESC);
CREATE INDEX IF NOT EXISTS idx_taux_change_code_institution ON taux_change(code_institution);
CREATE INDEX IF NOT EXISTS idx_taux_change_devise_date ON taux_change(code_devise, date_taux DESC);

-- Add comments
COMMENT ON TABLE taux_change IS 'Table for storing exchange rates by devise, date, and institution';
COMMENT ON COLUMN taux_change.taux IS 'Exchange rate value (precision 18, scale 6)';
COMMENT ON COLUMN taux_change.taux_payroll IS 'Payroll exchange rate value (precision 18, scale 6)';
COMMENT ON COLUMN taux_change.code_institution IS 'Optional institution reference - nullable';
