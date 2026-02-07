ALTER TABLE processus_parametre
ADD COLUMN IF NOT EXISTS description TEXT;

ALTER TABLE processus_parametre
ADD COLUMN IF NOT EXISTS entreprise_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints tc
        WHERE tc.constraint_type = 'FOREIGN KEY'
          AND tc.table_name = 'processus_parametre'
          AND tc.constraint_name = 'fk_processus_parametre_entreprise'
    ) THEN
        ALTER TABLE processus_parametre
            ADD CONSTRAINT fk_processus_parametre_entreprise
            FOREIGN KEY (entreprise_id) REFERENCES entreprise(id);
    END IF;
END $$;
