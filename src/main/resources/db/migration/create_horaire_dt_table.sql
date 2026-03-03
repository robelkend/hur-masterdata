-- Create horaire_dt table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS horaire_dt (
    id BIGSERIAL PRIMARY KEY,
    horaire_id BIGINT NOT NULL,
    jour INTEGER NOT NULL CHECK (jour BETWEEN 1 AND 7),
    heure_debut_jour CHAR(5),
    heure_fin_jour CHAR(5),
    heure_debut_nuit CHAR(5),
    heure_fin_nuit CHAR(5),
    heure_debut_pause CHAR(5),
    heure_fin_pause CHAR(5),
    exiger_presence CHAR(1) NOT NULL DEFAULT 'N' CHECK (exiger_presence IN ('Y', 'N')),
    heure_fermeture_auto CHAR(1) NOT NULL DEFAULT 'N' CHECK (heure_fermeture_auto IN ('Y', 'N')),
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1,
    
    -- Foreign key
    CONSTRAINT fk_horaire_dt_horaire FOREIGN KEY (horaire_id) REFERENCES horaire(id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicates
    CONSTRAINT uk_horaire_dt UNIQUE (horaire_id, jour)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_horaire_dt_horaire ON horaire_dt(horaire_id);
CREATE INDEX IF NOT EXISTS idx_horaire_dt_jour ON horaire_dt(jour);
