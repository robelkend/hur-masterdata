-- Create horaire table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS horaire (
    id BIGSERIAL PRIMARY KEY,
    code_horaire VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    generer_absence CHAR(1) NOT NULL DEFAULT 'Y' CHECK (generer_absence IN ('Y', 'N')),
    payer_supplementaire CHAR(1) NOT NULL DEFAULT 'Y' CHECK (payer_supplementaire IN ('Y', 'N')),
    montant_fixe CHAR(1) NOT NULL DEFAULT 'N' CHECK (montant_fixe IN ('Y', 'N')),
    montant_heure_sup NUMERIC(15,2) NOT NULL DEFAULT 0,
    coeff_jour_ferie NUMERIC(15,2) NOT NULL DEFAULT 0,
    nb_heures_ref NUMERIC(15,2) NOT NULL DEFAULT 0,
    coeff_dimanche NUMERIC(15,2) NOT NULL DEFAULT 0,
    coeff_supp_jour_ferie NUMERIC(15,2) NOT NULL DEFAULT 0,
    coeff_soir NUMERIC(15,2) NOT NULL DEFAULT 0,
    coeff_supp_soir NUMERIC(15,2) NOT NULL DEFAULT 0,
    coeff_supp_off NUMERIC(15,2) NOT NULL DEFAULT 0,
    devise_id BIGINT NOT NULL,
    alterner_jour_nuit CHAR(1) NOT NULL DEFAULT 'N' CHECK (alterner_jour_nuit IN ('Y', 'N')),
    unite_alternance VARCHAR(20) CHECK (unite_alternance IN ('JOUR', 'SEMAINE', 'MOIS')),
    nb_unite_jour INTEGER NOT NULL DEFAULT 0,
    heure_debut_nuit CHAR(5),
    heure_fin_nuit CHAR(5),
    heure_fermeture_auto_jour CHAR(5),
    heure_fermeture_auto_nuit CHAR(5),
    heure_debut CHAR(5),
    heure_fin CHAR(5),
    detail_present CHAR(1) NOT NULL DEFAULT 'Y' CHECK (detail_present IN ('Y', 'N')),
    shift_encours VARCHAR(20) CHECK (shift_encours IN ('jour', 'soir')),
    default_nb_hovertime INTEGER,
    debut_supplementaire CHAR(5),
    min_heure_ponctualite CHAR(5),
    nb_minute_ponctualite INTEGER,
    exiger_plan_nuit CHAR(1) NOT NULL DEFAULT 'Y' CHECK (exiger_plan_nuit IN ('Y', 'N')),
    planifier_nuit_auto CHAR(1) NOT NULL DEFAULT 'Y' CHECK (planifier_nuit_auto IN ('Y', 'N')),
    heure_fin_demi_journee CHAR(5),
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1,
    
    -- Foreign key
    CONSTRAINT fk_horaire_devise FOREIGN KEY (devise_id) REFERENCES devise(id)
);

-- Create index on code_horaire for faster lookups
CREATE INDEX IF NOT EXISTS idx_horaire_code_horaire ON horaire(code_horaire);
CREATE INDEX IF NOT EXISTS idx_horaire_devise ON horaire(devise_id);
