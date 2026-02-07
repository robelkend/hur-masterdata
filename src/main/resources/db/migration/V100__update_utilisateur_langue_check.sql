UPDATE utilisateur
SET langue = UPPER(langue)
WHERE langue IS NOT NULL;

ALTER TABLE utilisateur
    ALTER COLUMN langue SET DEFAULT 'FR';

ALTER TABLE utilisateur
    DROP CONSTRAINT IF EXISTS utilisateur_langue_check;

ALTER TABLE utilisateur
    ADD CONSTRAINT utilisateur_langue_check
    CHECK (langue IN ('FR', 'EN', 'CR', 'ES'));
