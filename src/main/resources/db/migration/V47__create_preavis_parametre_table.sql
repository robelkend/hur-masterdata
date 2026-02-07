-- Migration pour créer la table preavis_parametre
-- Cette table permet de définir les paramètres de préavis selon le type de départ, l'ancienneté, le type d'employé et le régime de paie

CREATE TABLE IF NOT EXISTS preavis_parametre (
    id BIGSERIAL PRIMARY KEY,
    type_employe_id BIGINT,
    regime_paie_id BIGINT,
    type_depart VARCHAR(20) NOT NULL CHECK (type_depart IN ('DEMISSION', 'ABANDON', 'LICENCIEMENT', 'FIN_CONTRAT', 'RETRAITE')),
    anciennete_min INTEGER NOT NULL CHECK (anciennete_min >= 0),
    anciennete_max INTEGER CHECK (anciennete_max IS NULL OR anciennete_max >= anciennete_min),
    inclure_max CHAR(1) NOT NULL DEFAULT 'Y' CHECK (inclure_max IN ('Y', 'N')),
    valeur_preavis INTEGER NOT NULL CHECK (valeur_preavis >= 0),
    unite_preavis VARCHAR(10) NOT NULL CHECK (unite_preavis IN ('JOUR', 'MOIS')),
    mode_application VARCHAR(20) NOT NULL CHECK (mode_application IN ('A_EFFECTUER', 'A_PAYER')),
    priorite INTEGER NOT NULL DEFAULT 1 CHECK (priorite >= 1),
    entreprise_id BIGINT NOT NULL,
    actif CHAR(1) NOT NULL DEFAULT 'Y' CHECK (actif IN ('Y', 'N')),
    
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    
    CONSTRAINT fk_preavis_parametre_type_employe FOREIGN KEY (type_employe_id) REFERENCES type_employe(id),
    CONSTRAINT fk_preavis_parametre_regime_paie FOREIGN KEY (regime_paie_id) REFERENCES regime_paie(id),
    CONSTRAINT fk_preavis_parametre_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id)
);

CREATE INDEX IF NOT EXISTS idx_preavis_parametre_type_employe ON preavis_parametre(type_employe_id);
CREATE INDEX IF NOT EXISTS idx_preavis_parametre_regime_paie ON preavis_parametre(regime_paie_id);
CREATE INDEX IF NOT EXISTS idx_preavis_parametre_entreprise ON preavis_parametre(entreprise_id);
CREATE INDEX IF NOT EXISTS idx_preavis_parametre_type_depart ON preavis_parametre(type_depart);
CREATE INDEX IF NOT EXISTS idx_preavis_parametre_anciennete ON preavis_parametre(anciennete_min, anciennete_max);
CREATE INDEX IF NOT EXISTS idx_preavis_parametre_priorite ON preavis_parametre(priorite DESC);
CREATE INDEX IF NOT EXISTS idx_preavis_parametre_actif ON preavis_parametre(actif);
