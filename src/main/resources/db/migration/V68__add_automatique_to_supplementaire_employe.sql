ALTER TABLE supplementaire_employe
    ADD COLUMN IF NOT EXISTS automatique CHAR(1) NOT NULL DEFAULT 'N';

UPDATE supplementaire_employe
SET automatique = 'N'
WHERE automatique IS NULL;

ALTER TABLE supplementaire_employe
    ADD CONSTRAINT chk_supplementaire_employe_automatique
    CHECK (automatique IN ('Y', 'N'));
