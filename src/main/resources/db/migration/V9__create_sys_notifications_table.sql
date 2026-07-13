CREATE TABLE IF NOT EXISTS sys_notifications (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    description VARCHAR(500),

    sender_user_id VARCHAR(36),
    sender_name VARCHAR(100),
    recipient_user_ids JSONB NOT NULL DEFAULT '[]'::jsonb,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    action_label VARCHAR(100),
    target_url VARCHAR(1000),
    file_url VARCHAR(1000),
    file_name VARCHAR(255),
    icon VARCHAR(100),
    related_entity_type VARCHAR(100),
    related_entity_id VARCHAR(36),
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    is_dismissible BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at TIMESTAMP,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE INDEX IF NOT EXISTS idx_sys_notifications_created_at
    ON sys_notifications(created_at);

DO $$
BEGIN
    ALTER TABLE sys_notifications
        ADD CONSTRAINT fk_sys_notifications_sender_user
        FOREIGN KEY (sender_user_id) REFERENCES idp_users(id)
        ON DELETE SET NULL;
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;
