ALTER TABLE ref_formule_token
    DROP CONSTRAINT IF EXISTS ref_formule_token_type_element_check;

ALTER TABLE ref_formule_token
    ADD CONSTRAINT chk_ref_formule_token_type_element
    CHECK (type_element IN ('OPERATEUR', 'OPERANDE', 'PARENTHSE', 'VARIABLE'));
