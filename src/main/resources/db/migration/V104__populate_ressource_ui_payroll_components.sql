-- Payroll sub-resources (sections/components)
INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT '/hr/payroll#filters', 'SECTION', r.id, 'Filtres', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.code_resource = '/hr/payroll'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT '/hr/payroll#form', 'SECTION', r.id, 'Formulaire', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.code_resource = '/hr/payroll'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT '/hr/payroll#table', 'SECTION', r.id, 'Liste', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.code_resource = '/hr/payroll'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT '/hr/payroll#details', 'SECTION', r.id, 'Details', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.code_resource = '/hr/payroll'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT '/hr/payroll#action-calc', 'COMPONENT', r.id, 'Calculer', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.code_resource = '/hr/payroll#form'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT '/hr/payroll#action-validate', 'COMPONENT', r.id, 'Valider', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.code_resource = '/hr/payroll#form'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT '/hr/payroll#action-finalize', 'COMPONENT', r.id, 'Finaliser', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.code_resource = '/hr/payroll#form'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT '/hr/payroll#action-save', 'COMPONENT', r.id, 'Enregistrer', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.code_resource = '/hr/payroll#form'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT '/hr/payroll#action-edit', 'COMPONENT', r.id, 'Editer', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.code_resource = '/hr/payroll#table'
ON CONFLICT (code_resource) DO NOTHING;

INSERT INTO ressource_ui (code_resource, type_resource, parent_id, libelle, est_menu, actif, created_by, rowscn)
SELECT '/hr/payroll#action-delete', 'COMPONENT', r.id, 'Supprimer', 'N', 'Y', 'system', 1
FROM ressource_ui r
WHERE r.code_resource = '/hr/payroll#table'
ON CONFLICT (code_resource) DO NOTHING;
