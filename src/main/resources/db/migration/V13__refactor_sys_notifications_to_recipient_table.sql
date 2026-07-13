ALTER TABLE sys_notifications
    DROP COLUMN IF EXISTS recipient_user_ids;

CREATE TABLE IF NOT EXISTS sys_notification_recipients (
    id VARCHAR(36) PRIMARY KEY,

    notification_id VARCHAR(36) NOT NULL,
    recipient_user_id VARCHAR(36) NOT NULL,

    read_status VARCHAR(20) NOT NULL DEFAULT 'UNREAD',
    read_at TIMESTAMP,

    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    dismissed_at TIMESTAMP,
    deleted_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_recipient_notification
        FOREIGN KEY (notification_id)
        REFERENCES sys_notifications(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_notification_recipient
        UNIQUE (notification_id, recipient_user_id)
);

CREATE INDEX IF NOT EXISTS idx_notification_recipient_user_created
    ON sys_notification_recipients (
        recipient_user_id,
        created_at DESC
    );

CREATE INDEX IF NOT EXISTS idx_notification_recipient_user_status
    ON sys_notification_recipients (
        recipient_user_id,
        read_status,
        created_at DESC
    );

CREATE INDEX IF NOT EXISTS idx_notification_recipient_notification
    ON sys_notification_recipients (
        notification_id
    );

DO $$
BEGIN
    ALTER TABLE sys_notification_recipients
        ADD CONSTRAINT fk_notification_recipient_user
        FOREIGN KEY (recipient_user_id) REFERENCES idp_users(id)
        ON DELETE CASCADE;
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;
