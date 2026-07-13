ALTER TABLE sys_email_configs
    ADD COLUMN IF NOT EXISTS config_json JSONB NOT NULL DEFAULT '{}'::jsonb;

UPDATE sys_email_configs
SET config_json = jsonb_strip_nulls(
    jsonb_build_object(
        'host', host,
        'port', port,
        'username', username,
        'password', password,
        'fromAddress', from_address,
        'fromName', from_name,
        'replyTo', reply_to,
        'protocol', protocol,
        'encoding', encoding,
        'authEnabled', auth_enabled,
        'starttlsEnabled', starttls_enabled,
        'sslEnabled', ssl_enabled,
        'debugEnabled', debug_enabled,
        'timeoutMs', timeout_ms,
        'connectionTimeoutMs', connection_timeout_ms,
        'writeTimeoutMs', write_timeout_ms,
        'maxRecipients', max_recipients,
        'defaultCc', default_cc,
        'defaultBcc', default_bcc,
        'providerSettings', provider_settings
    )
)
WHERE config_json = '{}'::jsonb;
