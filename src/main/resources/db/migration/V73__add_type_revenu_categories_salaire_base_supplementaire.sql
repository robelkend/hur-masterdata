-- Add SALAIRE_BASE and SUPPLEMENTAIRE to type_revenu categorie check constraint
DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    SELECT con.conname INTO constraint_name
    FROM pg_constraint con
    JOIN pg_class rel ON rel.oid = con.conrelid
    WHERE rel.relname = 'type_revenu'
      AND con.contype = 'c'
      AND pg_get_constraintdef(con.oid) LIKE '%categorie%';

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE type_revenu DROP CONSTRAINT %I', constraint_name);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        WHERE rel.relname = 'type_revenu'
          AND con.contype = 'c'
          AND con.conname = 'chk_type_revenu_categorie'
    ) THEN
        EXECUTE 'ALTER TABLE type_revenu ADD CONSTRAINT chk_type_revenu_categorie ' ||
                'CHECK (categorie IN (''SALAIRE_BASE'', ''SUPPLEMENTAIRE'', ''PRIME'', ''PRIME-PONCTUALITE'', ''PRIME-PRESENCE'', ''PRIME-REGULARITE'', ''FRAIS'', ''INDEMNITE'', ''COMMISSION'', ''RELIQUAT'', ''AUTRE''))';
    END IF;
END $$;
