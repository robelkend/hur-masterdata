-- Create conge_employe table
CREATE TABLE IF NOT EXISTS conge_employe (
    id BIGSERIAL PRIMARY KEY,
    entreprise_id BIGINT,
    employe_id BIGINT NOT NULL,
    type_conge_id BIGINT NOT NULL,
    date_debut_plan DATE NOT NULL,
    date_fin_plan DATE NOT NULL,
    date_debut_reel DATE,
    date_fin_reel DATE,
    motif TEXT,
    reference VARCHAR(120),
    approbateur VARCHAR(255),
    date_decision TIMESTAMP WITH TIME ZONE,
    commentaire_decision TEXT,
    nb_jours_plan NUMERIC(10,2) NOT NULL DEFAULT 0,
    nb_jours_reel NUMERIC(10,2) NOT NULL DEFAULT 0,
    statut VARCHAR(20) NOT NULL DEFAULT 'BROUILLON' CHECK (statut IN ('BROUILLON', 'SOUMIS', 'APPROUVE', 'EN_COURS', 'TERMINE', 'ANNULE', 'REJETE', 'FINALISE')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_conge_employe_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id) ON DELETE RESTRICT,
    CONSTRAINT fk_conge_employe_employe FOREIGN KEY (employe_id) REFERENCES employe(id) ON DELETE RESTRICT,
    CONSTRAINT fk_conge_employe_type_conge FOREIGN KEY (type_conge_id) REFERENCES type_conge(id) ON DELETE RESTRICT
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_conge_employe_entreprise ON conge_employe(entreprise_id);
CREATE INDEX IF NOT EXISTS idx_conge_employe_employe ON conge_employe(employe_id);
CREATE INDEX IF NOT EXISTS idx_conge_employe_type_conge ON conge_employe(type_conge_id);
CREATE INDEX IF NOT EXISTS idx_conge_employe_date_debut_plan ON conge_employe(date_debut_plan);
CREATE INDEX IF NOT EXISTS idx_conge_employe_date_fin_plan ON conge_employe(date_fin_plan);
CREATE INDEX IF NOT EXISTS idx_conge_employe_statut ON conge_employe(statut);
