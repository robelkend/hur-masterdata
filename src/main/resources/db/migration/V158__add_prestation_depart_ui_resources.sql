INSERT INTO ressource_ui (code_resource, type_resource, libelle, est_menu, actif, created_by, rowscn)
VALUES
('/hr/prestation-depart', 'WINDOW', 'Prestations depart', 'Y', 'Y', 'system', 1)
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT r.code_resource || '#filters', 'SECTION', r.id, 'Filtres', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.code_resource = '/hr/prestation-depart'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT r.code_resource || '#form', 'SECTION', r.id, 'Calcul', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.code_resource = '/hr/prestation-depart'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT r.code_resource || '#table', 'SECTION', r.id, 'Liste', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.code_resource = '/hr/prestation-depart'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT s.code_resource || '#action-calculate', 'COMPONENT', s.id, 'Calculer', 'N', 'Y', 'system', 1
FROM ressource_ui s
WHERE s.code_resource = '/hr/prestation-depart#form'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT s.code_resource || '#action-validate', 'COMPONENT', s.id, 'Valider', 'N', 'Y', 'system', 1
FROM ressource_ui s
WHERE s.code_resource = '/hr/prestation-depart#table'
ON CONFLICT (code_resource) DO NOTHING;
