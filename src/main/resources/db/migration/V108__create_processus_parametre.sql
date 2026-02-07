CREATE TABLE processus_parametre (
    id BIGSERIAL PRIMARY KEY,
    code_processus VARCHAR(120) NOT NULL UNIQUE,
    actif CHAR(1) NOT NULL DEFAULT 'Y' CHECK (actif IN ('Y', 'N')),
    derniere_execution_at TIMESTAMP WITH TIME ZONE,
    prochaine_execution_at TIMESTAMP WITH TIME ZONE,
    frequence VARCHAR(20) NOT NULL CHECK (frequence IN ('MINUTE', 'HEURE', 'JOUR')),
    nombre INTEGER NOT NULL DEFAULT 1,
    marge INTEGER DEFAULT 0,
    unite_marge VARCHAR(20) CHECK (unite_marge IN ('MINUTE', 'HEURE', 'JOUR')),
    statut VARCHAR(20) NOT NULL CHECK (statut IN ('EN_EXECUTION', 'REUSSI', 'ERREUR', 'SUSPENDU')),
    derniere_erreur TEXT,
    nb_echecs_consecutifs INTEGER NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1
);

INSERT INTO processus_parametre (
    code_processus,
    actif,
    prochaine_execution_at,
    frequence,
    nombre,
    marge,
    unite_marge,
    statut,
    nb_echecs_consecutifs,
    created_by,
    created_on,
    rowscn
) VALUES (
    'DB_CLOCKIN_OUT',
    'Y',
    CURRENT_TIMESTAMP,
    'HEURE',
    1,
    0,
    'JOUR',
    'REUSSI',
    0,
    'system',
    CURRENT_TIMESTAMP,
    1
);
