INSERT INTO processus_parametre (
    code_processus,
    description,
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
)
SELECT
    'PROCESS_ATTENDANCES',
    'Construction presences depuis pointage brut',
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
WHERE NOT EXISTS (
    SELECT 1 FROM processus_parametre WHERE code_processus = 'PROCESS_ATTENDANCES'
);
