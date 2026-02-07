-- Add niveau_employe_id column to type_employe table
ALTER TABLE type_employe
ADD COLUMN niveau_employe_id BIGINT;

-- Add foreign key constraint
ALTER TABLE type_employe
ADD CONSTRAINT fk_type_employe_niveau_employe
FOREIGN KEY (niveau_employe_id) REFERENCES niveau_employe(id);

-- Add index for better query performance
CREATE INDEX idx_type_employe_niveau_employe_id ON type_employe(niveau_employe_id);
