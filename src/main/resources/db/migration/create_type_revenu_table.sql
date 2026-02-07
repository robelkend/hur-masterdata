-- Create type_revenu table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS type_revenu (
    id BIGSERIAL PRIMARY KEY,
    code_revenu VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    created_by TEXT NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by TEXT,
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_type_revenu_code ON type_revenu(code_revenu);
