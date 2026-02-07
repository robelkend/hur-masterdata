-- Create interface_extraction table
CREATE TABLE IF NOT EXISTS interface_extraction (
    id BIGSERIAL PRIMARY KEY,
    code_extraction VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    separateur VARCHAR(10),
    encadreur VARCHAR(10),
    actif CHAR(1) NOT NULL DEFAULT 'N' CHECK (actif IN ('Y', 'N')),
    entreprise_id BIGINT,

    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,

    CONSTRAINT fk_interface_extraction_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id)
);

-- Create interface_extraction_requete table
CREATE TABLE IF NOT EXISTS interface_extraction_requete (
    id BIGSERIAL PRIMARY KEY,
    interface_extraction_id BIGINT NOT NULL,
    script_sql TEXT NOT NULL,
    parent_id BIGINT,
    ordre_execution INTEGER NOT NULL DEFAULT 1,
    type_requete VARCHAR(30) NOT NULL DEFAULT 'PRINCIPALE' CHECK (type_requete IN ('PRINCIPALE', 'SOUS_REQUETE', 'POST_TRAITEMENT')),
    actif CHAR(1) NOT NULL DEFAULT 'Y' CHECK (actif IN ('Y', 'N')),

    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,

    CONSTRAINT fk_interface_extraction_requete_extraction FOREIGN KEY (interface_extraction_id) REFERENCES interface_extraction(id) ON DELETE CASCADE,
    CONSTRAINT fk_interface_extraction_requete_parent FOREIGN KEY (parent_id) REFERENCES interface_extraction_requete(id) ON DELETE CASCADE
);

-- Create interface_extraction_param table
CREATE TABLE IF NOT EXISTS interface_extraction_param (
    id BIGSERIAL PRIMARY KEY,
    requete_id BIGINT NOT NULL,
    nom_param VARCHAR(120) NOT NULL,
    type_param VARCHAR(30) NOT NULL CHECK (type_param IN ('STRING', 'INTEGER', 'DECIMAL', 'DATE', 'BOOLEAN')),
    position INTEGER NOT NULL,
    obligatoire CHAR(1) NOT NULL DEFAULT 'Y' CHECK (obligatoire IN ('Y', 'N')),
    actif CHAR(1) NOT NULL DEFAULT 'Y' CHECK (actif IN ('Y', 'N')),

    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,

    CONSTRAINT fk_interface_extraction_param_requete FOREIGN KEY (requete_id) REFERENCES interface_extraction_requete(id) ON DELETE CASCADE,
    CONSTRAINT uk_interface_extraction_param_position UNIQUE (requete_id, position)
);

-- Create interface_extraction_liaison table
CREATE TABLE IF NOT EXISTS interface_extraction_liaison (
    id BIGSERIAL PRIMARY KEY,
    requete_fille_id BIGINT NOT NULL,
    param_position INTEGER NOT NULL,
    source_type VARCHAR(30) NOT NULL CHECK (source_type IN ('PARENT_COL', 'EXTERNAL', 'CONSTANT')),
    source_valeur VARCHAR(255) NOT NULL,

    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,

    CONSTRAINT fk_interface_extraction_liaison_requete FOREIGN KEY (requete_fille_id) REFERENCES interface_extraction_requete(id) ON DELETE CASCADE,
    CONSTRAINT uk_interface_extraction_liaison_param UNIQUE (requete_fille_id, param_position)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_interface_extraction_code ON interface_extraction(code_extraction);
CREATE INDEX IF NOT EXISTS idx_interface_extraction_entreprise ON interface_extraction(entreprise_id);
CREATE INDEX IF NOT EXISTS idx_interface_extraction_requete_extraction ON interface_extraction_requete(interface_extraction_id);
CREATE INDEX IF NOT EXISTS idx_interface_extraction_requete_parent ON interface_extraction_requete(parent_id);
CREATE INDEX IF NOT EXISTS idx_interface_extraction_requete_ordre ON interface_extraction_requete(interface_extraction_id, ordre_execution);
CREATE INDEX IF NOT EXISTS idx_interface_extraction_param_requete ON interface_extraction_param(requete_id);
CREATE INDEX IF NOT EXISTS idx_interface_extraction_liaison_requete ON interface_extraction_liaison(requete_fille_id);

-- Add comments to tables
COMMENT ON TABLE interface_extraction IS 'Configuration pour l''extraction de données vers CSV';
COMMENT ON TABLE interface_extraction_requete IS 'Requêtes SQL pour l''extraction de données avec support hiérarchique';
COMMENT ON TABLE interface_extraction_param IS 'Paramètres SQL (?1, ?2, ...) pour les requêtes d''extraction';
COMMENT ON TABLE interface_extraction_liaison IS 'Liaisons entre requêtes parent/enfant pour passer des valeurs';
