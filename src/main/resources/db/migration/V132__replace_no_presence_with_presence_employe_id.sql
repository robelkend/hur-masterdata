-- no_presence is redundant with presence_employe_id.
-- Backfill presence_employe_id when possible, then remove no_presence.

UPDATE pointage_brut pb
SET presence_employe_id = pb.no_presence
WHERE pb.presence_employe_id IS NULL
  AND pb.no_presence IS NOT NULL
  AND EXISTS (
    SELECT 1
    FROM presence_employe pe
    WHERE pe.id = pb.no_presence
  );

ALTER TABLE pointage_brut
    DROP COLUMN IF EXISTS no_presence;
