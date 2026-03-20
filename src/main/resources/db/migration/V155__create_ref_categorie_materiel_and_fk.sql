CREATE TABLE IF NOT EXISTS ref_categorie_materiel (
    code_categorie VARCHAR(50) PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1
);

INSERT INTO ref_categorie_materiel (code_categorie, libelle, created_by, rowscn)
VALUES
    ('VEHICULE', 'Vehicule', 'system', 1),
    ('INFORMATIQUE', 'Informatique', 'system', 1),
    ('TELECOMMUNICATION', 'Telecommunication', 'system', 1),
    ('OUTILLAGE', 'Outillage', 'system', 1),
    ('SECURITE', 'Securite', 'system', 1),
    ('UNIFORME', 'Uniforme', 'system', 1),
    ('ACCES', 'Acces', 'system', 1),
    ('AUTRE', 'Autre', 'system', 1)
ON CONFLICT (code_categorie) DO NOTHING;

UPDATE ref_materiel
   SET categorie = 'AUTRE'
 WHERE categorie IS NULL
    OR categorie NOT IN (SELECT code_categorie FROM ref_categorie_materiel);

ALTER TABLE ref_materiel
    ALTER COLUMN categorie TYPE VARCHAR(50);

ALTER TABLE ref_materiel
    ALTER COLUMN categorie SET DEFAULT 'AUTRE';

ALTER TABLE ref_materiel
    ALTER COLUMN categorie SET NOT NULL;

ALTER TABLE ref_materiel
    DROP CONSTRAINT IF EXISTS ck_ref_materiel_categorie;

ALTER TABLE ref_materiel
    DROP CONSTRAINT IF EXISTS fk_ref_materiel_categorie;

ALTER TABLE ref_materiel
    ADD CONSTRAINT fk_ref_materiel_categorie
    FOREIGN KEY (categorie) REFERENCES ref_categorie_materiel(code_categorie);
