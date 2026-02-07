-- Remove entreprise_id column and its foreign key constraint from entreprise table
ALTER TABLE entreprise DROP CONSTRAINT IF EXISTS fk_entreprise_entreprise;
ALTER TABLE entreprise DROP COLUMN IF EXISTS entreprise_id;
