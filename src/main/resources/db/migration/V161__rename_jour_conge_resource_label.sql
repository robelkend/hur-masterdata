UPDATE ressource_ui
SET libelle = 'Jour Férié'
WHERE code_resource = '/hr/jour-conge'
  AND (libelle = 'Jour Conge' OR libelle IS NULL);
