ALTER TABLE IF EXISTS payroll_periode_boni
    RENAME TO periode_paie;

ALTER INDEX IF EXISTS ux_payroll_periode_boni_single_actif
    RENAME TO ux_periode_paie_single_actif;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_payroll_periode_boni_dates'
    ) THEN
        ALTER TABLE periode_paie
            RENAME CONSTRAINT ck_payroll_periode_boni_dates TO ck_periode_paie_dates;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_payroll_periode_boni_statut'
    ) THEN
        ALTER TABLE periode_paie
            RENAME CONSTRAINT ck_payroll_periode_boni_statut TO ck_periode_paie_statut;
    END IF;
END $$;
