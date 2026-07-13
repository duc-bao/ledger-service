ALTER TABLE sys_email_configs
    DROP COLUMN IF EXISTS provider_settings,
    DROP COLUMN IF EXISTS config_json,
    DROP COLUMN IF EXISTS max_recipients;