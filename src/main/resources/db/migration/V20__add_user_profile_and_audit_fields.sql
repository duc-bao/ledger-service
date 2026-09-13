-- V20__add_user_profile_and_audit_fields.sql
-- Bổ sung các trường phục vụ Figma Mapping (last_password_changed_at, is_active_captcha, delete_reason)

ALTER TABLE idp_users ADD COLUMN IF NOT EXISTS last_password_changed_at TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE idp_users ADD COLUMN IF NOT EXISTS is_active_captcha BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE idp_users ADD COLUMN IF NOT EXISTS delete_reason VARCHAR(500);

ALTER TABLE idp_groups ADD COLUMN IF NOT EXISTS delete_reason VARCHAR(500);

CREATE INDEX IF NOT EXISTS idx_idp_users_created_at ON idp_users (created_at);
CREATE INDEX IF NOT EXISTS idx_idp_users_last_login_at ON idp_users (last_login_at);
CREATE INDEX IF NOT EXISTS idx_org_department_users_user_id ON org_department_users (user_id, status);
CREATE INDEX IF NOT EXISTS idx_idp_user_groups_group_id ON idp_user_groups (group_id, status);
