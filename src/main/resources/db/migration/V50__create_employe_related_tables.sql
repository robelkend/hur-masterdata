-- Create employe_adresse table
CREATE TABLE IF NOT EXISTS employe_adresse (
    id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL,
    type_adresse VARCHAR(20) NOT NULL CHECK (type_adresse IN ('DOMICILE', 'POSTALE')),
    ligne1 VARCHAR(255) NOT NULL,
    ligne2 VARCHAR(255),
    ville VARCHAR(100) NOT NULL,
    etat VARCHAR(100),
    code_postal VARCHAR(20),
    pays VARCHAR(2),
    date_debut DATE NOT NULL,
    date_fin DATE,
    actif CHAR(1) NOT NULL DEFAULT 'Y' CHECK (actif IN ('Y', 'N')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_employe_adresse_employe FOREIGN KEY (employe_id) REFERENCES employe(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_employe_adresse_employe ON employe_adresse(employe_id);
CREATE INDEX IF NOT EXISTS idx_employe_adresse_actif ON employe_adresse(actif);

-- Create employe_identite table
CREATE TABLE IF NOT EXISTS employe_identite (
    id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL,
    type_piece VARCHAR(50) NOT NULL CHECK (type_piece IN ('NIF', 'CARTE_ELECTORALE', 'PASSEPORT', 'PERMIS', 'AUTRE')),
    numero_piece VARCHAR(100) NOT NULL,
    date_emission DATE,
    date_expiration DATE,
    pays_emission VARCHAR(2),
    actif CHAR(1) NOT NULL DEFAULT 'Y' CHECK (actif IN ('Y', 'N')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_employe_identite_employe FOREIGN KEY (employe_id) REFERENCES employe(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_employe_identite_employe ON employe_identite(employe_id);
CREATE INDEX IF NOT EXISTS idx_employe_identite_actif ON employe_identite(actif);

-- Create employe_contact table
CREATE TABLE IF NOT EXISTS employe_contact (
    id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255),
    lien VARCHAR(50),
    telephone1 VARCHAR(50),
    telephone2 VARCHAR(50),
    courriel VARCHAR(255),
    priorite INTEGER NOT NULL DEFAULT 1,
    actif CHAR(1) NOT NULL DEFAULT 'Y' CHECK (actif IN ('Y', 'N')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_employe_contact_employe FOREIGN KEY (employe_id) REFERENCES employe(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_employe_contact_employe ON employe_contact(employe_id);
CREATE INDEX IF NOT EXISTS idx_employe_contact_actif ON employe_contact(actif);

-- Create employe_document table
CREATE TABLE IF NOT EXISTS employe_document (
    id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL,
    type_document VARCHAR(50) NOT NULL CHECK (type_document IN ('CONTRAT', 'CV', 'DIPLOME', 'PIECE_ID', 'AUTRE')),
    nom_fichier VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100),
    taille_octets BIGINT,
    storage_ref TEXT,
    hash_sha256 VARCHAR(64),
    date_document DATE,
    commentaire TEXT,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_employe_document_employe FOREIGN KEY (employe_id) REFERENCES employe(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_employe_document_employe ON employe_document(employe_id);
CREATE INDEX IF NOT EXISTS idx_employe_document_type ON employe_document(type_document);

-- Create employe_note table
CREATE TABLE IF NOT EXISTS employe_note (
    id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL,
    type_note VARCHAR(50) NOT NULL CHECK (type_note IN ('BLAME', 'REMARQUE', 'PLAINTE', 'MESSAGE', 'AUTRE')),
    titre VARCHAR(255),
    note TEXT,
    confidentiel CHAR(1) NOT NULL DEFAULT 'N' CHECK (confidentiel IN ('Y', 'N')),
    envoye CHAR(1) NOT NULL DEFAULT 'N' CHECK (envoye IN ('Y', 'N')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_employe_note_employe FOREIGN KEY (employe_id) REFERENCES employe(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_employe_note_employe ON employe_note(employe_id);
CREATE INDEX IF NOT EXISTS idx_employe_note_type ON employe_note(type_note);

-- Create coordonnee_bancaire_employe table
CREATE TABLE IF NOT EXISTS coordonnee_bancaire_employe (
    id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL,
    banque_id BIGINT NOT NULL,
    numero_compte VARCHAR(100) NOT NULL,
    categorie VARCHAR(20) NOT NULL CHECK (categorie IN ('FRAIS', 'PRINCIPAL')),
    actif CHAR(1) NOT NULL DEFAULT 'N' CHECK (actif IN ('Y', 'N')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_coord_banc_employe FOREIGN KEY (employe_id) REFERENCES employe(id) ON DELETE CASCADE,
    CONSTRAINT fk_coord_banc_banque FOREIGN KEY (banque_id) REFERENCES institution_tierse(id)
);

CREATE INDEX IF NOT EXISTS idx_coord_banc_employe ON coordonnee_bancaire_employe(employe_id);
CREATE INDEX IF NOT EXISTS idx_coord_banc_banque ON coordonnee_bancaire_employe(banque_id);

-- Create assurance_employe table
CREATE TABLE IF NOT EXISTS assurance_employe (
    id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL,
    plan_assurance_id BIGINT NOT NULL,
    no_assurance VARCHAR(100) NOT NULL,
    actif CHAR(1) NOT NULL DEFAULT 'N' CHECK (actif IN ('Y', 'N')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_assurance_employe_employe FOREIGN KEY (employe_id) REFERENCES employe(id) ON DELETE CASCADE,
    CONSTRAINT fk_assurance_employe_plan FOREIGN KEY (plan_assurance_id) REFERENCES plan_assurance(id)
);

CREATE INDEX IF NOT EXISTS idx_assurance_employe_employe ON assurance_employe(employe_id);
CREATE INDEX IF NOT EXISTS idx_assurance_employe_plan ON assurance_employe(plan_assurance_id);

-- Add comments
COMMENT ON TABLE employe_adresse IS 'Employee addresses (DOMICILE or POSTALE)';
COMMENT ON TABLE employe_identite IS 'Employee identity documents (NIF, Carte Electorale, Passeport, etc.)';
COMMENT ON TABLE employe_contact IS 'Employee emergency contacts';
COMMENT ON TABLE employe_document IS 'Employee documents (contracts, CV, diplomas, etc.)';
COMMENT ON TABLE employe_note IS 'Employee notes (blame, remarks, complaints, messages, etc.)';
COMMENT ON TABLE coordonnee_bancaire_employe IS 'Employee bank account information';
COMMENT ON TABLE assurance_employe IS 'Employee insurance information';
