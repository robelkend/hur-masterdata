ALTER TABLE presence_employe
    ADD COLUMN IF NOT EXISTS cumul_pause_min INTEGER DEFAULT 0;
