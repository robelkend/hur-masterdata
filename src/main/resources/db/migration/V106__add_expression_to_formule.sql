-- Add free-text expression to formule and remove element table
ALTER TABLE formule ADD COLUMN IF NOT EXISTS expression TEXT;

UPDATE formule SET expression = '' WHERE expression IS NULL;

ALTER TABLE formule ALTER COLUMN expression SET NOT NULL;

DROP TABLE IF EXISTS formule_element;
