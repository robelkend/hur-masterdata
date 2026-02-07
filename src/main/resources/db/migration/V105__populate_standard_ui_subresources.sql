-- Create standard sub-resources for every WINDOW resource
-- Sections: filters, form, table, details
INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT r.code_resource || '#filters', 'SECTION', r.id, 'Filtres', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.type_resource = 'WINDOW'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT r.code_resource || '#form', 'SECTION', r.id, 'Formulaire', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.type_resource = 'WINDOW'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT r.code_resource || '#table', 'SECTION', r.id, 'Liste', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.type_resource = 'WINDOW'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT r.code_resource || '#details', 'SECTION', r.id, 'Details', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.type_resource = 'WINDOW'
ON CONFLICT (code_resource) DO NOTHING;

-- Filter actions
INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT r.code_resource || '#action-search', 'COMPONENT', r.id, 'Rechercher', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.type_resource = 'SECTION'
  AND r.code_resource LIKE '%#filters'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT r.code_resource || '#action-reset', 'COMPONENT', r.id, 'Reinitialiser', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.type_resource = 'SECTION'
  AND r.code_resource LIKE '%#filters'
ON CONFLICT (code_resource) DO NOTHING;

-- Form actions
INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT r.code_resource || '#action-save', 'COMPONENT', r.id, 'Enregistrer', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.type_resource = 'SECTION'
  AND r.code_resource LIKE '%#form'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT r.code_resource || '#action-create', 'COMPONENT', r.id, 'Creer', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.type_resource = 'SECTION'
  AND r.code_resource LIKE '%#form'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT r.code_resource || '#action-update', 'COMPONENT', r.id, 'Modifier', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.type_resource = 'SECTION'
  AND r.code_resource LIKE '%#form'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT r.code_resource || '#action-validate', 'COMPONENT', r.id, 'Valider', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.type_resource = 'SECTION'
  AND r.code_resource LIKE '%#form'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT r.code_resource || '#action-finalize', 'COMPONENT', r.id, 'Finaliser', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.type_resource = 'SECTION'
  AND r.code_resource LIKE '%#form'
ON CONFLICT (code_resource) DO NOTHING;

-- Table actions
INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT r.code_resource || '#action-edit', 'COMPONENT', r.id, 'Editer', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.type_resource = 'SECTION'
  AND r.code_resource LIKE '%#table'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT r.code_resource || '#action-delete', 'COMPONENT', r.id, 'Supprimer', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.type_resource = 'SECTION'
  AND r.code_resource LIKE '%#table'
ON CONFLICT (code_resource) DO NOTHING;
