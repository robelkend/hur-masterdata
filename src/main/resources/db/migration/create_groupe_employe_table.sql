-- Create groupe_employe table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS groupe_employe (
    id BIGSERIAL PRIMARY KEY,
    code_groupe VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    created_by TEXT NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by TEXT,
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_groupe_employe_code ON groupe_employe(code_groupe);
