ALTER TABLE ref_categorie_materiel
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);

ALTER TABLE ref_categorie_materiel
    ADD COLUMN IF NOT EXISTS created_on TIMESTAMP WITH TIME ZONE;

ALTER TABLE ref_categorie_materiel
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

ALTER TABLE ref_categorie_materiel
    ADD COLUMN IF NOT EXISTS updated_on TIMESTAMP WITH TIME ZONE;

ALTER TABLE ref_categorie_materiel
    ADD COLUMN IF NOT EXISTS rowscn INTEGER;

UPDATE ref_categorie_materiel
   SET created_by = COALESCE(created_by, 'system'),
       created_on = COALESCE(created_on, CURRENT_TIMESTAMP),
       rowscn = COALESCE(rowscn, 1);

ALTER TABLE ref_categorie_materiel
    ALTER COLUMN created_by SET NOT NULL;

ALTER TABLE ref_categorie_materiel
    ALTER COLUMN created_on SET NOT NULL;

ALTER TABLE ref_categorie_materiel
    ALTER COLUMN rowscn SET NOT NULL;

ALTER TABLE ref_categorie_materiel
    ALTER COLUMN created_on SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE ref_categorie_materiel
    ALTER COLUMN rowscn SET DEFAULT 1;
