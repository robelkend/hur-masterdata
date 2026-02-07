-- Create bareme_sanction table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS bareme_sanction (
    id BIGSERIAL PRIMARY KEY,
    type_employe_id BIGINT NOT NULL,
    infraction_type VARCHAR(50) NOT NULL CHECK (infraction_type IN ('RETARD', 'ABSENCE')),
    unite_infraction VARCHAR(50) NOT NULL CHECK (unite_infraction IN ('MINUTE', 'HEURE', 'JOUR')),
    seuil_min INTEGER NOT NULL CHECK (seuil_min >= 0),
    seuil_max INTEGER CHECK (seuil_max IS NULL OR seuil_max >= seuil_min),
    penalite_minutes INTEGER NOT NULL CHECK (penalite_minutes >= 0),
    unite_penalite VARCHAR(50) NOT NULL CHECK (unite_penalite IN ('MINUTE', 'HEURE', 'JOUR')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_bareme_sanction_type_employe FOREIGN KEY (type_employe_id) REFERENCES type_employe(id)
);

CREATE INDEX IF NOT EXISTS idx_bareme_sanction_type_employe ON bareme_sanction(type_employe_id);
CREATE INDEX IF NOT EXISTS idx_bareme_sanction_infraction_type ON bareme_sanction(infraction_type);
