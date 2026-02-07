CREATE TABLE IF NOT EXISTS pointage_brut (
    id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NULL,
    entreprise_id BIGINT NULL,
    systeme_source VARCHAR(30) NOT NULL DEFAULT 'HORODATEUR',
    id_pointage_source VARCHAR(80),
    id_appareil VARCHAR(50),
    id_badge VARCHAR(50),
    date_heure_pointage TIMESTAMP WITH TIME ZONE NOT NULL,
    type_evenement VARCHAR(10) NOT NULL DEFAULT 'UNKNOWN',
    qualite_pointage VARCHAR(20) NOT NULL DEFAULT 'BRUT',
    motif_rejet VARCHAR(120),
    statut_traitement VARCHAR(20) NOT NULL DEFAULT 'BRUT',
    presence_employe_id BIGINT,
    traite_le TIMESTAMP WITH TIME ZONE,
    traite_par VARCHAR(100),
    importe_le TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    importe_par VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_pointage_employe FOREIGN KEY (employe_id) REFERENCES employe(id),
    CONSTRAINT fk_pointage_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id),
    CONSTRAINT fk_pointage_presence FOREIGN KEY (presence_employe_id) REFERENCES presence_employe(id),
    CONSTRAINT ck_pointage_type_evenement CHECK (type_evenement IN ('IN','OUT','UNKNOWN')),
    CONSTRAINT ck_pointage_qualite CHECK (qualite_pointage IN ('BRUT','OK','DUPLICAT','SUSPECT','REJETE')),
    CONSTRAINT ck_pointage_statut_traitement CHECK (statut_traitement IN ('BRUT','PRET','UTILISE','IGNORE','ERREUR'))
);

CREATE INDEX IF NOT EXISTS ix_pointage_brut_badge_date
    ON pointage_brut(id_badge, date_heure_pointage);

CREATE INDEX IF NOT EXISTS ix_pointage_brut_entreprise_date
    ON pointage_brut(entreprise_id, date_heure_pointage);
CREATE TABLE IF NOT EXISTS pointage_brut (
    id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL,
    entreprise_id BIGINT NULL,
    systeme_source VARCHAR(30) NOT NULL DEFAULT 'HORODATEUR',
    id_pointage_source VARCHAR(80),
    id_appareil VARCHAR(50),
    id_badge VARCHAR(50),
    date_heure_pointage TIMESTAMP WITH TIME ZONE NOT NULL,
    type_evenement VARCHAR(10) NOT NULL DEFAULT 'UNKNOWN',
    qualite_pointage VARCHAR(20) NOT NULL DEFAULT 'BRUT',
    motif_rejet VARCHAR(120),
    statut_traitement VARCHAR(20) NOT NULL DEFAULT 'BRUT',
    presence_employe_id BIGINT,
    traite_le TIMESTAMP WITH TIME ZONE,
    traite_par VARCHAR(100),
    importe_le TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    importe_par VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_pointage_employe FOREIGN KEY (employe_id) REFERENCES employe(id),
    CONSTRAINT fk_pointage_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id),
    CONSTRAINT fk_pointage_presence FOREIGN KEY (presence_employe_id) REFERENCES presence_employe(id),
    CONSTRAINT ck_pointage_type_evenement CHECK (type_evenement IN ('IN','OUT','UNKNOWN')),
    CONSTRAINT ck_pointage_qualite CHECK (qualite_pointage IN ('BRUT','OK','DUPLICAT','SUSPECT','REJETE')),
    CONSTRAINT ck_pointage_statut CHECK (statut_traitement IN ('BRUT','PRET','UTILISE','IGNORE','ERREUR'))
);

CREATE INDEX IF NOT EXISTS ix_pointage_brut_badge_date
    ON pointage_brut(id_badge, date_heure_pointage);

CREATE INDEX IF NOT EXISTS ix_pointage_brut_entreprise_date
    ON pointage_brut(entreprise_id, date_heure_pointage);

CREATE INDEX IF NOT EXISTS ix_pointage_brut_employe_date
    ON pointage_brut(employe_id, date_heure_pointage);
