-- Create processus_parametre table
CREATE TABLE IF NOT EXISTS processus_parametre (
    id BIGSERIAL PRIMARY KEY,
    code_processus VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    actif CHAR(1) NOT NULL DEFAULT 'N' CHECK (actif IN ('Y', 'N')),
    derniere_execution_at TIMESTAMP WITH TIME ZONE,
    prochaine_execution_at TIMESTAMP WITH TIME ZONE,
    frequence VARCHAR(20) NOT NULL DEFAULT 'JOUR' CHECK (frequence IN ('MINUTE', 'HEURE', 'JOUR')),
    nombre INTEGER NOT NULL DEFAULT 1,
    marge INTEGER NOT NULL DEFAULT 0,
    unite_marge VARCHAR(20) NOT NULL DEFAULT 'JOUR' CHECK (unite_marge IN ('MINUTE', 'HEURE', 'JOUR')),
    statut VARCHAR(20) NOT NULL DEFAULT 'PRET' CHECK (statut IN ('PRET', 'REUSSI', 'EN_EXECUTION', 'SUSPENDU', 'ERREUR')),
    derniere_erreur TEXT,
    nb_echecs_consecutifs INTEGER NOT NULL DEFAULT 0,
    entreprise_id BIGINT,

    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,

    CONSTRAINT fk_processus_param_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_processus_param_code ON processus_parametre(code_processus);
CREATE INDEX IF NOT EXISTS idx_processus_param_actif ON processus_parametre(actif);
CREATE INDEX IF NOT EXISTS idx_processus_param_statut ON processus_parametre(statut);
CREATE INDEX IF NOT EXISTS idx_processus_param_entreprise ON processus_parametre(entreprise_id);
CREATE INDEX IF NOT EXISTS idx_processus_param_prochaine_exec ON processus_parametre(prochaine_execution_at);
