-- Migration pour créer la table alert_config
-- Cette table permet de configurer pour quel type de message on envoie un push alert ou un email

CREATE TABLE IF NOT EXISTS alert_config (
    id BIGSERIAL PRIMARY KEY,
    code_message VARCHAR(100) NOT NULL UNIQUE,
    email CHAR(1) NOT NULL DEFAULT 'N' CHECK (email IN ('Y', 'N')),
    push_alert CHAR(1) NOT NULL DEFAULT 'N' CHECK (push_alert IN ('Y', 'N')),
    
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    
    CONSTRAINT fk_alert_config_message FOREIGN KEY (code_message) REFERENCES message_definition(code_message)
);

CREATE INDEX IF NOT EXISTS idx_alert_config_code_message ON alert_config(code_message);
