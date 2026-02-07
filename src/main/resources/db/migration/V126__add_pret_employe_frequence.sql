ALTER TABLE pret_employe
    ADD COLUMN IF NOT EXISTS frequence_nb_periodicites INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS frequence_compteur INTEGER NOT NULL DEFAULT 0;

UPDATE pret_employe
SET frequence_nb_periodicites = 1
WHERE frequence_nb_periodicites IS NULL;

UPDATE pret_employe
SET frequence_compteur = 0
WHERE frequence_compteur IS NULL;
