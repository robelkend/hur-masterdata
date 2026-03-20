CREATE TABLE IF NOT EXISTS payroll_periode_boni (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    libelle VARCHAR(100) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    statut VARCHAR(30) NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT ck_payroll_periode_boni_dates CHECK (date_fin >= date_debut),
    CONSTRAINT ck_payroll_periode_boni_statut CHECK (statut IN ('ACTIF', 'INACTIF'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_payroll_periode_boni_single_actif
    ON payroll_periode_boni ((1))
    WHERE statut = 'ACTIF';
