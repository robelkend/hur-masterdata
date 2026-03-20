UPDATE conge_employe
SET statut = 'TERMINE'
WHERE statut = 'FINALISE';

ALTER TABLE conge_employe
DROP CONSTRAINT IF EXISTS conge_employe_statut_check;

ALTER TABLE conge_employe
ADD CONSTRAINT conge_employe_statut_check
CHECK (statut IN ('BROUILLON', 'SOUMIS', 'APPROUVE', 'EN_COURS', 'TERMINE', 'ANNULE', 'REJETE'));
