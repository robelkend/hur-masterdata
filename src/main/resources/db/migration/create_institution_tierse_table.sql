-- Create institution_tierse table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS institution_tierse (
    id BIGSERIAL PRIMARY KEY,
    code_institution VARCHAR(50) NOT NULL UNIQUE,
    nom VARCHAR(255) NOT NULL,
    created_by TEXT NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by TEXT,
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_institution_tierse_code ON institution_tierse(code_institution);
