-- Add MENSUEL to regime_paie periodicite check constraint
DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    SELECT con.conname INTO constraint_name
    FROM pg_constraint con
    JOIN pg_class rel ON rel.oid = con.conrelid
    WHERE rel.relname = 'regime_paie'
      AND con.contype = 'c'
      AND pg_get_constraintdef(con.oid) LIKE '%periodicite%';

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE regime_paie DROP CONSTRAINT %I', constraint_name);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        WHERE rel.relname = 'regime_paie'
          AND con.contype = 'c'
          AND con.conname = 'regime_paie_periodicite_check'
    ) THEN
        EXECUTE 'ALTER TABLE regime_paie ADD CONSTRAINT regime_paie_periodicite_check ' ||
                'CHECK (periodicite IN (''JOURNALIER'', ''HEBDO'', ''QUINZAINE'', ''QUINZOMADAIRE'', ''MENSUEL'', ''TRIMESTRIEL'', ''SEMESTRIEL'', ''ANNUEL''))';
    END IF;
END $$;
