CREATE TABLE IF NOT EXISTS sys_action_logs (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    description VARCHAR(500),

    request_id VARCHAR(50) NOT NULL,
    span_id VARCHAR(50),
    username VARCHAR(100),
    service VARCHAR(100),
    action VARCHAR(200),
    request_method VARCHAR(16),
    request_url VARCHAR(500),
    request_url_path VARCHAR(300),
    request_query VARCHAR(1000),
    request_ip VARCHAR(64),
    user_agent VARCHAR(500),
    status_code INTEGER,
    error_code VARCHAR(100),
    error_msg VARCHAR(2000),
    request_start TIMESTAMP,
    request_end TIMESTAMP,
    duration_ms BIGINT
);

CREATE INDEX IF NOT EXISTS idx_sys_action_logs_request_id ON sys_action_logs(request_id);
CREATE INDEX IF NOT EXISTS idx_sys_action_logs_username ON sys_action_logs(username);
CREATE INDEX IF NOT EXISTS idx_sys_action_logs_action ON sys_action_logs(action);
CREATE INDEX IF NOT EXISTS idx_sys_action_logs_status_code ON sys_action_logs(status_code);
CREATE INDEX IF NOT EXISTS idx_sys_action_logs_created_at ON sys_action_logs(created_at);
