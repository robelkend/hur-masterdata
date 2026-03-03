ALTER TABLE horaire_dt
    ADD COLUMN IF NOT EXISTS heure_debut_pause VARCHAR(5),
    ADD COLUMN IF NOT EXISTS heure_fin_pause VARCHAR(5);
