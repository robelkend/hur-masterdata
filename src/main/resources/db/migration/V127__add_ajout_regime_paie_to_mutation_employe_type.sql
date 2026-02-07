DO $$
DECLARE
    constraint_name text;
BEGIN
    -- Drop named constraint if it already exists
    EXECUTE 'ALTER TABLE mutation_employe DROP CONSTRAINT IF EXISTS mutation_employe_type_mutation_check';

    -- Drop any existing type_mutation check constraint
    SELECT conname INTO constraint_name
    FROM pg_constraint
    WHERE conrelid = 'mutation_employe'::regclass
      AND contype = 'c'
      AND pg_get_constraintdef(oid) LIKE '%type_mutation IN%';

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE mutation_employe DROP CONSTRAINT %I', constraint_name);
    END IF;

    -- Add the new constraint with AJOUT_REGIME_PAIE
    EXECUTE 'ALTER TABLE mutation_employe ADD CONSTRAINT mutation_employe_type_mutation_check ' ||
            'CHECK (type_mutation IN (''CHG_POSTE'', ''CHG_UNITE'', ''PROMOTION'', ''REVISION_SALAIRE'', ''AJOUT_REGIME_PAIE'', ''NOMINATION'', ''DEMISSION'', ''LICENCIEMENT'', ''FIN_CONTRAT'', ''ABANDON_POSTE'', ''SUSPENSION'', ''REINTEGRATION''))';
END $$;
