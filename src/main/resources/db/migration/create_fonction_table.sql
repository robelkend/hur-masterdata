-- Create fonction table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS fonction (
    id BIGSERIAL PRIMARY KEY,
    code_fonction VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    created_by TEXT NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by TEXT,
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_fonction_code ON fonction(code_fonction);
