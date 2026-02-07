-- Create ref_formule_token table
CREATE TABLE IF NOT EXISTS ref_formule_token (
    code_element VARCHAR(100) PRIMARY KEY,
    type_element VARCHAR(30) NOT NULL CHECK (type_element IN ('OPERATEUR', 'OPERANDE')),
    symbole VARCHAR(100) NOT NULL,
    libelle VARCHAR(255) NOT NULL,

    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1
);

-- Create formule table
CREATE TABLE IF NOT EXISTS formule (
    id BIGSERIAL PRIMARY KEY,
    code_variable VARCHAR(80) NOT NULL UNIQUE,
    valeur_defaut NUMERIC(15,2),
    actif CHAR(1) NOT NULL DEFAULT 'Y' CHECK (actif IN ('Y', 'N')),
    date_effectif DATE NOT NULL,
    date_fin DATE,
    description TEXT,

    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1
);

-- Create formule_element table
CREATE TABLE IF NOT EXISTS formule_element (
    id BIGSERIAL PRIMARY KEY,
    formule_id BIGINT NOT NULL,
    ordre INTEGER NOT NULL,
    token_code VARCHAR(100) NOT NULL,

    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,

    CONSTRAINT fk_formule_element_formule FOREIGN KEY (formule_id) REFERENCES formule(id) ON DELETE CASCADE,
    CONSTRAINT fk_formule_element_token FOREIGN KEY (token_code) REFERENCES ref_formule_token(code_element) ON DELETE RESTRICT,
    CONSTRAINT uk_formule_element_ordre UNIQUE (formule_id, ordre)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_ref_formule_token_type ON ref_formule_token(type_element);
CREATE INDEX IF NOT EXISTS idx_formule_code_variable ON formule(code_variable);
CREATE INDEX IF NOT EXISTS idx_formule_actif ON formule(actif);
CREATE INDEX IF NOT EXISTS idx_formule_date_effectif ON formule(date_effectif);
CREATE INDEX IF NOT EXISTS idx_formule_element_formule ON formule_element(formule_id);
CREATE INDEX IF NOT EXISTS idx_formule_element_token ON formule_element(token_code);
CREATE INDEX IF NOT EXISTS idx_formule_element_ordre ON formule_element(formule_id, ordre);

-- Add comments to tables
COMMENT ON TABLE ref_formule_token IS 'Tokens prédéfinis pour les formules de calcul (opérateurs et opérandes)';
COMMENT ON TABLE formule IS 'Formules de calcul avec code variable et dates d''effet';
COMMENT ON TABLE formule_element IS 'Éléments d''une formule (séquence ordonnée de tokens)';
