-- Create entreprise table with audit fields and optimistic concurrency control
CREATE TABLE IF NOT EXISTS entreprise (
    id BIGSERIAL PRIMARY KEY,
    code_entreprise VARCHAR(50) NOT NULL UNIQUE,
    nom_entreprise VARCHAR(255) NOT NULL,
    nom_legal VARCHAR(255),
    devise_id BIGINT NOT NULL,
    mois_debut_annee_fiscale VARCHAR(50) NOT NULL CHECK (mois_debut_annee_fiscale IN ('JANVIER', 'FEVRIER', 'MARS', 'AVRIL', 'MAI', 'JUIN', 'JUILLET', 'AOUT', 'SEPTEMBRE', 'OCTOBRE', 'NOVEMBRE', 'DECEMBRE')),
    annee_fiscale_courante VARCHAR(20),
    secteur_activite VARCHAR(50) CHECK (secteur_activite IN ('BANQUE', 'ASSURANCE', 'CROISIERE', 'DIVERTISSEMENT', 'TRANSPORT', 'AGROALIMENTAIRE', 'PETROLIER', 'COSMETIQUE', 'CONSTRUCTION', 'INDUSTRIE', 'TCI', 'HOTELERIE', 'GASTRONOMIE', 'COURRIER', 'PUBLICITE', 'COMMERCE', 'SANTE', 'EDUCATION', 'GOUVERNEMENT', 'AUTRE')),
    etat VARCHAR(100),
    ville VARCHAR(100),
    adresse TEXT,
    telephone1 VARCHAR(50),
    telephone2 VARCHAR(50),
    fax VARCHAR(50),
    courriel VARCHAR(255),
    conge_cumule CHAR(1) NOT NULL DEFAULT 'N' CHECK (conge_cumule IN ('Y', 'N')),
    conge_apres_annees INTEGER NOT NULL DEFAULT 0,
    nb_annees_cumul_accepte INTEGER NOT NULL DEFAULT 0,
    generer_absence_dans_jours INTEGER NOT NULL DEFAULT 1,
    date_conge_genere DATE,
    auto_activer_conge CHAR(1) NOT NULL DEFAULT 'N' CHECK (auto_activer_conge IN ('Y', 'N')),
    auto_fermer_conge CHAR(1) NOT NULL DEFAULT 'N' CHECK (auto_fermer_conge IN ('Y', 'N')),
    matricule_assureur_defaut VARCHAR(100),
    entreprise_mere_id BIGINT,
    actif CHAR(1) NOT NULL DEFAULT 'Y' CHECK (actif IN ('Y', 'N')),
    logo_url TEXT,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_entreprise_devise FOREIGN KEY (devise_id) REFERENCES devise(id),
    CONSTRAINT fk_entreprise_entreprise_mere FOREIGN KEY (entreprise_mere_id) REFERENCES entreprise(id)
);

CREATE INDEX IF NOT EXISTS idx_entreprise_code ON entreprise(code_entreprise);
CREATE INDEX IF NOT EXISTS idx_entreprise_actif ON entreprise(actif);
CREATE INDEX IF NOT EXISTS idx_entreprise_devise ON entreprise(devise_id);
