-- Create rubrique_prestation table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS rubrique_prestation (
    id BIGSERIAL PRIMARY KEY,
    code_prestation VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    prelevement VARCHAR(1) NOT NULL CHECK (prelevement IN ('N', 'Y')),
    created_by TEXT NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by TEXT,
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_rubrique_prestation_code ON rubrique_prestation(code_prestation);
