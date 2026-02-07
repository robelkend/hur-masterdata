-- Create payroll table
CREATE TABLE IF NOT EXISTS payroll (
    id BIGSERIAL PRIMARY KEY,
    regime_paie_id BIGINT NOT NULL,
    libelle VARCHAR(120),
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'BROUILLON' CHECK (statut IN ('BROUILLON', 'CALCULE', 'VALIDE', 'FINALISE')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_payroll_regime_paie FOREIGN KEY (regime_paie_id) REFERENCES regime_paie(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_payroll_regime ON payroll(regime_paie_id);
CREATE INDEX IF NOT EXISTS idx_payroll_statut ON payroll(statut);
CREATE INDEX IF NOT EXISTS idx_payroll_dates ON payroll(date_debut, date_fin);

-- Create payroll_employe table
CREATE TABLE IF NOT EXISTS payroll_employe (
    id BIGSERIAL PRIMARY KEY,
    payroll_id BIGINT NOT NULL,
    employe_id BIGINT NOT NULL,
    montant_salaire_base NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_supplementaire NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_autre_revenu NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_brut NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_deductions NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_recouvrements NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_sanctions NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant_net_a_payer NUMERIC(18,2) NOT NULL DEFAULT 0,
    mode_paiement VARCHAR(20) NOT NULL DEFAULT 'VIREMENT' CHECK (mode_paiement IN ('VIREMENT', 'CHEQUE', 'ESPECES')),
    no_cheque VARCHAR(50),
    libelle_banque VARCHAR(120),
    no_compte VARCHAR(120),
    type_compte VARCHAR(120),
    email_envoye CHAR(1) NOT NULL DEFAULT 'N' CHECK (email_envoye IN ('Y', 'N')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_payroll_employe_payroll FOREIGN KEY (payroll_id) REFERENCES payroll(id) ON DELETE CASCADE,
    CONSTRAINT fk_payroll_employe_employe FOREIGN KEY (employe_id) REFERENCES employe(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_payroll_employe_payroll ON payroll_employe(payroll_id);
CREATE INDEX IF NOT EXISTS idx_payroll_employe_employe ON payroll_employe(employe_id);

-- Create payroll_gain table
CREATE TABLE IF NOT EXISTS payroll_gain (
    id BIGSERIAL PRIMARY KEY,
    payroll_id BIGINT NOT NULL,
    payroll_employe_id BIGINT NOT NULL,
    categorie VARCHAR(50),
    montant NUMERIC(18,2) NOT NULL DEFAULT 0,
    imposable CHAR(1) NOT NULL DEFAULT 'Y' CHECK (imposable IN ('Y', 'N')),
    soumis_cotisations CHAR(1) NOT NULL DEFAULT 'Y' CHECK (soumis_cotisations IN ('Y', 'N')),
    source VARCHAR(20) NOT NULL DEFAULT 'SYSTEME' CHECK (source IN ('SYSTEME', 'MANUEL', 'IMPORT')),
    reference_externe VARCHAR(120),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_payroll_gain_payroll FOREIGN KEY (payroll_id) REFERENCES payroll(id) ON DELETE CASCADE,
    CONSTRAINT fk_payroll_gain_payroll_employe FOREIGN KEY (payroll_employe_id) REFERENCES payroll_employe(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_payroll_gain_payroll ON payroll_gain(payroll_id);

-- Create payroll_deduction table
CREATE TABLE IF NOT EXISTS payroll_deduction (
    id BIGSERIAL PRIMARY KEY,
    payroll_id BIGINT NOT NULL,
    payroll_employe_id BIGINT NOT NULL,
    code_deduction VARCHAR(120) NOT NULL,
    libelle VARCHAR(255) NOT NULL,
    categorie VARCHAR(20) NOT NULL DEFAULT 'AUTRE' CHECK (categorie IN ('TAXE', 'COTISATION', 'ASSURANCE', 'SAISIE', 'AUTRE')),
    base_montant NUMERIC(18,2) NOT NULL DEFAULT 0,
    taux NUMERIC(18,4) DEFAULT 0,
    montant NUMERIC(18,2) NOT NULL DEFAULT 0,
    reference_externe VARCHAR(120),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_payroll_deduction_payroll FOREIGN KEY (payroll_id) REFERENCES payroll(id) ON DELETE CASCADE,
    CONSTRAINT fk_payroll_deduction_payroll_employe FOREIGN KEY (payroll_employe_id) REFERENCES payroll_employe(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_payroll_deduction_payroll ON payroll_deduction(payroll_id);

-- Create payroll_sanction table
CREATE TABLE IF NOT EXISTS payroll_sanction (
    id BIGSERIAL PRIMARY KEY,
    payroll_id BIGINT NOT NULL,
    payroll_employe_id BIGINT NOT NULL,
    type_sanction VARCHAR(20) NOT NULL CHECK (type_sanction IN ('RETARD', 'ABSENCE', 'AUTRE')),
    date_jour DATE,
    quantite_minute NUMERIC(18,2),
    montant NUMERIC(18,2),
    reference_externe VARCHAR(120),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_payroll_sanction_payroll FOREIGN KEY (payroll_id) REFERENCES payroll(id) ON DELETE CASCADE,
    CONSTRAINT fk_payroll_sanction_payroll_employe FOREIGN KEY (payroll_employe_id) REFERENCES payroll_employe(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_payroll_sanction_payroll ON payroll_sanction(payroll_id);

-- Create payroll_employe_stats table
CREATE TABLE IF NOT EXISTS payroll_employe_stats (
    id BIGSERIAL PRIMARY KEY,
    payroll_id BIGINT NOT NULL,
    payroll_employe_id BIGINT NOT NULL,
    metric_code VARCHAR(80) NOT NULL,
    metric_label VARCHAR(120) NOT NULL,
    metric_group VARCHAR(20) NOT NULL DEFAULT 'AUTRE',
    unite_mesure VARCHAR(10) NOT NULL DEFAULT 'HEURE' CHECK (unite_mesure IN ('HEURE', 'JOUR')),
    quantite NUMERIC(18,2) NOT NULL DEFAULT 0,
    montant NUMERIC(18,2) NOT NULL DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_payroll_stats_payroll FOREIGN KEY (payroll_id) REFERENCES payroll(id) ON DELETE CASCADE,
    CONSTRAINT fk_payroll_stats_payroll_employe FOREIGN KEY (payroll_employe_id) REFERENCES payroll_employe(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_payroll_stats_payroll ON payroll_employe_stats(payroll_id);
