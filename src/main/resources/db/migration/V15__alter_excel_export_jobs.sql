-- Add new columns for the Excel Export Module to excel_export_jobs table.
ALTER TABLE excel_export_jobs
    ADD COLUMN IF NOT EXISTS template_code VARCHAR(100),
    ADD COLUMN IF NOT EXISTS template_snapshot TEXT,
    ADD COLUMN IF NOT EXISTS filter_snapshot TEXT,
    ADD COLUMN IF NOT EXISTS started_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS expired_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS content_type VARCHAR(150),
    ADD COLUMN IF NOT EXISTS total_rows BIGINT,
    ADD COLUMN IF NOT EXISTS processed_rows BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS file_size_bytes BIGINT,
    ADD COLUMN IF NOT EXISTS storage_provider VARCHAR(30) NOT NULL DEFAULT 'MINIO',
    ADD COLUMN IF NOT EXISTS storage_bucket VARCHAR(100),
    ADD COLUMN IF NOT EXISTS storage_object_key VARCHAR(500),
    ADD COLUMN IF NOT EXISTS error_code VARCHAR(100),
    ADD COLUMN IF NOT EXISTS attempt_count INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS worker_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Create indexes for performance.
CREATE INDEX IF NOT EXISTS idx_export_jobs_owner_time ON excel_export_jobs(requested_by, requested_at DESC);
CREATE INDEX IF NOT EXISTS idx_export_jobs_status_time ON excel_export_jobs(status, requested_at);
CREATE INDEX IF NOT EXISTS idx_export_jobs_type_status ON excel_export_jobs(report_type, status);

-- Seed permission definitions.
INSERT INTO idp_permission_definitions (id, code, name, module_code, action_code, resource_type, status, description, created_at, created_by, updated_at, updated_by)
VALUES
  ('d64e031a-7b3b-48ae-8a4e-0a56acbb0001', 'USER_EXPORT', 'Export Users to Excel', 'EXPORT', 'USER_EXPORT', 'EXPORT', 'ACTIVE', 'Permission to export users list to Excel', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
  ('d64e031a-7b3b-48ae-8a4e-0a56acbb0002', 'DEPARTMENT_EXPORT', 'Export Departments to Excel', 'EXPORT', 'DEPARTMENT_EXPORT', 'EXPORT', 'ACTIVE', 'Permission to export departments list to Excel', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
  ('d64e031a-7b3b-48ae-8a4e-0a56acbb0003', 'AUDIT_LOG_EXPORT', 'Export Audit Logs to Excel', 'EXPORT', 'AUDIT_LOG_EXPORT', 'EXPORT', 'ACTIVE', 'Permission to export audit/action logs to Excel', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
  ('d64e031a-7b3b-48ae-8a4e-0a56acbb0004', 'EXPORT_JOB_VIEW_ALL', 'View All Export Jobs', 'EXPORT', 'EXPORT_JOB_VIEW_ALL', 'EXPORT', 'ACTIVE', 'Permission to view and manage all export jobs', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
  ('d64e031a-7b3b-48ae-8a4e-0a56acbb0005', 'EXPORT_JOB_VIEW', 'View Own Export Jobs', 'EXPORT', 'EXPORT_JOB_VIEW', 'EXPORT', 'ACTIVE', 'Permission to view and download own export jobs', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system')
ON CONFLICT (code) DO NOTHING;

-- Seed API permission mappings.
INSERT INTO idp_permission_apis (id, permission_id, http_method, uri_pattern, match_type, service_code, status, priority, is_allow, description, created_at, created_by, updated_at, updated_by)
VALUES
  ('a0e4e2a8-1234-4567-89ab-cdef00000001', 'd64e031a-7b3b-48ae-8a4e-0a56acbb0001', 'POST', '/api/v1/exports/users', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 0, FALSE, 'Export Users API', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
  ('a0e4e2a8-1234-4567-89ab-cdef00000002', 'd64e031a-7b3b-48ae-8a4e-0a56acbb0002', 'POST', '/api/v1/exports/departments', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 0, FALSE, 'Export Departments API', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
  ('a0e4e2a8-1234-4567-89ab-cdef00000003', 'd64e031a-7b3b-48ae-8a4e-0a56acbb0003', 'POST', '/api/v1/exports/audit-logs', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 0, FALSE, 'Export Audit Logs API', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
  ('a0e4e2a8-1234-4567-89ab-cdef00000004', 'd64e031a-7b3b-48ae-8a4e-0a56acbb0005', 'GET', '/api/v1/exports', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 0, FALSE, 'List Export Jobs API', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
  ('a0e4e2a8-1234-4567-89ab-cdef00000005', 'd64e031a-7b3b-48ae-8a4e-0a56acbb0005', 'GET', '/api/v1/exports/*', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 0, FALSE, 'Get Export Job Status API', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
  ('a0e4e2a8-1234-4567-89ab-cdef00000006', 'd64e031a-7b3b-48ae-8a4e-0a56acbb0005', 'GET', '/api/v1/exports/*/download', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 0, FALSE, 'Download Export File API', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
  ('a0e4e2a8-1234-4567-89ab-cdef00000007', 'd64e031a-7b3b-48ae-8a4e-0a56acbb0005', 'POST', '/api/v1/exports/*/cancel', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 0, FALSE, 'Cancel Export Job API', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system')
ON CONFLICT (permission_id, http_method, uri_pattern, service_code) DO NOTHING;

-- Grant permissions to SUPER_ADMIN / ROLE_ADMIN.
INSERT INTO idp_role_permissions (id, group_id, permission_id, status, effective_from, created_at, created_by, updated_at, updated_by)
SELECT
    LOWER(SUBSTRING(md5(g.id || ':' || p.id) FROM 1 FOR 8) || '-' ||
          SUBSTRING(md5(g.id || ':' || p.id) FROM 9 FOR 4) || '-' ||
          SUBSTRING(md5(g.id || ':' || p.id) FROM 13 FOR 4) || '-' ||
          SUBSTRING(md5(g.id || ':' || p.id) FROM 17 FOR 4) || '-' ||
          SUBSTRING(md5(g.id || ':' || p.id) FROM 21 FOR 12)),
    g.id,
    p.id,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system'
FROM idp_groups g
CROSS JOIN idp_permission_definitions p
WHERE (g.is_super_admin = TRUE OR g.code = 'ROLE_ADMIN')
  AND p.code IN ('USER_EXPORT', 'DEPARTMENT_EXPORT', 'AUDIT_LOG_EXPORT', 'EXPORT_JOB_VIEW_ALL', 'EXPORT_JOB_VIEW')
ON CONFLICT (group_id, permission_id) DO NOTHING;

-- Grant export view permission to all active roles/groups.
INSERT INTO idp_role_permissions (id, group_id, permission_id, status, effective_from, created_at, created_by, updated_at, updated_by)
SELECT
    LOWER(SUBSTRING(md5(g.id || ':' || p.id) FROM 1 FOR 8) || '-' ||
          SUBSTRING(md5(g.id || ':' || p.id) FROM 9 FOR 4) || '-' ||
          SUBSTRING(md5(g.id || ':' || p.id) FROM 13 FOR 4) || '-' ||
          SUBSTRING(md5(g.id || ':' || p.id) FROM 17 FOR 4) || '-' ||
          SUBSTRING(md5(g.id || ':' || p.id) FROM 21 FOR 12)),
    g.id,
    p.id,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP,
    'system'
FROM idp_groups g
CROSS JOIN idp_permission_definitions p
WHERE g.status = 'ACTIVE'
  AND p.code = 'EXPORT_JOB_VIEW'
ON CONFLICT (group_id, permission_id) DO NOTHING;
