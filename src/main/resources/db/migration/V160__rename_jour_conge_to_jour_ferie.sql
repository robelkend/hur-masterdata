ALTER TABLE IF EXISTS jour_conge
    RENAME TO jour_ferie;

ALTER INDEX IF EXISTS idx_jour_conge_date_conge
    RENAME TO idx_jour_ferie_date_conge;

ALTER INDEX IF EXISTS idx_jour_conge_type
    RENAME TO idx_jour_ferie_type;

ALTER SEQUENCE IF EXISTS jour_conge_id_seq
    RENAME TO jour_ferie_id_seq;

COMMENT ON TABLE jour_ferie IS 'Table for storing holiday/leave day definitions';
