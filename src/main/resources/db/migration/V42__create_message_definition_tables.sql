-- Create message_definition table
CREATE TABLE IF NOT EXISTS message_definition (
    id_message BIGSERIAL PRIMARY KEY,
    code_message VARCHAR(100) NOT NULL UNIQUE,
    titre VARCHAR(255) NOT NULL,
    langue VARCHAR(10) NOT NULL CHECK (langue IN ('en', 'fr', 'cr', 'es')),
    frequence VARCHAR(20) NOT NULL CHECK (frequence IN ('SERVICE', 'UNE', 'JOUR', 'SEMAINE', 'MOIS', 'EVENEMENT')),
    email_envoye CHAR(1) NOT NULL DEFAULT 'N' CHECK (email_envoye IN ('Y', 'N')),
    format VARCHAR(20) NOT NULL CHECK (format IN ('TEXT', 'HTML')),
    contenu TEXT NOT NULL,
    actif CHAR(1) NOT NULL DEFAULT 'Y' CHECK (actif IN ('Y', 'N')),
    entreprise_id BIGINT,

    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,

    CONSTRAINT fk_message_def_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id) ON DELETE SET NULL
);

-- Create message_destinataire table
CREATE TABLE IF NOT EXISTS message_destinataire (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    type_cible VARCHAR(20) NOT NULL CHECK (type_cible IN ('EMPLOYE', 'GROUPE', 'SERVICE', 'EMAIL')),
    valeur_cible VARCHAR(255) NOT NULL,
    mode_envoi VARCHAR(20) NOT NULL CHECK (mode_envoi IN ('EMAIL', 'NOTIFICATION')),

    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,

    CONSTRAINT fk_message_dest_message FOREIGN KEY (message_id) REFERENCES message_definition(id_message) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_message_def_code ON message_definition(code_message);
CREATE INDEX IF NOT EXISTS idx_message_def_langue ON message_definition(langue);
CREATE INDEX IF NOT EXISTS idx_message_def_frequence ON message_definition(frequence);
CREATE INDEX IF NOT EXISTS idx_message_def_actif ON message_definition(actif);
CREATE INDEX IF NOT EXISTS idx_message_def_entreprise ON message_definition(entreprise_id);
CREATE INDEX IF NOT EXISTS idx_message_dest_message ON message_destinataire(message_id);
CREATE INDEX IF NOT EXISTS idx_message_dest_type_cible ON message_destinataire(type_cible);
