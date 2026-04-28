CREATE TABLE IF NOT EXISTS excel_export_jobs (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    description VARCHAR(500),
    report_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    file_name VARCHAR(255),
    file_path VARCHAR(1000),
    error_message VARCHAR(2000),
    requested_by VARCHAR(100),
    requested_at TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_excel_export_jobs_status ON excel_export_jobs(status);
CREATE INDEX IF NOT EXISTS idx_excel_export_jobs_requested_by ON excel_export_jobs(requested_by);
CREATE INDEX IF NOT EXISTS idx_excel_export_jobs_requested_at ON excel_export_jobs(requested_at);
