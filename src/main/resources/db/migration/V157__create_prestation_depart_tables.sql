CREATE TABLE IF NOT EXISTS prestation_depart (
    id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL REFERENCES employe(id),
    regime_paie_id BIGINT NOT NULL REFERENCES regime_paie(id),
    mutation_employe_id BIGINT NOT NULL REFERENCES mutation_employe(id),
    type_depart VARCHAR(50) NOT NULL,
    date_depart DATE NOT NULL,
    date_calcul TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_gains NUMERIC(18,2) NOT NULL DEFAULT 0,
    total_deductions NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_net NUMERIC(18,2) NOT NULL DEFAULT 0,
    statut VARCHAR(30) NOT NULL DEFAULT 'CALCULE',
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_prestation_depart_employe ON prestation_depart(employe_id);
CREATE INDEX IF NOT EXISTS idx_prestation_depart_regime ON prestation_depart(regime_paie_id);
CREATE INDEX IF NOT EXISTS idx_prestation_depart_mutation ON prestation_depart(mutation_employe_id);
CREATE INDEX IF NOT EXISTS idx_prestation_depart_statut ON prestation_depart(statut);
CREATE INDEX IF NOT EXISTS idx_prestation_depart_type ON prestation_depart(type_depart);
CREATE INDEX IF NOT EXISTS idx_prestation_depart_date_depart ON prestation_depart(date_depart);

CREATE TABLE IF NOT EXISTS prestation_depart_detail (
    id BIGSERIAL PRIMARY KEY,
    prestation_depart_id BIGINT NOT NULL REFERENCES prestation_depart(id) ON DELETE CASCADE,
    rubrique_prestation VARCHAR(120) NOT NULL,
    libelle VARCHAR(255) NOT NULL,
    categorie VARCHAR(20) NOT NULL,
    montant_base NUMERIC(18,2) NOT NULL DEFAULT 0,
    taux NUMERIC(18,4) NOT NULL DEFAULT 0,
    montant NUMERIC(18,2) NOT NULL DEFAULT 0,
    ordre_affichage INTEGER NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_prestation_depart_detail_parent ON prestation_depart_detail(prestation_depart_id);
CREATE INDEX IF NOT EXISTS idx_prestation_depart_detail_rubrique ON prestation_depart_detail(rubrique_prestation);
CREATE INDEX IF NOT EXISTS idx_prestation_depart_detail_categorie ON prestation_depart_detail(categorie);

CREATE TABLE IF NOT EXISTS prestation_depart_deduction (
    id BIGSERIAL PRIMARY KEY,
    prestation_depart_id BIGINT NOT NULL REFERENCES prestation_depart(id) ON DELETE CASCADE,
    payroll_employe_id BIGINT NOT NULL REFERENCES payroll_employe(id),
    code_deduction VARCHAR(120) NOT NULL,
    libelle VARCHAR(255) NOT NULL,
    categorie VARCHAR(20) NOT NULL DEFAULT 'AUTRE',
    base_montant NUMERIC(18,2) NOT NULL DEFAULT 0,
    taux NUMERIC(18,4) DEFAULT 0,
    montant NUMERIC(18,2) NOT NULL DEFAULT 0,
    reference_externe VARCHAR(120),
    montant_couvert NUMERIC(18,2) NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_prestation_depart_deduction_parent ON prestation_depart_deduction(prestation_depart_id);
CREATE INDEX IF NOT EXISTS idx_prestation_depart_deduction_payroll_emp ON prestation_depart_deduction(payroll_employe_id);
CREATE INDEX IF NOT EXISTS idx_prestation_depart_deduction_code ON prestation_depart_deduction(code_deduction);
