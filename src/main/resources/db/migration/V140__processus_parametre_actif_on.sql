UPDATE processus_parametre
SET actif = 'O'
WHERE actif = 'Y';

ALTER TABLE processus_parametre
ALTER COLUMN actif SET DEFAULT 'O';

ALTER TABLE processus_parametre
DROP CONSTRAINT IF EXISTS processus_parametre_actif_check;

ALTER TABLE processus_parametre
ADD CONSTRAINT processus_parametre_actif_check
CHECK (actif IN ('O', 'N'));
