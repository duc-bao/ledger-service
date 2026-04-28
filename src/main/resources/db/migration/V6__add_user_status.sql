ALTER TABLE idp_users
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

UPDATE idp_users
SET status = 'ACTIVE'
WHERE status IS NULL;

CREATE INDEX IF NOT EXISTS idx_idp_users_status ON idp_users(status);

