ALTER TABLE processus_parametre
DROP CONSTRAINT IF EXISTS processus_parametre_statut_check;

ALTER TABLE processus_parametre
ADD CONSTRAINT processus_parametre_statut_check
CHECK (statut IN ('EN_EXECUTION', 'REUSSI', 'ERREUR', 'SUSPENDU', 'PRET'));
