package com.sunflower.pinpic.sync.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSyncInitializer {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeSyncSchema() {
        try {
            log.info("Initializing delta sync schema (sequence, columns, indexes, triggers)...");

            // 1. Sequence
            jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS global_sync_seq START WITH 1;");

            // 2. Photos columns & indexes
            jdbcTemplate.execute("ALTER TABLE photos ADD COLUMN IF NOT EXISTS revision BIGINT NOT NULL DEFAULT 0;");
            jdbcTemplate.execute("ALTER TABLE photos ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;");
            jdbcTemplate.execute("ALTER TABLE photos ADD COLUMN IF NOT EXISTS last_modified_device_id VARCHAR(64) NULL;");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_photos_sync_rev ON photos (user_id, revision ASC);");

            // 3. Clusters columns & indexes
            jdbcTemplate.execute("ALTER TABLE clusters ADD COLUMN IF NOT EXISTS revision BIGINT NOT NULL DEFAULT 0;");
            jdbcTemplate.execute("ALTER TABLE clusters ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;");
            jdbcTemplate.execute("ALTER TABLE clusters ADD COLUMN IF NOT EXISTS last_modified_device_id VARCHAR(64) NULL;");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_clusters_sync_rev ON clusters (user_id, revision ASC);");

            // 4. Trigger function for monotonic sequence assignment
            jdbcTemplate.execute(
                    "CREATE OR REPLACE FUNCTION set_sync_revision() " +
                    "RETURNS TRIGGER AS $$ " +
                    "BEGIN " +
                    "    NEW.revision := nextval('global_sync_seq'); " +
                    "    RETURN NEW; " +
                    "END; " +
                    "$$ LANGUAGE plpgsql;"
            );

            // 5. Triggers
            jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_photos_sync_revision ON photos;");
            jdbcTemplate.execute(
                    "CREATE TRIGGER trg_photos_sync_revision " +
                    "BEFORE INSERT OR UPDATE ON photos " +
                    "FOR EACH ROW EXECUTE FUNCTION set_sync_revision();"
            );

            jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_clusters_sync_revision ON clusters;");
            jdbcTemplate.execute(
                    "CREATE TRIGGER trg_clusters_sync_revision " +
                    "BEFORE INSERT OR UPDATE ON clusters " +
                    "FOR EACH ROW EXECUTE FUNCTION set_sync_revision();"
            );

            log.info("Delta sync schema initialized successfully.");
        } catch (Exception e) {
            log.warn("Could not automatically initialize delta sync triggers/sequence (may not be PostgreSQL or lacks permissions): {}", e.getMessage());
        }
    }
}
