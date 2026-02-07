-- Rename table niveau_employe to niveau_qualification
ALTER TABLE IF EXISTS niveau_employe RENAME TO niveau_qualification;

-- Rename the foreign key constraint in famille_metier if it exists
ALTER TABLE famille_metier 
DROP CONSTRAINT IF EXISTS fk_famille_metier_niveau_qualification;

-- Re-add the foreign key constraint with the new table name
ALTER TABLE famille_metier 
ADD CONSTRAINT fk_famille_metier_niveau_qualification 
FOREIGN KEY (niveau_qualification_id) REFERENCES niveau_qualification(id);

-- Rename indexes
ALTER INDEX IF EXISTS idx_niveau_employe_code_niveau RENAME TO idx_niveau_qualification_code_niveau;
ALTER INDEX IF EXISTS idx_niveau_employe_description RENAME TO idx_niveau_qualification_description;
ALTER INDEX IF EXISTS idx_niveau_employe_niveau_hierarchique RENAME TO idx_niveau_qualification_niveau_hierarchique;

-- Update table comment
COMMENT ON TABLE niveau_qualification IS 'Table for storing qualification levels with hierarchical structure.';
