INSERT INTO ressource_ui (code_resource, type_resource, libelle, est_menu, actif, created_by, rowscn)
VALUES
('/hr/ref-materiel', 'WINDOW', 'References materiels', 'Y', 'Y', 'system', 1),
('/hr/employe-materiel', 'WINDOW', 'Attribution materiels', 'Y', 'Y', 'system', 1)
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT r.code_resource || '#filters', 'SECTION', r.id, 'Filtres', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.code_resource IN ('/hr/ref-materiel', '/hr/employe-materiel')
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT r.code_resource || '#form', 'SECTION', r.id, 'Formulaire', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.code_resource IN ('/hr/ref-materiel', '/hr/employe-materiel')
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT r.code_resource || '#table', 'SECTION', r.id, 'Liste', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.code_resource IN ('/hr/ref-materiel', '/hr/employe-materiel')
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT s.code_resource || '#action-search', 'COMPONENT', s.id, 'Rechercher', 'N', 'Y', 'system', 1
FROM ressource_ui s
WHERE s.code_resource IN ('/hr/ref-materiel#filters', '/hr/employe-materiel#filters')
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT s.code_resource || '#action-save', 'COMPONENT', s.id, 'Enregistrer', 'N', 'Y', 'system', 1
FROM ressource_ui s
WHERE s.code_resource IN ('/hr/ref-materiel#form', '/hr/employe-materiel#form')
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT s.code_resource || '#action-create', 'COMPONENT', s.id, 'Creer', 'N', 'Y', 'system', 1
FROM ressource_ui s
WHERE s.code_resource IN ('/hr/ref-materiel#form', '/hr/employe-materiel#form')
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT s.code_resource || '#action-edit', 'COMPONENT', s.id, 'Editer', 'N', 'Y', 'system', 1
FROM ressource_ui s
WHERE s.code_resource IN ('/hr/ref-materiel#table', '/hr/employe-materiel#table')
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT s.code_resource || '#action-delete', 'COMPONENT', s.id, 'Supprimer', 'N', 'Y', 'system', 1
FROM ressource_ui s
WHERE s.code_resource IN ('/hr/ref-materiel#table', '/hr/employe-materiel#table')
ON CONFLICT (code_resource) DO NOTHING;
