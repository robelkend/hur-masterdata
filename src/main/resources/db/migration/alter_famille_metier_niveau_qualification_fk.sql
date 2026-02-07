-- Alter famille_metier table to change foreign key from niveau_qualification to niveau_employe
-- This migration fixes the reference for niveau_qualification_id to point to niveau_employe table

-- Drop the old foreign key constraint if it exists
ALTER TABLE famille_metier 
DROP CONSTRAINT IF EXISTS fk_famille_metier_niveau_qualification;

-- Add the new foreign key constraint pointing to niveau_employe
ALTER TABLE famille_metier 
ADD CONSTRAINT fk_famille_metier_niveau_qualification 
FOREIGN KEY (niveau_qualification_id) REFERENCES niveau_employe(id);

-- Update the index comment if needed
COMMENT ON COLUMN famille_metier.niveau_qualification_id IS 'Foreign key reference to niveau_employe table';
