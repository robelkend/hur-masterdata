-- Add reference column to institution_tierse table
ALTER TABLE institution_tierse 
ADD COLUMN IF NOT EXISTS reference VARCHAR(255);

-- Add comment to the column
COMMENT ON COLUMN institution_tierse.reference IS 'Reference field for institution tierse';
