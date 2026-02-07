UPDATE ref_formule_token
SET type_element = 'PARENTHESE'
WHERE type_element = 'PARENTHSE';

ALTER TABLE ref_formule_token
    DROP CONSTRAINT IF EXISTS chk_ref_formule_token_type_element;

ALTER TABLE ref_formule_token
    ADD CONSTRAINT chk_ref_formule_token_type_element
    CHECK (type_element IN ('OPERATEUR', 'OPERANDE', 'PARENTHESE', 'VARIABLE'));
