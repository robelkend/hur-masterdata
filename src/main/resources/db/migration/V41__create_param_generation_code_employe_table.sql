-- Create param_generation_code_employe table
CREATE TABLE IF NOT EXISTS param_generation_code_employe (
    id BIGSERIAL PRIMARY KEY,
    entreprise_id BIGINT,
    type_employe_id BIGINT,
    date_effectif DATE NOT NULL,
    date_fin DATE,
    mode_generation VARCHAR(20) NOT NULL CHECK (mode_generation IN ('SEQUENCE', 'PATTERN')),
    valeur_depart INTEGER,
    valeur_courante INTEGER,
    pas_incrementation INTEGER NOT NULL DEFAULT 1,
    longueur_min INTEGER,
    padding_char CHAR(1) NOT NULL DEFAULT '0',
    prefixe_fixe VARCHAR(50),
    suffixe_fixe VARCHAR(50),
    pattern TEXT,
    majuscules CHAR(1) NOT NULL DEFAULT 'Y' CHECK (majuscules IN ('Y', 'N')),
    enlever_accents CHAR(1) NOT NULL DEFAULT 'Y' CHECK (enlever_accents IN ('Y', 'N')),
    options JSONB NOT NULL DEFAULT '{}'::jsonb,
    actif CHAR(1) NOT NULL DEFAULT 'Y' CHECK (actif IN ('Y', 'N')),

    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,

    CONSTRAINT fk_param_gen_code_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id) ON DELETE SET NULL,
    CONSTRAINT fk_param_gen_code_type_employe FOREIGN KEY (type_employe_id) REFERENCES type_employe(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_param_gen_code_entreprise ON param_generation_code_employe(entreprise_id);
CREATE INDEX IF NOT EXISTS idx_param_gen_code_type_employe ON param_generation_code_employe(type_employe_id);
CREATE INDEX IF NOT EXISTS idx_param_gen_code_dates ON param_generation_code_employe(date_effectif, date_fin);
CREATE INDEX IF NOT EXISTS idx_param_gen_code_actif ON param_generation_code_employe(actif);
