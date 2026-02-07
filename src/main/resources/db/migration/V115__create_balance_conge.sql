CREATE TABLE balance_conge (
    id BIGSERIAL PRIMARY KEY,
    entreprise_id BIGINT REFERENCES entreprise(id),
    emploi_employe_id BIGINT REFERENCES emploi_employe(id),
    employe_id BIGINT NOT NULL REFERENCES employe(id),
    type_conge_id BIGINT NOT NULL REFERENCES type_conge(id),
    solde_actuel NUMERIC(10,2) NOT NULL DEFAULT 0,
    solde_disponible NUMERIC(10,2) NOT NULL DEFAULT 0,
    derniere_mise_a_jour DATE DEFAULT CURRENT_DATE,
    actif CHAR(1) NOT NULL DEFAULT 'Y' CHECK (actif IN ('Y', 'N')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    UNIQUE (emploi_employe_id, employe_id, type_conge_id)
);

CREATE TABLE balance_conge_annee (
    id BIGSERIAL PRIMARY KEY,
    balance_conge_id BIGINT NOT NULL REFERENCES balance_conge(id),
    emploi_employe_id BIGINT NOT NULL REFERENCES emploi_employe(id),
    type_conge_id BIGINT NOT NULL REFERENCES type_conge(id),
    annee INTEGER NOT NULL,
    jours_acquis NUMERIC(10,2) NOT NULL DEFAULT 0,
    jours_pris NUMERIC(10,2) NOT NULL DEFAULT 0,
    cumul_autorise CHAR(1) NOT NULL DEFAULT 'N',
    plafond_cumul NUMERIC(10,2),
    jours_reportes NUMERIC(10,2) NOT NULL DEFAULT 0,
    jours_expires NUMERIC(10,2) NOT NULL DEFAULT 0,
    solde_fin_annee NUMERIC(10,2) NOT NULL DEFAULT 0,
    date_cloture DATE,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    UNIQUE (emploi_employe_id, type_conge_id, annee)
);

CREATE INDEX idx_balance_conge_employe ON balance_conge(employe_id);
CREATE INDEX idx_balance_conge_type ON balance_conge(type_conge_id);
CREATE INDEX idx_balance_conge_annee_balance ON balance_conge_annee(balance_conge_id);
