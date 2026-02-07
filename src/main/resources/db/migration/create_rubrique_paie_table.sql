-- Create rubrique_paie table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS rubrique_paie (
    id BIGSERIAL PRIMARY KEY,
    code_rubrique VARCHAR(50) NOT NULL UNIQUE,
    libelle VARCHAR(255) NOT NULL,
    type_rubrique VARCHAR(20) NOT NULL CHECK (type_rubrique IN ('GAIN', 'RETENUE')),
    mode_calcul VARCHAR(20) NOT NULL CHECK (mode_calcul IN ('FIXE', 'HORAIRE', 'POURCENTAGE')),
    boni CHAR(1) NOT NULL DEFAULT 'Y' CHECK (boni IN ('Y', 'N')),
    prestation CHAR(1) NOT NULL DEFAULT 'Y' CHECK (prestation IN ('Y', 'N')),
    imposable CHAR(1) NOT NULL DEFAULT 'Y' CHECK (imposable IN ('Y', 'N')),
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1
);

-- Create index on code_rubrique for faster lookups
CREATE INDEX IF NOT EXISTS idx_rubrique_paie_code_rubrique ON rubrique_paie(code_rubrique);
