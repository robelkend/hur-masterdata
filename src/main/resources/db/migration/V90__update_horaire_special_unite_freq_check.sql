DO $$
DECLARE
    constraint_name text;
BEGIN
    -- Drop any unite_freq check constraint if it exists
    SELECT conname INTO constraint_name
    FROM pg_constraint
    WHERE conrelid = 'horaire_special'::regclass
      AND contype = 'c'
      AND pg_get_constraintdef(oid) LIKE '%unite_freq%';

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE horaire_special DROP CONSTRAINT %I', constraint_name);
    END IF;

    -- Allow unite_freq = 0 (used when frequence = AUCUN)
    EXECUTE 'ALTER TABLE horaire_special ADD CONSTRAINT horaire_special_unite_freq_check ' ||
            'CHECK (unite_freq >= 0)';
END $$;
