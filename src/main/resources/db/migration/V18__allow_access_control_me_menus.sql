-- Cho phep nguoi dung da dang nhap lay danh sach menu duoc phan quyen cua chinh minh
INSERT INTO idp_permission_apis (
    id, permission_id, http_method, uri_pattern, match_type, service_code, status, priority, is_allow, description, created_at, updated_at
)
SELECT 
    'f1a80001-2222-4444-8888-000000000001',
    p.id,
    'GET',
    '/api/v1/access-control/me/menus',
    'EXACT',
    'LEDGER_SERVICE',
    'ACTIVE',
    10,
    TRUE,
    'Get authorized menus for current user',
    NOW(),
    NOW()
FROM idp_permission_definitions p
WHERE p.code = 'COMMON_SYSTEM' OR p.code = 'USER_READ'
LIMIT 1
ON CONFLICT ON CONSTRAINT uk_idp_permission_apis_permission_http_uri_service DO NOTHING;
