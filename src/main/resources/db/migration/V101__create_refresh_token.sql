CREATE TABLE refresh_token (
    id BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked CHAR(1) NOT NULL DEFAULT 'N' CHECK (revoked IN ('Y', 'N')),
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_token_user ON refresh_token(utilisateur_id);
CREATE INDEX idx_refresh_token_token ON refresh_token(token);
