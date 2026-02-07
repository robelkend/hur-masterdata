-- Drop old piece table
DROP TABLE IF EXISTS piece;

-- Create type_piece
CREATE TABLE IF NOT EXISTS type_piece (
    id BIGSERIAL PRIMARY KEY,
    entreprise_id BIGINT NOT NULL,
    code_piece VARCHAR(30) NOT NULL,
    libelle VARCHAR(255) NOT NULL,
    actif CHAR(1) NOT NULL DEFAULT 'Y' CHECK (actif IN ('Y', 'N')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_type_piece_entreprise FOREIGN KEY (entreprise_id) REFERENCES entreprise(id)
);

CREATE INDEX IF NOT EXISTS idx_type_piece_entreprise ON type_piece(entreprise_id);
CREATE INDEX IF NOT EXISTS idx_type_piece_code ON type_piece(code_piece);
CREATE UNIQUE INDEX IF NOT EXISTS idx_type_piece_code_unique ON type_piece(entreprise_id, code_piece);

-- Create tarif_piece
CREATE TABLE IF NOT EXISTS tarif_piece (
    id BIGSERIAL PRIMARY KEY,
    type_piece_id BIGINT NOT NULL,
    devise_id BIGINT NOT NULL,
    prix_unitaire NUMERIC(15,2) NOT NULL,
    date_effectif DATE NOT NULL,
    date_fin DATE,
    actif CHAR(1) NOT NULL DEFAULT 'Y' CHECK (actif IN ('Y', 'N')),
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_on TIMESTAMP WITH TIME ZONE,
    rowscn INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_tarif_piece_type_piece FOREIGN KEY (type_piece_id) REFERENCES type_piece(id),
    CONSTRAINT fk_tarif_piece_devise FOREIGN KEY (devise_id) REFERENCES devise(id),
    CONSTRAINT chk_tarif_piece_dates CHECK (date_fin IS NULL OR date_fin >= date_effectif)
);

CREATE INDEX IF NOT EXISTS idx_tarif_piece_type_piece ON tarif_piece(type_piece_id);
CREATE INDEX IF NOT EXISTS idx_tarif_piece_devise ON tarif_piece(devise_id);
CREATE INDEX IF NOT EXISTS idx_tarif_piece_date_effectif ON tarif_piece(date_effectif, date_fin);
