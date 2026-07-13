WITH active_config AS (
    SELECT id
    FROM sys_email_configs
    WHERE enabled = TRUE
    ORDER BY is_default DESC, updated_at DESC, created_at DESC, id DESC
    LIMIT 1
)
UPDATE sys_email_configs
SET enabled = FALSE
WHERE enabled = TRUE
  AND id NOT IN (SELECT id FROM active_config);

CREATE UNIQUE INDEX IF NOT EXISTS ux_sys_email_configs_single_active
    ON sys_email_configs ((enabled))
    WHERE enabled = TRUE;
