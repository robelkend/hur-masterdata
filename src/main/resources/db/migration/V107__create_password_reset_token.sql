CREATE TABLE password_reset_token (
    id BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used CHAR(1) NOT NULL DEFAULT 'N' CHECK (used IN ('Y', 'N')),
    used_on TIMESTAMP WITH TIME ZONE,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_password_reset_token_user ON password_reset_token(utilisateur_id);
CREATE INDEX idx_password_reset_token_token ON password_reset_token(token);
