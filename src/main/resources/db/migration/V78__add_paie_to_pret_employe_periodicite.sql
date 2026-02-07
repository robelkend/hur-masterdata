-- Add PAIE to pret_employe.periodicite check constraint
DO $$
DECLARE
    constraint_name text;
BEGIN
    -- Drop known constraint name if it already exists (avoid duplicate name error)
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'pret_employe'::regclass
          AND contype = 'c'
          AND conname = 'pret_employe_periodicite_check'
    ) THEN
        EXECUTE 'ALTER TABLE pret_employe DROP CONSTRAINT pret_employe_periodicite_check';
    END IF;

    -- Drop any other periodicite check constraint (legacy name)
    SELECT conname INTO constraint_name
    FROM pg_constraint
    WHERE conrelid = 'pret_employe'::regclass
      AND contype = 'c'
      AND pg_get_constraintdef(oid) LIKE '%periodicite IN%'
    LIMIT 1;

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE pret_employe DROP CONSTRAINT %I', constraint_name);
    END IF;

    EXECUTE 'ALTER TABLE pret_employe ADD CONSTRAINT pret_employe_periodicite_check ' ||
            'CHECK (periodicite IN (''PAIE'', ''JOURNALIER'', ''HEBDO'', ''QUINZAINE'', ''QUINZOMADAIRE'', ''TRIMESTRIEL'', ''SEMESTRIEL'', ''ANNUEL''))';
END $$;
