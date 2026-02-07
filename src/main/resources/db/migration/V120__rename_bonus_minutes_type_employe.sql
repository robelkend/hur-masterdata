ALTER TABLE type_employe DROP CONSTRAINT IF EXISTS chk_ajouter_bonus_apres;

ALTER TABLE type_employe
    RENAME COLUMN ajouter_bonus_apres TO ajouter_bonus_apres_nb_minute_presence;

ALTER TABLE type_employe
    ALTER COLUMN ajouter_bonus_apres_nb_minute_presence TYPE INTEGER
    USING CASE
        WHEN ajouter_bonus_apres_nb_minute_presence ~ '^[0-9]+$'
            THEN ajouter_bonus_apres_nb_minute_presence::INTEGER
        ELSE NULL
    END;

COMMENT ON COLUMN type_employe.ajouter_bonus_apres_nb_minute_presence IS
    'Nombre de minutes de presence pour appliquer un frais (bonus) a une periode.';
