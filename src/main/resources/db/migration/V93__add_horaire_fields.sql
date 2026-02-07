ALTER TABLE horaire
    ADD COLUMN tolerance_retard_min INTEGER NOT NULL DEFAULT 5,
    ADD COLUMN seuil_doublon_min INTEGER NOT NULL DEFAULT 2,
    ADD COLUMN max_session_heures INTEGER NOT NULL DEFAULT 16;
