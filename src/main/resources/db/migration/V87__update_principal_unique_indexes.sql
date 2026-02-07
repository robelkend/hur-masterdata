-- Allow only one principal ACTIVE record per employe
DROP INDEX IF EXISTS idx_salaire_principal_unique;
CREATE UNIQUE INDEX IF NOT EXISTS idx_salaire_principal_unique
    ON employe_salaire(employe_id, principal)
    WHERE principal = 'Y' AND actif = 'Y';

DROP INDEX IF EXISTS idx_emploi_principal_unique;
CREATE UNIQUE INDEX IF NOT EXISTS idx_emploi_principal_unique
    ON emploi_employe(employe_id, principal)
    WHERE principal = 'Y' AND statut_emploi in ('ACTIF', 'SUSPENDU');
