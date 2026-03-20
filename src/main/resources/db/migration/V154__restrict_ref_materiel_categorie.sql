UPDATE ref_materiel
   SET categorie = 'AUTRE'
 WHERE categorie IS NULL
    OR categorie NOT IN ('VEHICULE','INFORMATIQUE','TELECOMMUNICATION','OUTILLAGE','SECURITE','UNIFORME','ACCES','AUTRE');

ALTER TABLE ref_materiel
    ALTER COLUMN categorie SET DEFAULT 'AUTRE';

ALTER TABLE ref_materiel
    ALTER COLUMN categorie SET NOT NULL;

ALTER TABLE ref_materiel
    DROP CONSTRAINT IF EXISTS ck_ref_materiel_categorie;

ALTER TABLE ref_materiel
    ADD CONSTRAINT ck_ref_materiel_categorie
    CHECK (categorie IN ('VEHICULE','INFORMATIQUE','TELECOMMUNICATION','OUTILLAGE','SECURITE','UNIFORME','ACCES','AUTRE'));
