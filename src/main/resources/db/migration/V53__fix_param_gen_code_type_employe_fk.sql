-- Fix foreign key constraint for param_generation_code_employe.type_employe_id
-- The previous migration incorrectly referenced type_employe(type_employe_id)
-- It should reference type_employe(id)

-- Drop the incorrect foreign key constraint if it exists
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_param_gen_code_type_employe'
        AND table_name = 'param_generation_code_employe'
    ) THEN
        ALTER TABLE param_generation_code_employe 
        DROP CONSTRAINT fk_param_gen_code_type_employe;
    END IF;
END $$;

-- Recreate the foreign key constraint with the correct reference
ALTER TABLE param_generation_code_employe
ADD CONSTRAINT fk_param_gen_code_type_employe 
FOREIGN KEY (type_employe_id) REFERENCES type_employe(id) ON DELETE SET NULL;
