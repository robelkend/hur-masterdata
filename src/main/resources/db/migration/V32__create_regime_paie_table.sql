-- Create regime_paie table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS regime_paie (
    id BIGSERIAL PRIMARY KEY,
    code_regime_paie VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    mode_remuneration VARCHAR(20) NOT NULL CHECK (mode_remuneration IN ('SALAIRE', 'HORAIRE', 'JOURNALIER', 'PIECE', 'PIECE_FIXE')),
    periodicite VARCHAR(20) NOT NULL CHECK (periodicite IN ('JOURNALIER', 'HEBDO', 'QUINZAINE', 'QUINZOMADAIRE', 'TRIMESTRIEL', 'SEMESTRIEL', 'ANNUEL')),
    devise_id BIGINT NOT NULL,
    horaire_actif CHAR(1) NOT NULL DEFAULT 'Y' CHECK (horaire_actif IN ('Y', 'N')),
    jours_payes INTEGER,
    supp_auto CHAR(1) NOT NULL DEFAULT 'N' CHECK (supp_auto IN ('Y', 'N')),
    bloquer_net_negatif CHAR(1) NOT NULL DEFAULT 'N' CHECK (bloquer_net_negatif IN ('Y', 'N')),
    taxe_chaque_n_paies INTEGER NOT NULL DEFAULT 0,
    supp_chaque_n_paies INTEGER NOT NULL DEFAULT 0,
    supp_decalage_nb_paies INTEGER NOT NULL DEFAULT 0,
    auto_traitement CHAR(1) NOT NULL DEFAULT 'N' CHECK (auto_traitement IN ('Y', 'N')),
    niveau_auto_traitement VARCHAR(20) NOT NULL DEFAULT 'AUCUN' CHECK (niveau_auto_traitement IN ('AUCUN', 'VALIDE', 'POSTE')),
    heures_min_jour CHAR(5),
    payer_si_moins_min CHAR(1) NOT NULL DEFAULT 'Y' CHECK (payer_si_moins_min IN ('Y', 'N')),
    retards_max_jour INTEGER NOT NULL DEFAULT 0,
    chk_template_index INTEGER,
    paiement_sur_compte CHAR(1) NOT NULL DEFAULT 'Y' CHECK (paiement_sur_compte IN ('Y', 'N')),
    taxe_sur_dernier_net_positif CHAR(1) NOT NULL DEFAULT 'Y' CHECK (taxe_sur_dernier_net_positif IN ('Y', 'N')),
    taxable CHAR(1) NOT NULL DEFAULT 'Y' CHECK (taxable IN ('Y', 'N')),
    responsable_id BIGINT,
    derniere_paie DATE,
    prochaine_paie DATE,
    dernier_prelevement DATE,
    dernier_supplement DATE,
    prochain_supplement DATE,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_regime_paie_devise FOREIGN KEY (devise_id) REFERENCES devise(id),
    CONSTRAINT fk_regime_paie_responsable FOREIGN KEY (responsable_id) REFERENCES employe(id)
);

CREATE INDEX IF NOT EXISTS idx_regime_paie_code ON regime_paie(code_regime_paie);
CREATE INDEX IF NOT EXISTS idx_regime_paie_devise ON regime_paie(devise_id);
CREATE INDEX IF NOT EXISTS idx_regime_paie_responsable ON regime_paie(responsable_id);
