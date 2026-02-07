-- Create interface_loading table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS interface_loading (
    id BIGSERIAL PRIMARY KEY,
    code_loading VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    source VARCHAR(50) NOT NULL CHECK (source IN ('FILE', 'RDB', 'API')),
    exclus_derniere_ligne CHAR(1) NOT NULL DEFAULT 'N' CHECK (exclus_derniere_ligne IN ('Y', 'N')),
    separateur_champ VARCHAR(10),
    delimiteur_champ VARCHAR(10),
    exclus_lignes INTEGER NOT NULL DEFAULT 0,
    table_cible VARCHAR(100) NOT NULL,
    table_source VARCHAR(255),
    extra_clause TEXT,
    entreprise_id BIGINT,
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1,
    
    -- Foreign keys
    CONSTRAINT fk_interface_loading_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id)
);

-- Create interface_loading_champ table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS interface_loading_champ (
    id BIGSERIAL PRIMARY KEY,
    loading_id BIGINT NOT NULL,
    nom_cible VARCHAR(120) NOT NULL,
    nom_source VARCHAR(120),
    type_donnee VARCHAR(30) NOT NULL CHECK (type_donnee IN ('CHAR', 'DATE', 'DOUBLE', 'FUNCTION', 'EXTRA')),
    taille INTEGER,
    format VARCHAR(100),
    position INTEGER NOT NULL,
    valeur TEXT,
    update_champ VARCHAR(120),
    update_valeur TEXT,
    update_condition TEXT,
    obligatoire CHAR(1) NOT NULL DEFAULT 'N' CHECK (obligatoire IN ('Y', 'N')),
    
    -- Audit fields
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    
    -- Optimistic concurrency control
    rowscn INTEGER NOT NULL DEFAULT 1,
    
    -- Foreign keys
    CONSTRAINT fk_interface_loading_champ_loading FOREIGN KEY (loading_id) REFERENCES interface_loading(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_interface_loading_code ON interface_loading(code_loading);
CREATE INDEX IF NOT EXISTS idx_interface_loading_source ON interface_loading(source);
CREATE INDEX IF NOT EXISTS idx_interface_loading_table_cible ON interface_loading(table_cible);
CREATE INDEX IF NOT EXISTS idx_interface_loading_entreprise ON interface_loading(entreprise_id);
CREATE INDEX IF NOT EXISTS idx_interface_loading_champ_loading ON interface_loading_champ(loading_id);
CREATE INDEX IF NOT EXISTS idx_interface_loading_champ_position ON interface_loading_champ(position);

-- Add comments to tables
COMMENT ON TABLE interface_loading IS 'Configuration pour le chargement de données depuis différentes sources (fichier CSV, base de données, API)';
COMMENT ON TABLE interface_loading_champ IS 'Mapping des champs pour le chargement de données';
