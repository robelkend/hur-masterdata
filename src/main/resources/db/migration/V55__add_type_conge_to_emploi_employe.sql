-- Migration: Add type_conge_id column to emploi_employe table
-- Date: 2026-01-10

-- Add type_conge_id column to emploi_employe table
ALTER TABLE emploi_employe 
ADD COLUMN IF NOT EXISTS type_conge_id BIGINT;

-- Add foreign key constraint
ALTER TABLE emploi_employe
ADD CONSTRAINT fk_emploi_type_conge 
FOREIGN KEY (type_conge_id) REFERENCES type_conge(id);

-- Add index for better query performance
CREATE INDEX IF NOT EXISTS idx_emploi_type_conge ON emploi_employe(type_conge_id);

-- Add comment
COMMENT ON COLUMN emploi_employe.type_conge_id IS 'Type de congé associé à cet emploi';
