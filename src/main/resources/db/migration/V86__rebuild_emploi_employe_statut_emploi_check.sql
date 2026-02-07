DO $$
DECLARE
    constraint_name text;
BEGIN
    -- Drop any existing statut_emploi check constraint
    SELECT conname INTO constraint_name
    FROM pg_constraint
    WHERE conrelid = 'emploi_employe'::regclass
      AND contype = 'c'
      AND pg_get_constraintdef(oid) LIKE '%statut_emploi IN%';

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE emploi_employe DROP CONSTRAINT %I', constraint_name);
    END IF;

    -- Add the new constraint with LICENCIE and ABANDONNE
    EXECUTE 'ALTER TABLE emploi_employe ADD CONSTRAINT emploi_employe_statut_emploi_check ' ||
            'CHECK (statut_emploi IN (''NOUVEAU'', ''ACTIF'', ''SUSPENDU'', ''TERMINE'', ''LICENCIE'', ''ABANDONNE''))';
END $$;
