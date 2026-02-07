DO $$
DECLARE
    constraint_name text;
BEGIN
    -- Drop the specific constraint if it exists
    SELECT conname INTO constraint_name
    FROM pg_constraint
    WHERE conrelid = 'horaire_special'::regclass
      AND conname = 'horaire_special_frequence_check';

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE horaire_special DROP CONSTRAINT %I', constraint_name);
    END IF;

    -- Drop any other frequence check constraint if it exists
    SELECT conname INTO constraint_name
    FROM pg_constraint
    WHERE conrelid = 'horaire_special'::regclass
      AND contype = 'c'
      AND pg_get_constraintdef(oid) LIKE '%frequence IN%'
      AND conname <> 'horaire_special_frequence_check';

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE horaire_special DROP CONSTRAINT %I', constraint_name);
    END IF;

    -- Add the new constraint with AUCUN
    EXECUTE 'ALTER TABLE horaire_special ADD CONSTRAINT horaire_special_frequence_check ' ||
            'CHECK (frequence IN (''AUCUN'', ''JOUR'', ''SEMAINE'', ''QUINZAINE'', ''MOIS''))';
END $$;
