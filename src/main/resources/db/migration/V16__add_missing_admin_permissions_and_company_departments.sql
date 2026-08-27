CREATE TABLE IF NOT EXISTS companies (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    description VARCHAR(500),
    name VARCHAR(100),
    code VARCHAR(50) UNIQUE,
    short_name VARCHAR(50),
    unit_level VARCHAR(50),
    parent_id VARCHAR(36),
    company_type VARCHAR(50),
    sort_level INTEGER,
    status VARCHAR(20),
    CONSTRAINT parent_id_sort_level_unique UNIQUE (parent_id, sort_level)
);

CREATE TABLE IF NOT EXISTS org_company_departments (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    description VARCHAR(500),
    company_id VARCHAR(36) NOT NULL REFERENCES companies(id),
    department_id VARCHAR(36) NOT NULL REFERENCES org_departments(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uk_org_company_departments_company_department UNIQUE (company_id, department_id)
);

CREATE INDEX IF NOT EXISTS idx_org_company_departments_company_id ON org_company_departments(company_id);
CREATE INDEX IF NOT EXISTS idx_org_company_departments_department_id ON org_company_departments(department_id);
CREATE INDEX IF NOT EXISTS idx_org_company_departments_status ON org_company_departments(status);

INSERT INTO idp_permission_definitions (id, code, name, module_code, action_code, resource_type, status, description, created_at, created_by, updated_at, updated_by)
VALUES
  ('8f2aa001-1111-4444-8888-000000000001', 'USER_DELETE', 'Xóa ngu?i dùng', 'USER', 'DELETE', 'USER', 'ACTIVE', 'Cho phép xóa m?m ngu?i dùng', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
  ('8f2aa001-1111-4444-8888-000000000002', 'DEPARTMENT_CREATE', 'T?o phòng ban', 'DEPARTMENT', 'CREATE', 'DEPARTMENT', 'ACTIVE', 'Cho phép t?o phòng ban', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
  ('8f2aa001-1111-4444-8888-000000000003', 'DEPARTMENT_UPDATE', 'C?p nh?t phòng ban', 'DEPARTMENT', 'UPDATE', 'DEPARTMENT', 'ACTIVE', 'Cho phép c?p nh?t phòng ban', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
  ('8f2aa001-1111-4444-8888-000000000004', 'ROLE_CREATE', 'T?o nhóm quy?n', 'ROLE', 'CREATE', 'ROLE', 'ACTIVE', 'Cho phép t?o nhóm quy?n', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
  ('8f2aa001-1111-4444-8888-000000000005', 'ROLE_DELETE', 'Xóa nhóm quy?n', 'ROLE', 'DELETE', 'ROLE', 'ACTIVE', 'Cho phép vô hi?u hóa nhóm quy?n', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
  ('8f2aa001-1111-4444-8888-000000000006', 'ORGANIZATION_ASSIGN_DEPARTMENT', 'Gán phòng ban cho công ty', 'ORGANIZATION', 'ASSIGN_DEPARTMENT', 'ORGANIZATION', 'ACTIVE', 'Cho phép gán phòng ban cho công ty', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
  ('8f2aa001-1111-4444-8888-000000000007', 'EXPORT_JOB_VIEW', 'Xem job export', 'EXPORT', 'VIEW', 'EXPORT_JOB', 'ACTIVE', 'Cho phép xem job export c?a b?n thân', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
  ('8f2aa001-1111-4444-8888-000000000008', 'EXPORT_JOB_CANCEL', 'H?y job export', 'EXPORT', 'CANCEL', 'EXPORT_JOB', 'ACTIVE', 'Cho phép h?y job export', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'),
  ('8f2aa001-1111-4444-8888-000000000009', 'EXPORT_JOB_VIEW_ALL', 'Xem m?i job export', 'EXPORT', 'VIEW_ALL', 'EXPORT_JOB', 'ACTIVE', 'Cho phép xem job export c?a ngu?i khác', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system')
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    module_code = EXCLUDED.module_code,
    action_code = EXCLUDED.action_code,
    resource_type = EXCLUDED.resource_type,
    status = EXCLUDED.status,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

DELETE FROM idp_role_permissions rp
USING idp_permission_definitions p
WHERE rp.permission_id = p.id
  AND p.code IN ('USER_EXPORT', 'DEPARTMENT_EXPORT', 'AUDIT_LOG_EXPORT', 'EXPORT_JOB_VIEW', 'EXPORT_JOB_VIEW_ALL')
  AND rp.created_by = 'system';

INSERT INTO idp_permission_apis (id, permission_id, http_method, uri_pattern, match_type, service_code, status, priority, is_allow, description, created_at, created_by, updated_at, updated_by)
SELECT *
FROM (
    SELECT '8f2ab001-1111-4444-8888-000000000001' AS id, p.id AS permission_id, 'DELETE' AS http_method, '/api/v1/admin/users/*' AS uri_pattern, 'ANT_PATH' AS match_type, 'LEDGER_SERVICE' AS service_code, 'ACTIVE' AS status, 80 AS priority, FALSE AS is_allow, 'Delete user API' AS description, CURRENT_TIMESTAMP AS created_at, 'system' AS created_by, CURRENT_TIMESTAMP AS updated_at, 'system' AS updated_by
    FROM idp_permission_definitions p WHERE p.code = 'USER_DELETE'
    UNION ALL
    SELECT '8f2ab001-1111-4444-8888-000000000002', p.id, 'POST', '/api/v1/admin/departments', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Create department API', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'
    FROM idp_permission_definitions p WHERE p.code = 'DEPARTMENT_CREATE'
    UNION ALL
    SELECT '8f2ab001-1111-4444-8888-000000000003', p.id, 'PUT', '/api/v1/admin/departments/*', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Update department API', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'
    FROM idp_permission_definitions p WHERE p.code = 'DEPARTMENT_UPDATE'
    UNION ALL
    SELECT '8f2ab001-1111-4444-8888-000000000004', p.id, 'POST', '/api/v1/admin/rbac/roles', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Create role API', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'
    FROM idp_permission_definitions p WHERE p.code = 'ROLE_CREATE'
    UNION ALL
    SELECT '8f2ab001-1111-4444-8888-000000000005', p.id, 'DELETE', '/api/v1/admin/rbac/roles/*', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Delete role API', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'
    FROM idp_permission_definitions p WHERE p.code = 'ROLE_DELETE'
    UNION ALL
    SELECT '8f2ab001-1111-4444-8888-000000000006', p.id, 'PUT', '/api/v1/admin/companies/*/departments', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Assign company departments API', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'
    FROM idp_permission_definitions p WHERE p.code = 'ORGANIZATION_ASSIGN_DEPARTMENT'
    UNION ALL
    SELECT '8f2ab001-1111-4444-8888-000000000007', p.id, 'GET', '/api/v1/exports', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Search export jobs API', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'
    FROM idp_permission_definitions p WHERE p.code = 'EXPORT_JOB_VIEW'
    UNION ALL
    SELECT '8f2ab001-1111-4444-8888-000000000008', p.id, 'GET', '/api/v1/exports/*', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Get export job status API', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'
    FROM idp_permission_definitions p WHERE p.code = 'EXPORT_JOB_VIEW'
    UNION ALL
    SELECT '8f2ab001-1111-4444-8888-000000000009', p.id, 'GET', '/api/v1/exports/*/download', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Download export job file API', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'
    FROM idp_permission_definitions p WHERE p.code = 'EXPORT_JOB_VIEW'
    UNION ALL
    SELECT '8f2ab001-1111-4444-8888-000000000010', p.id, 'POST', '/api/v1/exports/*/cancel', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Cancel export job API', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'
    FROM idp_permission_definitions p WHERE p.code = 'EXPORT_JOB_CANCEL'
) seed
ON CONFLICT (permission_id, http_method, uri_pattern, service_code) DO UPDATE
SET match_type = EXCLUDED.match_type,
    status = EXCLUDED.status,
    priority = EXCLUDED.priority,
    is_allow = EXCLUDED.is_allow,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

INSERT INTO sys_menu_permissions (id, menu_id, permission_id, display_action, status, created_at, created_by, updated_at, updated_by)
SELECT md5(m.code || ':' || p.code), m.id, p.id, p.action_code, 'ACTIVE', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system'
FROM sys_menus m
JOIN idp_permission_definitions p ON (m.code = 'USER_MANAGEMENT' AND p.code = 'USER_DELETE')
   OR (m.code = 'DEPARTMENT_MANAGEMENT' AND p.code IN ('DEPARTMENT_CREATE', 'DEPARTMENT_UPDATE'))
   OR (m.code = 'ROLE_MANAGEMENT' AND p.code IN ('ROLE_CREATE', 'ROLE_DELETE'))
   OR (m.code = 'ORGANIZATION_MANAGEMENT' AND p.code = 'ORGANIZATION_ASSIGN_DEPARTMENT')
ON CONFLICT (menu_id, permission_id) DO UPDATE
SET display_action = EXCLUDED.display_action,
    status = EXCLUDED.status,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';
