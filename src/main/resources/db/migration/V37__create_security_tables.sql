-- Migration pour créer les tables du système de sécurité
-- Utilisateur, Rôles, Groupes, Permissions

-- Table: utilisateur
CREATE TABLE utilisateur (
    id BIGSERIAL PRIMARY KEY,
    identifiant VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    courriel VARCHAR(255),
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    langue VARCHAR(10) NOT NULL DEFAULT 'fr' CHECK (langue IN ('en', 'fr', 'cr', 'es')),
    actif CHAR(1) NOT NULL DEFAULT 'N' CHECK (actif IN ('Y', 'N')),
    password_hash VARCHAR(255) NOT NULL,
    date_exp_password DATE,
    entreprise_id BIGINT,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_utilisateur_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id) ON DELETE SET NULL
);

CREATE INDEX idx_utilisateur_identifiant ON utilisateur(identifiant);
CREATE INDEX idx_utilisateur_email ON utilisateur(email);
CREATE INDEX idx_utilisateur_actif ON utilisateur(actif);

-- Table: role_groupe
CREATE TABLE role_groupe (
    id BIGSERIAL PRIMARY KEY,
    code_groupe VARCHAR(100) NOT NULL UNIQUE,
    libelle VARCHAR(255) NOT NULL,
    actif CHAR(1) NOT NULL DEFAULT 'N' CHECK (actif IN ('Y', 'N')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_role_groupe_code ON role_groupe(code_groupe);
CREATE INDEX idx_role_groupe_actif ON role_groupe(actif);

-- Table: app_role
CREATE TABLE app_role (
    id BIGSERIAL PRIMARY KEY,
    code_role VARCHAR(100) NOT NULL UNIQUE,
    libelle VARCHAR(255) NOT NULL,
    actif CHAR(1) NOT NULL DEFAULT 'N' CHECK (actif IN ('Y', 'N')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_app_role_code ON app_role(code_role);
CREATE INDEX idx_app_role_actif ON app_role(actif);

-- Table: permission_action
CREATE TABLE permission_action (
    id BIGSERIAL PRIMARY KEY,
    code_action VARCHAR(50) NOT NULL UNIQUE,
    libelle VARCHAR(255) NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_permission_action_code ON permission_action(code_action);

-- Insertion des actions de permission par défaut
INSERT INTO permission_action (code_action, libelle, created_by) VALUES
('VIEW', 'Visualiser', 'system'),
('CREATE', 'Créer', 'system'),
('UPDATE', 'Modifier', 'system'),
('DELETE', 'Supprimer', 'system'),
('VALIDATE', 'Valider', 'system'),
('FINALIZE', 'Finaliser', 'system');

-- Table: ressource_ui
CREATE TABLE ressource_ui (
    id BIGSERIAL PRIMARY KEY,
    code_resource VARCHAR(255) NOT NULL UNIQUE,
    type_resource VARCHAR(50) NOT NULL CHECK (type_resource IN ('WINDOW', 'SECTION', 'COMPONENT', 'FIELD')),
    parent_id BIGINT,
    libelle VARCHAR(255) NOT NULL,
    est_menu CHAR(1) NOT NULL DEFAULT 'N' CHECK (est_menu IN ('Y', 'N')),
    actif CHAR(1) NOT NULL DEFAULT 'N' CHECK (actif IN ('Y', 'N')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_ressource_ui_parent FOREIGN KEY (parent_id) REFERENCES ressource_ui(id) ON DELETE SET NULL
);

CREATE INDEX idx_ressource_ui_code ON ressource_ui(code_resource);
CREATE INDEX idx_ressource_ui_type ON ressource_ui(type_resource);
CREATE INDEX idx_ressource_ui_parent ON ressource_ui(parent_id);
CREATE INDEX idx_ressource_ui_actif ON ressource_ui(actif);

-- Table: groupe_role_utilisateur
CREATE TABLE groupe_role_utilisateur (
    id BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT NOT NULL,
    groupe_id BIGINT NOT NULL,
    est_primaire CHAR(1) NOT NULL DEFAULT 'N' CHECK (est_primaire IN ('Y', 'N')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_groupe_role_utilisateur_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    CONSTRAINT fk_groupe_role_utilisateur_groupe FOREIGN KEY (groupe_id) REFERENCES role_groupe(id) ON DELETE CASCADE,
    CONSTRAINT uk_groupe_role_utilisateur UNIQUE (utilisateur_id, groupe_id)
);

CREATE INDEX idx_groupe_role_utilisateur_utilisateur ON groupe_role_utilisateur(utilisateur_id);
CREATE INDEX idx_groupe_role_utilisateur_groupe ON groupe_role_utilisateur(groupe_id);
CREATE INDEX idx_groupe_role_utilisateur_primaire ON groupe_role_utilisateur(est_primaire);

-- Table: role_groupe_role
CREATE TABLE role_groupe_role (
    id BIGSERIAL PRIMARY KEY,
    groupe_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    actif CHAR(1) NOT NULL DEFAULT 'N' CHECK (actif IN ('Y', 'N')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_role_groupe_role_groupe FOREIGN KEY (groupe_id) REFERENCES role_groupe(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_groupe_role_role FOREIGN KEY (role_id) REFERENCES app_role(id) ON DELETE CASCADE,
    CONSTRAINT uk_role_groupe_role UNIQUE (groupe_id, role_id)
);

CREATE INDEX idx_role_groupe_role_groupe ON role_groupe_role(groupe_id);
CREATE INDEX idx_role_groupe_role_role ON role_groupe_role(role_id);
CREATE INDEX idx_role_groupe_role_actif ON role_groupe_role(actif);

-- Table: role_permission
CREATE TABLE role_permission (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    ressource_id BIGINT NOT NULL,
    action_id BIGINT NOT NULL,
    effet VARCHAR(10) NOT NULL CHECK (effet IN ('ALLOW', 'DENY')),
    heritage_descendant BOOLEAN NOT NULL DEFAULT TRUE,
    actif CHAR(1) NOT NULL DEFAULT 'N' CHECK (actif IN ('Y', 'N')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES app_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permission_ressource FOREIGN KEY (ressource_id) REFERENCES ressource_ui(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permission_action FOREIGN KEY (action_id) REFERENCES permission_action(id) ON DELETE CASCADE,
    CONSTRAINT uk_role_permission UNIQUE (role_id, ressource_id, action_id)
);

CREATE INDEX idx_role_permission_role ON role_permission(role_id);
CREATE INDEX idx_role_permission_ressource ON role_permission(ressource_id);
CREATE INDEX idx_role_permission_action ON role_permission(action_id);
CREATE INDEX idx_role_permission_actif ON role_permission(actif);
