-- Create type_conge table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS type_conge (
    id BIGSERIAL PRIMARY KEY,
    code_conge VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    conge_annuel VARCHAR(1) NOT NULL CHECK (conge_annuel IN ('N', 'Y')),
    nb_jours INTEGER,
    nb_annee_cumul INTEGER,
    created_by TEXT NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by TEXT,
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_type_conge_code ON type_conge(code_conge);
