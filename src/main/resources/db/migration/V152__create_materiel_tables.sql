CREATE TABLE IF NOT EXISTS ref_materiel (
    id BIGSERIAL PRIMARY KEY,
    code_materiel VARCHAR(80) NOT NULL,
    libelle VARCHAR(255) NOT NULL,
    categorie VARCHAR(30) NOT NULL DEFAULT 'AUTRE',
    depreciable VARCHAR(1) NOT NULL DEFAULT 'Y',
    duree_depreciation_mois INTEGER NOT NULL DEFAULT 60,
    duree_transfert_propriete_mois INTEGER NOT NULL DEFAULT 60,
    valeur_reference NUMERIC(18,2) NOT NULL DEFAULT 0,
    actif VARCHAR(1) NOT NULL DEFAULT 'Y',
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT uq_ref_materiel_code UNIQUE (code_materiel),
    CONSTRAINT ck_ref_materiel_categorie CHECK (categorie IN ('VEHICULE','INFORMATIQUE','TELECOMMUNICATION','OUTILLAGE','SECURITE','UNIFORME','ACCES','AUTRE')),
    CONSTRAINT ck_ref_materiel_depreciable CHECK (depreciable IN ('Y', 'N')),
    CONSTRAINT ck_ref_materiel_actif CHECK (actif IN ('Y', 'N'))
);

CREATE TABLE IF NOT EXISTS employe_materiel (
    id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL,
    materiel_id BIGINT NOT NULL,
    numero_serie VARCHAR(150),
    date_attribution DATE NOT NULL,
    date_fin_prevue DATE,
    valeur_attribution NUMERIC(18,2) NOT NULL DEFAULT 0,
    statut VARCHAR(30) NOT NULL,
    date_transfert_propriete DATE,
    date_restitution_effective DATE,
    valeur_residuelle_calculee NUMERIC(18,2),
    observations VARCHAR(500),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_employe_materiel_employe FOREIGN KEY (employe_id) REFERENCES employe(id),
    CONSTRAINT fk_employe_materiel_materiel FOREIGN KEY (materiel_id) REFERENCES ref_materiel(id),
    CONSTRAINT ck_employe_materiel_statut CHECK (statut IN ('ATTRIBUE','RESTITUE','TRANSFERE_EMPLOYE','PERDU','ENDOMMAGE','FACTURE','CLOTURE'))
);

CREATE INDEX IF NOT EXISTS idx_employe_materiel_employe ON employe_materiel(employe_id);
CREATE INDEX IF NOT EXISTS idx_employe_materiel_materiel ON employe_materiel(materiel_id);
CREATE INDEX IF NOT EXISTS idx_employe_materiel_statut ON employe_materiel(statut);

CREATE TABLE IF NOT EXISTS employe_materiel_evenement (
    id BIGSERIAL PRIMARY KEY,
    employe_materiel_id BIGINT NOT NULL,
    type_evenement VARCHAR(30) NOT NULL,
    date_evenement DATE NOT NULL,
    montant NUMERIC(18,2),
    commentaire VARCHAR(500),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_employe_materiel_evenement_em FOREIGN KEY (employe_materiel_id) REFERENCES employe_materiel(id),
    CONSTRAINT ck_employe_materiel_type_evt CHECK (type_evenement IN ('ATTRIBUTION','RESTITUTION','TRANSFERT','PERTE','DETERIORATION','FACTURATION','ANNULATION'))
);

CREATE INDEX IF NOT EXISTS idx_employe_materiel_evt_parent ON employe_materiel_evenement(employe_materiel_id);
