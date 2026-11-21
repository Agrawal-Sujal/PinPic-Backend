-- PinPic Delta Sync Schema Migration (PostgreSQL)
-- Monotonic global revision sequence + Soft Delete tombstones + Echo Suppression

-- 1. Create global monotonic sequence
CREATE SEQUENCE IF NOT EXISTS global_sync_seq START WITH 1;

-- 2. Add columns to photos table
ALTER TABLE photos 
    ADD COLUMN IF NOT EXISTS revision BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS last_modified_device_id VARCHAR(64) NULL;

CREATE INDEX IF NOT EXISTS idx_photos_sync_rev ON photos (user_id, revision ASC);

-- 3. Add columns to clusters table
ALTER TABLE clusters 
    ADD COLUMN IF NOT EXISTS revision BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS last_modified_device_id VARCHAR(64) NULL;

CREATE INDEX IF NOT EXISTS idx_clusters_sync_rev ON clusters (user_id, revision ASC);

-- 4. Trigger function to auto-assign revision from sequence
CREATE OR REPLACE FUNCTION set_sync_revision()
RETURNS TRIGGER AS $$
BEGIN
    NEW.revision := nextval('global_sync_seq');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 5. Attach triggers
DROP TRIGGER IF EXISTS trg_photos_sync_revision ON photos;
CREATE TRIGGER trg_photos_sync_revision
    BEFORE INSERT OR UPDATE ON photos
    FOR EACH ROW EXECUTE FUNCTION set_sync_revision();

DROP TRIGGER IF EXISTS trg_clusters_sync_revision ON clusters;
CREATE TRIGGER trg_clusters_sync_revision
    BEFORE INSERT OR UPDATE ON clusters
    FOR EACH ROW EXECUTE FUNCTION set_sync_revision();
