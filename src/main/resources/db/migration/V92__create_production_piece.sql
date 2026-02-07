CREATE TABLE IF NOT EXISTS production_piece (
    id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL,
    entreprise_id BIGINT NULL,
    date_jour DATE NOT NULL,
    type_piece_id BIGINT NOT NULL,
    quantite NUMERIC(18,2) NOT NULL DEFAULT 0,
    quantite_rejet NUMERIC(18,2) NOT NULL DEFAULT 0,
    quantite_valide NUMERIC(18,2) NOT NULL DEFAULT 0,
    devise_id BIGINT NOT NULL,
    prix_unitaire NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_total NUMERIC(18,2) NOT NULL DEFAULT 0,
    payroll_id BIGINT,
    emploi_employe_id BIGINT,
    employe_salaire_id BIGINT,
    statut VARCHAR(20) NOT NULL DEFAULT 'BROUILLON',
    note TEXT,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_prod_employe FOREIGN KEY (employe_id) REFERENCES employe(id),
    CONSTRAINT fk_prod_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id),
    CONSTRAINT fk_prod_type_piece FOREIGN KEY (type_piece_id) REFERENCES type_piece(id),
    CONSTRAINT fk_prod_devise FOREIGN KEY (devise_id) REFERENCES devise(id),
    CONSTRAINT fk_prod_emploi_employe FOREIGN KEY (emploi_employe_id) REFERENCES emploi_employe(id),
    CONSTRAINT fk_prod_employe_salaire FOREIGN KEY (employe_salaire_id) REFERENCES employe_salaire(id),
    CONSTRAINT ck_prod_statut CHECK (statut IN ('BROUILLON','VALIDE','PAYE','ANNULE')),
    CONSTRAINT ck_prod_quantite CHECK (quantite >= 0 AND quantite_rejet >= 0),
    CONSTRAINT ck_prod_quantite_valide CHECK (quantite_valide >= 0),
    CONSTRAINT uq_prod UNIQUE (entreprise_id, employe_id, date_jour, type_piece_id)
);

CREATE INDEX IF NOT EXISTS idx_prod_employe ON production_piece(employe_id);
CREATE INDEX IF NOT EXISTS idx_prod_entreprise ON production_piece(entreprise_id);
CREATE INDEX IF NOT EXISTS idx_prod_date ON production_piece(date_jour);
