-- Create audit_acces table
CREATE TABLE IF NOT EXISTS audit_acces (
    id BIGSERIAL PRIMARY KEY,
    date_evenement TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    entreprise_id BIGINT,
    utilisateur VARCHAR(100),
    type_evenement VARCHAR(20) NOT NULL CHECK (type_evenement IN ('LOGIN', 'LOGOUT', 'PAGE_VIEW', 'ACTION', 'API_CALL', 'FAIL_LOGIN')),
    resultat VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' CHECK (resultat IN ('SUCCESS', 'FAIL', 'DENY')),
    resource_type VARCHAR(50),
    resource_code VARCHAR(255),
    action_code VARCHAR(50),
    cible_type VARCHAR(50),
    cible_id VARCHAR(255),
    ip_address VARCHAR(50),
    user_agent TEXT,
    session_id VARCHAR(255),
    request_id VARCHAR(255),
    duree_ms INTEGER,
    details JSONB NOT NULL DEFAULT '{}'::jsonb,

    CONSTRAINT fk_audit_acces_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_audit_acces_date ON audit_acces(date_evenement DESC);
CREATE INDEX IF NOT EXISTS idx_audit_acces_utilisateur ON audit_acces(utilisateur);
CREATE INDEX IF NOT EXISTS idx_audit_acces_type_evenement ON audit_acces(type_evenement);
CREATE INDEX IF NOT EXISTS idx_audit_acces_resultat ON audit_acces(resultat);
CREATE INDEX IF NOT EXISTS idx_audit_acces_entreprise ON audit_acces(entreprise_id);
CREATE INDEX IF NOT EXISTS idx_audit_acces_session ON audit_acces(session_id);
CREATE INDEX IF NOT EXISTS idx_audit_acces_ip ON audit_acces(ip_address);
