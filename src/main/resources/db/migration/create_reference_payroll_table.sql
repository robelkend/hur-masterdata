-- Create reference_payroll table
CREATE TABLE IF NOT EXISTS reference_payroll (
    id BIGSERIAL PRIMARY KEY,
    code_payroll VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1
);

-- Create index on code_payroll for faster lookups
CREATE INDEX IF NOT EXISTS idx_reference_payroll_code ON reference_payroll(code_payroll);

-- Add comment to table
COMMENT ON TABLE reference_payroll IS 'Table for storing payroll reference definitions';
