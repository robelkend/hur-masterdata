ALTER TABLE role_groupe
    ADD COLUMN all_access CHAR(1) NOT NULL DEFAULT 'N'
    CHECK (all_access IN ('Y', 'N'));
