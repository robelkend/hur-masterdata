-- Migration pour créer la table horaire_special
-- Cette table permet de définir des horaires spéciaux pour un employé (travail de nuit, jour férié à travailler, etc.)
-- Ces horaires ont la priorité sur l'horaire fixe (horaire)

CREATE TABLE IF NOT EXISTS horaire_special (
    id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE,
    heure_debut CHAR(5),
    heure_fin CHAR(5),
    priorite VARCHAR(20) NOT NULL CHECK (priorite IN ('MINEURE', 'MAJEURE')),
    frequence VARCHAR(20) NOT NULL CHECK (frequence IN ('JOUR', 'SEMAINE', 'QUINZAINE', 'MOIS')),
    unite_freq INTEGER NOT NULL DEFAULT 1 CHECK (unite_freq >= 1),
    actif CHAR(1) NOT NULL DEFAULT 'Y' CHECK (actif IN ('Y', 'N')),
    
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    
    CONSTRAINT fk_horaire_special_employe FOREIGN KEY (employe_id) REFERENCES employe(id),
    CONSTRAINT chk_horaire_special_dates CHECK (date_fin IS NULL OR date_fin >= date_debut)
);

CREATE INDEX IF NOT EXISTS idx_horaire_special_employe ON horaire_special(employe_id);
CREATE INDEX IF NOT EXISTS idx_horaire_special_dates ON horaire_special(date_debut, date_fin);
CREATE INDEX IF NOT EXISTS idx_horaire_special_actif ON horaire_special(actif);
