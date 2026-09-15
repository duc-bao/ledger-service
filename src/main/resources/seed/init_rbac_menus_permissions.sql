-- ====================================================================================================
-- FILE KHỞI TẠO DỮ LIỆU TĨNH RBAC & MENU TOÀN DIỆN CHO DỰ ÁN (SEED / INIT SCRIPT)
-- HỆ THỐNG: ledger-service (Spring Boot 3, Java 21, PostgreSQL 16)
-- ĐỐI TƯỢNG PHỤC VỤ: Chạy khởi tạo ban đầu khi bàn giao hoặc dựng môi trường mới cho Khách hàng
-- ĐẶC TÍNH: Idempotent (Chạy lại nhiều lần không lỗi nhờ ON CONFLICT DO UPDATE / DO NOTHING)
-- ====================================================================================================

-- ----------------------------------------------------------------------------------------------------
-- PHẦN 1: BẢNG MENU HỆ THỐNG (sys_menus)
-- Khởi tạo cây Menu Động chuẩn xác theo đúng bản thiết kế Figma Dashboard và Phân quyền Sidebar
-- ----------------------------------------------------------------------------------------------------

-- 1.1. Các Module / Menu Cha (Root Nodes - parent_id IS NULL)
INSERT INTO sys_menus (
    id, code, name, menu_type, path, icon, component, menu_offset, parent_id, status, visible, description, created_at, updated_at, created_by, updated_by
)
VALUES
    ('710533dd-879d-d120-2e5c-73b27705bf02', 'HOME', 'Trang chủ', 'MENU', '/home', 'fas fa-home', NULL, 10, NULL, 'ACTIVE', TRUE, 'Màn hình Trang chủ tổng quan', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('39458698-b036-4cdc-b701-2514892f9803', 'DASHBOARD', 'Báo cáo & Thống kê', 'MODULE', '/dashboard', 'fas fa-chart-pie', NULL, 20, NULL, 'ACTIVE', TRUE, 'Nhóm báo cáo phân tích và thống kê số liệu', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('ead79718-9ff1-c2d9-35e5-39e9d65fe041', 'PRODUCT_MANAGEMENT', 'Quản lý sản phẩm', 'MODULE', '/products', 'fas fa-boxes-stacked', NULL, 30, NULL, 'ACTIVE', TRUE, 'Nhóm nghiệp vụ quản trị danh mục sản phẩm', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('a2c0909b-cb10-3d3f-edf0-9b57ac14c897', 'ACTIVITY_LOG', 'Nhật ký hoạt động', 'MENU', '/admin/audit-logs', 'fas fa-clock-rotate-left', NULL, 40, NULL, 'ACTIVE', TRUE, 'Tra cứu vết nhật ký thao tác người dùng trên hệ thống', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('3e3d9fde-19aa-501b-c5d0-e208931a84c1', 'ACCESS_MANAGEMENT', 'Người dùng & phân quyền', 'MODULE', '/admin', 'fas fa-users-gear', NULL, 50, NULL, 'ACTIVE', TRUE, 'Quản lý tài khoản, phòng ban, vai trò và phân quyền ma trận', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('25ede29f-7c49-6828-2294-809e23e17020', 'SYSTEM_CONFIGURATION', 'Cấu hình hệ thống', 'MODULE', '/settings', 'fas fa-gears', NULL, 60, NULL, 'ACTIVE', TRUE, 'Cấu hình tham số hệ thống và tích hợp dịch vụ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system')
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    menu_type = EXCLUDED.menu_type,
    path = EXCLUDED.path,
    icon = EXCLUDED.icon,
    menu_offset = EXCLUDED.menu_offset,
    status = EXCLUDED.status,
    visible = EXCLUDED.visible,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

-- 1.2. Các Menu Con (Child Nodes - Liên kết với Menu Cha qua mã code)
INSERT INTO sys_menus (
    id, code, name, menu_type, path, icon, component, menu_offset, parent_id, status, visible, description, created_at, updated_at, created_by, updated_by
)
SELECT
    m.id, m.code, m.name, m.menu_type, m.path, m.icon, m.component, m.menu_offset, p.id AS parent_id, m.status, m.visible, m.description, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'
FROM (
    VALUES
        -- Con của DASHBOARD (Báo cáo & Thống kê)
        ('465ac3a5-7e4c-1c96-5d3b-bfb088d27dd2', 'DASHBOARD_REVENUE', 'Dashboard doanh thu', 'MENU', '/dashboard/revenue', 'fas fa-chart-line', NULL, 10, 'DASHBOARD', 'ACTIVE', TRUE, 'Báo cáo trực quan biến động doanh thu theo chu kỳ'),
        ('3cf4b308-a79c-da1f-246e-49dc4231924c', 'DASHBOARD_VOLUME', 'Dashboard sản lượng', 'MENU', '/dashboard/volume', 'fas fa-cubes', NULL, 20, 'DASHBOARD', 'ACTIVE', TRUE, 'Báo cáo chỉ số sản lượng giao dịch'),
        ('419f7dc5-8025-20c2-0c8b-1c1907b6b8ae', 'DASHBOARD_DEBT', 'Dashboard công nợ', 'MENU', '/dashboard/debt', 'fas fa-file-invoice-dollar', NULL, 30, 'DASHBOARD', 'ACTIVE', TRUE, 'Báo cáo tổng hợp số dư và hạn mức công nợ'),
        ('07aa8094-47d4-163d-07c2-4ae8d7520054', 'AI_INSIGHT', 'AI Insight', 'MENU', '/dashboard/ai-insight', 'fas fa-brain', NULL, 40, 'DASHBOARD', 'ACTIVE', TRUE, 'Phân tích dự báo thông minh ứng dụng AI'),

        -- Con của PRODUCT_MANAGEMENT (Quản lý sản phẩm)
        ('532c2fbb-3aab-de9a-23b6-53762c0e77f6', 'PRODUCT_MANUAL', 'Sản phẩm thủ công', 'MENU', '/products/manual', 'fas fa-hand-holding-dollar', NULL, 10, 'PRODUCT_MANAGEMENT', 'ACTIVE', TRUE, 'Tạo và quản lý thông tin sản phẩm thủ công'),
        ('c6a0d7d8-2774-a6fd-c56c-42a246de20b0', 'PRODUCT_EXTERNAL', 'Lấy dữ liệu hệ thống ngoài', 'MENU', '/products/external', 'fas fa-cloud-arrow-down', NULL, 20, 'PRODUCT_MANAGEMENT', 'ACTIVE', TRUE, 'Tra cứu và kéo dữ liệu sản phẩm từ đối tác'),
        ('f6ecc1e8-4882-4bf2-5fe1-de46d3843e6a', 'PRODUCT_SYNC', 'Đồng bộ dữ liệu sản phẩm', 'MENU', '/products/sync', 'fas fa-arrows-rotate', NULL, 30, 'PRODUCT_MANAGEMENT', 'ACTIVE', TRUE, 'Thực thi và giám sát tác vụ đồng bộ sản phẩm'),
        ('8087cc78-ca96-f2d9-d8a7-a46cad99f0f9', 'PRODUCT_IMPORT', 'Import dữ liệu sản phẩm', 'MENU', '/products/import', 'fas fa-file-import', NULL, 40, 'PRODUCT_MANAGEMENT', 'ACTIVE', TRUE, 'Nhập danh sách sản phẩm hàng loạt từ file Excel'),

        -- Con của ACCESS_MANAGEMENT (Người dùng & phân quyền)
        ('5b29f8ed-be95-ef9d-3198-b771b601833e', 'USER_MANAGEMENT', 'Quản lý người dùng', 'MENU', '/admin/users', 'fas fa-user', NULL, 10, 'ACCESS_MANAGEMENT', 'ACTIVE', TRUE, 'Quản lý danh sách tài khoản, hồ sơ và trạng thái người dùng'),
        ('a1d4b851-8c45-6156-2dcf-41641031d93a', 'DEPARTMENT_MANAGEMENT', 'Quản lý phòng ban', 'MENU', '/admin/departments', 'fas fa-sitemap', NULL, 20, 'ACCESS_MANAGEMENT', 'ACTIVE', TRUE, 'Quản lý cơ cấu phòng ban và phân bổ nhân sự'),
        ('3fe67a81-2744-8298-e2d7-2e73c0b8f74c', 'ROLE_MANAGEMENT', 'Quản lý nhóm quyền', 'MENU', '/admin/roles', 'fas fa-shield-halved', NULL, 30, 'ACCESS_MANAGEMENT', 'ACTIVE', TRUE, 'Quản lý danh mục vai trò và ma trận phân quyền RBAC'),
        ('4ccc6480-a20c-e123-f7db-b87785357fe2', 'ORGANIZATION_MANAGEMENT', 'Quản lý công ty / tổ chức', 'MENU', '/admin/organizations', 'fas fa-building', NULL, 40, 'ACCESS_MANAGEMENT', 'ACTIVE', TRUE, 'Quản lý mô hình công ty mẹ - chi nhánh và công ty con'),

        -- Con của SYSTEM_CONFIGURATION (Cấu hình hệ thống)
        ('3692ceee-3980-b865-eba3-050140ece543', 'EMAIL_GATEWAY', 'Cấu hình Email Gateway', 'MENU', '/settings/email-gateway', 'fas fa-envelope-circle-check', NULL, 10, 'SYSTEM_CONFIGURATION', 'ACTIVE', TRUE, 'Thiết lập máy chủ gửi mail thông báo và mã xác thực OTP')
) AS m(id, code, name, menu_type, path, icon, component, menu_offset, parent_code, status, visible, description)
JOIN sys_menus p ON p.code = m.parent_code
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    menu_type = EXCLUDED.menu_type,
    path = EXCLUDED.path,
    icon = EXCLUDED.icon,
    menu_offset = EXCLUDED.menu_offset,
    parent_id = EXCLUDED.parent_id,
    status = EXCLUDED.status,
    visible = EXCLUDED.visible,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';


-- ----------------------------------------------------------------------------------------------------
-- PHẦN 2: BẢNG ĐỊNH NGHĨA QUYỀN NGUYÊN TỬ (idp_permission_definitions)
-- Danh mục toàn bộ các Permission Code theo chuẩn RBAC tương ứng với Ma trận phân quyền Figma
-- ----------------------------------------------------------------------------------------------------

INSERT INTO idp_permission_definitions (
    id, code, name, module_code, action_code, resource_type, status, description, created_at, updated_at, created_by, updated_by
)
VALUES
    -- Phân hệ TRANG CHỦ & DÙNG CHUNG (HOME & COMMON)
    ('8f2aa001-0000-0000-0000-000000000000', 'HOME_VIEW', 'Xem trang chủ', 'HOME', 'VIEW', 'HOME', 'ACTIVE', 'Cho phép truy cập và xem dashboard trang chủ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('8f2aa001-0000-0000-0000-000000000099', 'COMMON_SYSTEM', 'Quyền chung của hệ thống', 'COMMON_SYSTEM', 'SEARCH', 'SYSTEM', 'ACTIVE', 'Quyền dùng chung cho toàn bộ tài khoản sau khi đăng nhập thành công', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),

    -- Phân hệ QUẢN LÝ NGƯỜI DÙNG (USER)
    ('8f2aa001-0000-0000-0000-000000000001', 'USER_VIEW', 'Xem danh sách và chi tiết người dùng', 'USER', 'VIEW', 'USER', 'ACTIVE', 'Cho phép tìm kiếm và xem thông tin chi tiết người dùng', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('8f2aa001-0000-0000-0000-000000000002', 'USER_CREATE', 'Tạo mới người dùng', 'USER', 'CREATE', 'USER', 'ACTIVE', 'Cho phép tạo mới tài khoản người dùng và gán vai trò ban đầu', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('8f2aa001-0000-0000-0000-000000000003', 'USER_UPDATE', 'Cập nhật người dùng', 'USER', 'UPDATE', 'USER', 'ACTIVE', 'Cho phép chỉnh sửa thông tin người dùng và đặt lại mật khẩu tạm thời', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('8f2aa001-1111-4444-8888-000000000001', 'USER_DELETE', 'Xóa người dùng', 'USER', 'DELETE', 'USER', 'ACTIVE', 'Cho phép xóa tài khoản người dùng đã ngừng hoạt động (kèm lý do)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('8f2aa001-0000-0000-0000-000000000004', 'USER_LOCK', 'Khóa / Mở khóa người dùng', 'USER', 'LOCK', 'USER', 'ACTIVE', 'Cho phép tạm khóa hoặc mở khóa tài khoản người dùng', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('d64e031a-7b3b-48ae-8a4e-0a56acbb0001', 'USER_EXPORT', 'Xuất Excel danh sách người dùng', 'USER', 'EXPORT', 'USER', 'ACTIVE', 'Cho phép xuất dữ liệu danh sách người dùng ra file Excel', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('8f2aa001-0000-0000-0000-000000000005', 'USER_ASSIGN_ROLE', 'Gán vai trò cho người dùng', 'USER', 'ASSIGN_ROLE', 'USER', 'ACTIVE', 'Cho phép phân bổ vai trò trực tiếp cho tài khoản', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),

    -- Phân hệ QUẢN LÝ PHÒNG BAN (DEPARTMENT)
    ('b11800e5-b892-f9d1-4dea-60ff82288766', 'DEPARTMENT_VIEW', 'Xem danh sách phòng ban', 'DEPARTMENT', 'VIEW', 'DEPARTMENT', 'ACTIVE', 'Cho phép xem cây danh mục và thông tin phòng ban', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('8f2aa001-1111-4444-8888-000000000002', 'DEPARTMENT_CREATE', 'Tạo mới phòng ban', 'DEPARTMENT', 'CREATE', 'DEPARTMENT', 'ACTIVE', 'Cho phép thành lập mới phòng ban trong tổ chức', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('8f2aa001-1111-4444-8888-000000000003', 'DEPARTMENT_UPDATE', 'Cập nhật phòng ban', 'DEPARTMENT', 'UPDATE', 'DEPARTMENT', 'ACTIVE', 'Cho phép chỉnh sửa thông tin phòng ban', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('d64e031a-7b3b-48ae-8a4e-0a56acbb0002', 'DEPARTMENT_EXPORT', 'Xuất Excel danh sách phòng ban', 'DEPARTMENT', 'EXPORT', 'DEPARTMENT', 'ACTIVE', 'Cho phép xuất dữ liệu danh mục phòng ban', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('3ff16c3e-e8e1-4ff5-9868-ee666f852398', 'DEPARTMENT_ASSIGN_ROLE', 'Gán vai trò phòng ban', 'DEPARTMENT', 'ASSIGN_ROLE', 'DEPARTMENT', 'ACTIVE', 'Cho phép phân quyền thành viên trong nội bộ phòng ban', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),

    -- Phân hệ QUẢN LÝ NHÓM QUYỀN / VAI TRÒ (ROLE)
    ('37bfc02d-5701-455f-bd28-c391c2cca605', 'ROLE_VIEW', 'Xem danh sách nhóm quyền', 'ROLE', 'VIEW', 'ROLE', 'ACTIVE', 'Cho phép xem danh sách vai trò và ma trận phân quyền', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('8f2aa001-1111-4444-8888-000000000004', 'ROLE_CREATE', 'Tạo mới nhóm quyền', 'ROLE', 'CREATE', 'ROLE', 'ACTIVE', 'Cho phép định nghĩa vai trò người dùng mới', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('d386612c-20ba-4f5e-8342-814693652fc5', 'ROLE_UPDATE', 'Cập nhật phân quyền vai trò', 'ROLE', 'UPDATE', 'ROLE', 'ACTIVE', 'Cho phép chỉnh sửa thông tin vai trò và thay thế ma trận quyền', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('8f2aa001-1111-4444-8888-000000000005', 'ROLE_DELETE', 'Xóa nhóm quyền', 'ROLE', 'DELETE', 'ROLE', 'ACTIVE', 'Cho phép xóa vai trò (chặn xóa nếu vai trò đang có người dùng)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('66c15fb7-c12f-4c44-a479-5623bf4533f1', 'ROLE_ASSIGN', 'Phân bổ vai trò hệ thống', 'ROLE', 'ASSIGN', 'ROLE', 'ACTIVE', 'Cho phép gán vai trò phân quyền', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),

    -- Phân hệ CÔNG TY / CƠ CẤU TỔ CHỨC (ORGANIZATION)
    ('deca1d97-ca49-eccf-a107-bf3dfa2930b1', 'ORGANIZATION_VIEW', 'Xem công ty và công ty con', 'ORGANIZATION', 'VIEW', 'ORGANIZATION', 'ACTIVE', 'Cho phép tra cứu chi tiết công ty và chi nhánh', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('7ef53cc6-7d23-5c0b-9c2f-f9aadac64828', 'ORGANIZATION_VIEW_TREE', 'Xem cây cơ cấu tổ chức', 'ORGANIZATION', 'VIEW_TREE', 'ORGANIZATION', 'ACTIVE', 'Cho phép xem sơ đồ cây phân cấp công ty toàn hệ thống', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('7cec4d53-62e7-26c4-5e15-0b46a6f6b19b', 'ORGANIZATION_CREATE', 'Tạo công ty / chi nhánh', 'ORGANIZATION', 'CREATE', 'ORGANIZATION', 'ACTIVE', 'Cho phép thiết lập công ty mới', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('723def06-e486-289b-208e-5870eab3c853', 'ORGANIZATION_UPDATE', 'Cập nhật thông tin công ty', 'ORGANIZATION', 'UPDATE', 'ORGANIZATION', 'ACTIVE', 'Cho phép sửa thông tin định danh công ty', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('d9b1a1d9-3fdb-1ce0-3992-eb2cbc71e5f3', 'ORGANIZATION_CHANGE_STATUS', 'Đổi trạng thái công ty', 'ORGANIZATION', 'CHANGE_STATUS', 'ORGANIZATION', 'ACTIVE', 'Cho phép kích hoạt hoặc tạm dừng hoạt động của đơn vị', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('8f2aa001-1111-4444-8888-000000000006', 'ORGANIZATION_ASSIGN_DEPARTMENT', 'Gán phòng ban cho công ty', 'ORGANIZATION', 'ASSIGN_DEPARTMENT', 'ORGANIZATION', 'ACTIVE', 'Cho phép liên kết phòng ban vào đơn vị trực thuộc', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('6b5067d2-d358-a4e2-b402-14203bb26d3d', 'ORGANIZATION_ASSIGN_ROLE', 'Gán vai trò cấp công ty', 'ORGANIZATION', 'ASSIGN_ROLE', 'ORGANIZATION', 'ACTIVE', 'Cho phép phân quyền tài khoản theo đơn vị công ty', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),

    -- Phân hệ NHẬT KÝ HOẠT ĐỘNG (ACTIVITY_LOG)
    ('a2c0909b-cb10-3d3f-edf0-9b57ac14c898', 'ACTIVITY_LOG_VIEW', 'Xem nhật ký hoạt động', 'ACTIVITY_LOG', 'VIEW', 'ACTIVITY_LOG', 'ACTIVE', 'Cho phép tra cứu và xem lịch sử thao tác của người dùng', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('a2c0909b-cb10-3d3f-edf0-9b57ac14c899', 'ACTIVITY_LOG_EXPORT', 'Xuất Excel nhật ký hoạt động', 'ACTIVITY_LOG', 'EXPORT', 'ACTIVITY_LOG', 'ACTIVE', 'Cho phép trích xuất lịch sử thao tác ra file Excel', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),

    -- Phân hệ BÁO CÁO & THỐNG KÊ (DASHBOARD)
    ('39458698-b036-4cdc-b701-2514892f9804', 'DASHBOARD_READ', 'Xem Báo cáo & Thống kê', 'DASHBOARD', 'READ', 'DASHBOARD', 'ACTIVE', 'Cho phép truy cập tổng quan nhóm chức năng báo cáo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('9e005b70-1dcd-6fc2-6abb-e3c0d23bec22', 'DASHBOARD_REVENUE_VIEW', 'Xem dashboard doanh thu', 'DASHBOARD_REVENUE', 'VIEW', 'DASHBOARD_REVENUE', 'ACTIVE', 'Cho phép xem biểu đồ doanh thu chi tiết', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('97592521-7482-4f85-b510-402bd587d664', 'DASHBOARD_REVENUE_EXPORT_EXCEL', 'Xuất Excel dashboard doanh thu', 'DASHBOARD_REVENUE', 'EXPORT_EXCEL', 'DASHBOARD_REVENUE', 'ACTIVE', 'Cho phép xuất dữ liệu doanh thu ra file Excel', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('ddcca5b9-d13a-6272-2fb6-f7c439ec4b2f', 'DASHBOARD_REVENUE_EXPORT_PDF', 'Xuất PDF dashboard doanh thu', 'DASHBOARD_REVENUE', 'EXPORT_PDF', 'DASHBOARD_REVENUE', 'ACTIVE', 'Cho phép in và xuất báo cáo doanh thu ra PDF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('bc023950-b272-37b1-f66a-ea848031ad22', 'DASHBOARD_VOLUME_VIEW', 'Xem dashboard sản lượng', 'DASHBOARD_VOLUME', 'VIEW', 'DASHBOARD_VOLUME', 'ACTIVE', 'Cho phép xem biểu đồ sản lượng sản phẩm', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('87f3f660-b70e-64b2-2b39-9cba6128a1b9', 'DASHBOARD_VOLUME_EXPORT_EXCEL', 'Xuất Excel dashboard sản lượng', 'DASHBOARD_VOLUME', 'EXPORT_EXCEL', 'DASHBOARD_VOLUME', 'ACTIVE', 'Cho phép xuất dữ liệu sản lượng ra file Excel', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('b973d822-8433-b09c-da74-8ab0305167c0', 'DASHBOARD_VOLUME_EXPORT_PDF', 'Xuất PDF dashboard sản lượng', 'DASHBOARD_VOLUME', 'EXPORT_PDF', 'DASHBOARD_VOLUME', 'ACTIVE', 'Cho phép xuất báo cáo sản lượng ra PDF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('7be6d2de-deda-66fc-456b-8266245358ad', 'DASHBOARD_DEBT_VIEW', 'Xem dashboard công nợ', 'DASHBOARD_DEBT', 'VIEW', 'DASHBOARD_DEBT', 'ACTIVE', 'Cho phép xem biểu đồ và bảng theo dõi công nợ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('47893018-ad75-63cd-27e4-d83b3661bc24', 'DASHBOARD_DEBT_EXPORT_EXCEL', 'Xuất Excel dashboard công nợ', 'DASHBOARD_DEBT', 'EXPORT_EXCEL', 'DASHBOARD_DEBT', 'ACTIVE', 'Cho phép xuất báo cáo công nợ ra file Excel', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('ff261fc0-bccb-b098-db00-1c2c102043e9', 'DASHBOARD_DEBT_EXPORT_PDF', 'Xuất PDF dashboard công nợ', 'DASHBOARD_DEBT', 'EXPORT_PDF', 'DASHBOARD_DEBT', 'ACTIVE', 'Cho phép xuất báo cáo công nợ ra PDF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('07aa8094-47d4-163d-07c2-4ae8d7520055', 'AI_INSIGHT_VIEW', 'Xem AI Insight', 'AI_INSIGHT', 'VIEW', 'AI_INSIGHT', 'ACTIVE', 'Cho phép truy cập các phân tích xu hướng và dự báo từ AI', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('07aa8094-47d4-163d-07c2-4ae8d7520056', 'AI_INSIGHT_EXPORT', 'Xuất báo cáo AI Insight', 'AI_INSIGHT', 'EXPORT', 'AI_INSIGHT', 'ACTIVE', 'Cho phép trích xuất kết quả dự báo AI ra file', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),

    -- Phân hệ QUẢN LÝ SẢN PHẨM (PRODUCT)
    ('ecabbdf3-7116-aca7-bb13-653e38a0ae83', 'PRODUCT_VIEW', 'Xem danh mục sản phẩm', 'PRODUCT', 'VIEW', 'PRODUCT', 'ACTIVE', 'Cho phép tra cứu danh sách sản phẩm', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('bdc49a9c-f6c1-030c-f1a5-77d8759b7c19', 'PRODUCT_CREATE', 'Tạo sản phẩm thủ công', 'PRODUCT', 'CREATE', 'PRODUCT', 'ACTIVE', 'Cho phép tạo mới sản phẩm thủ công', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('bc2caf94-f9e3-d379-c670-3909e3271468', 'PRODUCT_UPDATE', 'Cập nhật sản phẩm', 'PRODUCT', 'UPDATE', 'PRODUCT', 'ACTIVE', 'Cho phép chỉnh sửa thông tin sản phẩm', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('18558c02-ca17-5fbc-a904-dfd8b61a56c6', 'PRODUCT_DELETE', 'Xóa sản phẩm', 'PRODUCT', 'DELETE', 'PRODUCT', 'ACTIVE', 'Cho phép gỡ bỏ sản phẩm khỏi danh mục', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('111647d5-f2fa-7234-17c3-d23040f32ef4', 'PRODUCT_EXTERNAL_FETCH', 'Lấy dữ liệu từ hệ thống ngoài', 'PRODUCT', 'FETCH', 'PRODUCT', 'ACTIVE', 'Cho phép truy vấn dữ liệu sản phẩm đối tác', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('cf3a9c1f-d20d-a322-4a59-d35adbd2e8d9', 'PRODUCT_HISTORY_VIEW', 'Xem lịch sử sản phẩm', 'PRODUCT', 'HISTORY_VIEW', 'PRODUCT', 'ACTIVE', 'Cho phép tra cứu vòng đời thay đổi của sản phẩm', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('8087cc78-ca96-f2d9-d8a7-a46cad99f0fa', 'PRODUCT_IMPORT', 'Import dữ liệu sản phẩm', 'PRODUCT', 'IMPORT', 'PRODUCT', 'ACTIVE', 'Cho phép nhập dữ liệu sản phẩm từ file bảng tính Excel', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('f6ecc1e8-4882-4bf2-5fe1-de46d3843e6b', 'PRODUCT_SYNC', 'Đồng bộ dữ liệu sản phẩm', 'PRODUCT', 'SYNC', 'PRODUCT', 'ACTIVE', 'Cho phép chạy tác vụ đồng bộ sản phẩm tự động', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),

    -- Phân hệ CẤU HÌNH HỆ THỐNG & EMAIL (EMAIL_GATEWAY)
    ('e238736d-ea79-64b3-8586-3e4cbd8b1175', 'EMAIL_GATEWAY_VIEW', 'Xem cấu hình Email Gateway', 'EMAIL_GATEWAY', 'VIEW', 'EMAIL_GATEWAY', 'ACTIVE', 'Cho phép xem thông số máy chủ gửi email', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('e0419427-a4e6-1c51-999b-260c35c4a184', 'EMAIL_GATEWAY_UPDATE', 'Cập nhật cấu hình Email Gateway', 'EMAIL_GATEWAY', 'UPDATE', 'EMAIL_GATEWAY', 'ACTIVE', 'Cho phép cập nhật thông số SMTP server gửi email', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),

    -- Phân hệ TIẾN TRÌNH XUẤT DỮ LIỆU (EXPORT)
    ('d64e031a-7b3b-48ae-8a4e-0a56acbb0005', 'EXPORT_JOB_VIEW', 'Xem job export của bản thân', 'EXPORT', 'VIEW', 'EXPORT_JOB', 'ACTIVE', 'Cho phép kiểm tra tiến độ và tải file export cá nhân', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('d64e031a-7b3b-48ae-8a4e-0a56acbb0004', 'EXPORT_JOB_VIEW_ALL', 'Xem mọi job export', 'EXPORT', 'VIEW_ALL', 'EXPORT_JOB', 'ACTIVE', 'Cho phép quản trị viên xem và tải file export của toàn bộ người dùng', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('8f2aa001-1111-4444-8888-000000000008', 'EXPORT_JOB_CANCEL', 'Hủy job export', 'EXPORT', 'CANCEL', 'EXPORT_JOB', 'ACTIVE', 'Cho phép hủy tác vụ xuất file đang xử lý trong hàng đợi', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),

    -- Phân hệ QUẢN TRỊ NÂNG CAO (RBAC & MENU - Kỹ thuật)
    ('6deb626f-251f-e327-c598-d97713e3fb56', 'PERMISSION_VIEW', 'Xem danh mục định nghĩa quyền', 'RBAC', 'VIEW', 'PERMISSION', 'ACTIVE', 'Cho phép tra cứu danh sách quyền và API mapping', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('83f196bd-8ece-c61e-7813-19f75b0231fa', 'PERMISSION_CREATE', 'Tạo định nghĩa quyền mới', 'RBAC', 'CREATE', 'PERMISSION', 'ACTIVE', 'Cho phép tạo quyền nguyên tử mới', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('4634460d-7344-cc33-a432-11ca6215f4f3', 'PERMISSION_UPDATE', 'Cập nhật định nghĩa quyền', 'RBAC', 'UPDATE', 'PERMISSION', 'ACTIVE', 'Cho phép sửa thông tin và liên kết API của quyền', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('0c741c17-c894-798c-9909-64fdadec28ce', 'PERMISSION_DELETE', 'Xóa định nghĩa quyền', 'RBAC', 'DELETE', 'PERMISSION', 'ACTIVE', 'Cho phép vô hiệu hóa quyền', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),

    ('0c189014-7341-f3e9-5f56-debbdf8f8835', 'MENU_VIEW', 'Xem cấu hình menu', 'MENU', 'VIEW', 'MENU', 'ACTIVE', 'Cho phép tra cứu cây menu hệ thống', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('42ab2d85-8b8c-3ded-a879-b2ccd76a4982', 'MENU_CREATE', 'Tạo menu mới', 'MENU', 'CREATE', 'MENU', 'ACTIVE', 'Cho phép tạo node menu mới', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('6efb317c-fdc2-8c36-d21b-2c4b33a429e3', 'MENU_UPDATE', 'Cập nhật menu và gán quyền menu', 'MENU', 'UPDATE', 'MENU', 'ACTIVE', 'Cho phép chỉnh sửa thuộc tính menu và cấu hình menu-permission', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('174e5f53-7af5-9c23-158c-56202987d4a5', 'MENU_DELETE', 'Xóa menu', 'MENU', 'DELETE', 'MENU', 'ACTIVE', 'Cho phép xóa node menu', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system')
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    module_code = EXCLUDED.module_code,
    action_code = EXCLUDED.action_code,
    resource_type = EXCLUDED.resource_type,
    status = EXCLUDED.status,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';


-- ----------------------------------------------------------------------------------------------------
-- PHẦN 3: BẢNG BẢO VỆ API (idp_permission_apis)
-- Ánh xạ các URI endpoint trong controller vào Permission tương ứng để Gateway/Interceptor kiểm soát
-- ----------------------------------------------------------------------------------------------------

INSERT INTO idp_permission_apis (
    id, permission_id, http_method, uri_pattern, match_type, service_code, status, priority, is_allow, description, created_at, updated_at, created_by, updated_by
)
SELECT
    LOWER(SUBSTRING(md5(p.id || ':' || src.http_method || ':' || src.uri_pattern || ':' || src.service_code) FROM 1 FOR 8) || '-' ||
          SUBSTRING(md5(p.id || ':' || src.http_method || ':' || src.uri_pattern || ':' || src.service_code) FROM 9 FOR 4) || '-' ||
          SUBSTRING(md5(p.id || ':' || src.http_method || ':' || src.uri_pattern || ':' || src.service_code) FROM 13 FOR 4) || '-' ||
          SUBSTRING(md5(p.id || ':' || src.http_method || ':' || src.uri_pattern || ':' || src.service_code) FROM 17 FOR 4) || '-' ||
          SUBSTRING(md5(p.id || ':' || src.http_method || ':' || src.uri_pattern || ':' || src.service_code) FROM 21 FOR 12)),
    p.id,
    src.http_method,
    src.uri_pattern,
    src.match_type,
    src.service_code,
    src.status,
    src.priority,
    src.is_allow,
    src.description,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
FROM (
    VALUES
        -- === 3.1. NHÓM API PUBLIC / CHUNG (is_allow = TRUE, dùng quyền COMMON_SYSTEM) ===
        ('COMMON_SYSTEM', 'POST', '/api/v1/login/request-otp', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 10, TRUE, 'Yêu cầu gửi OTP đăng nhập'),
        ('COMMON_SYSTEM', 'POST', '/api/v1/login/verify-otp', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 10, TRUE, 'Xác thực OTP lấy JWT Token'),
        ('COMMON_SYSTEM', 'POST', '/api/v1/login/resend-otp', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 10, TRUE, 'Gửi lại mã OTP đăng nhập'),
        ('COMMON_SYSTEM', 'POST', '/api/v1/login/forgot-password/request-otp', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 10, TRUE, 'Yêu cầu OTP khôi phục mật khẩu'),
        ('COMMON_SYSTEM', 'POST', '/api/v1/login/forgot-password/verify-otp', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 10, TRUE, 'Xác nhận OTP và đổi mật khẩu mới'),
        ('COMMON_SYSTEM', 'POST', '/api/v1/login/forgot-password/resend-otp', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 10, TRUE, 'Gửi lại mã OTP quên mật khẩu'),
        ('COMMON_SYSTEM', 'POST', '/api/v1/login/logout', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 10, TRUE, 'Đăng xuất tài khoản khỏi hệ thống'),
        ('COMMON_SYSTEM', 'GET', '/api/v1/profile', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 10, TRUE, 'Xem thông tin hồ sơ người dùng hiện tại'),
        ('COMMON_SYSTEM', 'PUT', '/api/v1/profile', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 10, TRUE, 'Cập nhật thông tin cá nhân'),
        ('COMMON_SYSTEM', 'PUT', '/api/v1/profile/password', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 10, TRUE, 'Đổi mật khẩu tài khoản cá nhân'),
        ('COMMON_SYSTEM', 'PUT', '/api/v1/profile/two-factor', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 10, TRUE, 'Bật hoặc tắt xác thực hai lớp (2FA)'),
        ('COMMON_SYSTEM', 'GET', '/api/v1/users/me', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 10, TRUE, 'Xem hồ sơ của tôi (Legacy)'),
        ('COMMON_SYSTEM', 'PUT', '/api/v1/users/me', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 10, TRUE, 'Cập nhật hồ sơ của tôi (Legacy)'),
        ('COMMON_SYSTEM', 'PUT', '/api/v1/users/me/password', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 10, TRUE, 'Đổi mật khẩu của tôi (Legacy)'),
        ('COMMON_SYSTEM', 'GET', '/api/v1/notifications/me', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 10, TRUE, 'Lấy danh sách thông báo của tôi'),
        ('COMMON_SYSTEM', 'PUT', '/api/v1/notifications/me/read-all', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 10, TRUE, 'Đánh dấu đọc tất cả thông báo'),
        ('COMMON_SYSTEM', 'PUT', '/api/v1/notifications/{recipientId}/read', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 10, TRUE, 'Đánh dấu đọc 1 thông báo'),
        ('COMMON_SYSTEM', 'GET', '/api/v1/admin/rbac/menu-permissions/me/authorized-menus', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 10, TRUE, 'Lấy danh sách menu sidebar động được cấp quyền của tài khoản'),
        ('COMMON_SYSTEM', 'GET', '/api/v1/access-control/me/menus', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 10, TRUE, 'Lấy authorized menus cho client'),

        -- === 3.2. NHÓM API QUẢN LÝ NGƯỜI DÙNG (USER) ===
        ('USER_VIEW', 'POST', '/api/v1/admin/users/search', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Tìm kiếm và phân trang danh sách người dùng'),
        ('USER_VIEW', 'GET', '/api/v1/admin/users/{userId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xem chi tiết thông tin tài khoản người dùng'),
        ('USER_CREATE', 'POST', '/api/v1/admin/access-control/users', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Tạo tài khoản người dùng kèm gán vai trò & tổ chức'),
        ('USER_UPDATE', 'PUT', '/api/v1/admin/users/{userId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Cập nhật thông tin tài khoản người dùng'),
        ('USER_UPDATE', 'POST', '/api/v1/admin/users/*/reset-password', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Đặt lại mật khẩu mặc định cho người dùng'),
        ('USER_LOCK', 'PUT', '/api/v1/admin/users/{userId}/lock', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Tạm khóa tài khoản người dùng'),
        ('USER_LOCK', 'PUT', '/api/v1/admin/users/{userId}/unlock', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Mở khóa tài khoản người dùng'),
        ('USER_DELETE', 'DELETE', '/api/v1/admin/users/*', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xóa an toàn tài khoản người dùng (Xác thực mật khẩu admin)'),
        ('USER_ASSIGN_ROLE', 'POST', '/api/v1/admin/access-control/users/assign-role', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Gán vai trò cho người dùng'),
        ('USER_EXPORT', 'POST', '/api/v1/exports/users', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Khởi tạo job xuất danh sách người dùng ra Excel'),

        -- === 3.3. NHÓM API QUẢN LÝ PHÒNG BAN (DEPARTMENT) ===
        ('DEPARTMENT_VIEW', 'GET', '/api/v1/admin/departments', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Lấy danh sách phòng ban'),
        ('DEPARTMENT_VIEW', 'GET', '/api/v1/admin/departments/*', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xem chi tiết phòng ban'),
        ('DEPARTMENT_CREATE', 'POST', '/api/v1/admin/departments', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Tạo mới phòng ban'),
        ('DEPARTMENT_UPDATE', 'PUT', '/api/v1/admin/departments/*', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Cập nhật phòng ban'),
        ('DEPARTMENT_EXPORT', 'POST', '/api/v1/exports/departments', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Khởi tạo job xuất phòng ban ra Excel'),
        ('DEPARTMENT_ASSIGN_ROLE', 'POST', '/api/v1/admin/rbac/department-user-roles', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Gán vai trò phòng ban cho nhân sự'),
        ('DEPARTMENT_ASSIGN_ROLE', 'DELETE', '/api/v1/admin/rbac/department-user-roles/{departmentUserRoleId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Hủy vai trò phòng ban của nhân sự'),
        ('DEPARTMENT_VIEW', 'GET', '/api/v1/admin/rbac/department-user-roles/users/{userId}/departments/{departmentId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xem vai trò của nhân sự trong phòng ban'),
        ('DEPARTMENT_VIEW', 'GET', '/api/v1/admin/rbac/department-user-roles/users/{userId}/departments/{departmentId}/permissions', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xem quyền của nhân sự trong phòng ban'),

        -- === 3.4. NHÓM API QUẢN LÝ NHÓM QUYỀN / VAI TRÒ (ROLE) ===
        ('ROLE_VIEW', 'GET', '/api/v1/admin/rbac/roles', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Xem danh sách vai trò kèm số lượng userCount'),
        ('ROLE_VIEW', 'GET', '/api/v1/admin/rbac/roles/*', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xem chi tiết thông tin vai trò'),
        ('ROLE_CREATE', 'POST', '/api/v1/admin/rbac/roles', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Tạo vai trò phân quyền mới'),
        ('ROLE_UPDATE', 'PUT', '/api/v1/admin/rbac/roles/*', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Cập nhật thông tin vai trò'),
        ('ROLE_DELETE', 'DELETE', '/api/v1/admin/rbac/roles/*', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xóa an toàn vai trò (Chặn xóa khi còn người dùng - 409 Conflict)'),
        ('ROLE_VIEW', 'GET', '/api/v1/admin/rbac/role-permissions/roles/{roleId}/permissions', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xem danh sách quyền hiện có của vai trò để render ma trận'),
        ('ROLE_UPDATE', 'PUT', '/api/v1/admin/rbac/role-permissions/roles/{roleId}/permissions', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Thay thế toàn bộ ma trận quyền của vai trò (Diff & Replace)'),
        ('ROLE_UPDATE', 'POST', '/api/v1/admin/rbac/role-permissions', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Gán quyền đơn lẻ cho vai trò'),
        ('ROLE_UPDATE', 'DELETE', '/api/v1/admin/rbac/role-permissions/{rolePermissionId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Hủy quyền đơn lẻ của vai trò'),

        -- === 3.5. NHÓM API QUẢN LÝ CÔNG TY / CƠ CẤU TỔ CHỨC (ORGANIZATION) ===
        ('ORGANIZATION_VIEW_TREE', 'GET', '/api/v1/admin/companies/tree', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Lấy toàn bộ cây sơ đồ cơ cấu tổ chức'),
        ('ORGANIZATION_VIEW_TREE', 'POST', '/api/v1/admin/companies/search', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Tìm kiếm trong cây cơ cấu tổ chức'),
        ('ORGANIZATION_VIEW', 'GET', '/api/v1/admin/companies/{companyId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xem chi tiết công ty'),
        ('ORGANIZATION_CREATE', 'POST', '/api/v1/admin/companies', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Tạo mới công ty / chi nhánh'),
        ('ORGANIZATION_UPDATE', 'PUT', '/api/v1/admin/companies/{companyId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Cập nhật thông tin công ty'),
        ('ORGANIZATION_ASSIGN_DEPARTMENT', 'PUT', '/api/v1/admin/companies/*/departments', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Gán phòng ban vào công ty'),
        ('ORGANIZATION_ASSIGN_ROLE', 'POST', '/api/v1/admin/rbac/company-user-roles', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Gán vai trò công ty cho người dùng'),
        ('ORGANIZATION_VIEW', 'GET', '/api/v1/admin/rbac/company-user-roles/users/{userId}/companies/{companyId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xem vai trò công ty của người dùng'),

        -- === 3.6. NHÓM API NHẬT KÝ HOẠT ĐỘNG (ACTIVITY_LOG) ===
        ('ACTIVITY_LOG_VIEW', 'POST', '/api/v1/admin/audit-logs/search', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Tìm kiếm và phân trang nhật ký hoạt động người dùng'),
        ('ACTIVITY_LOG_VIEW', 'GET', '/api/v1/admin/audit-logs/{logId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xem chi tiết log bao gồm payload request/response'),
        ('ACTIVITY_LOG_EXPORT', 'POST', '/api/v1/exports/audit-logs', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Khởi tạo job xuất nhật ký hoạt động ra Excel'),

        -- === 3.7. NHÓM API DASHBOARD & THỐNG KÊ (DASHBOARD) ===
        ('DASHBOARD_READ', 'GET', '/api/v1/admin/dashboard/user-stats', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Lấy số liệu 4 thẻ thống kê người dùng và tỷ lệ tăng trưởng'),
        ('DASHBOARD_READ', 'GET', '/api/v1/admin/dashboard/role-distribution', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Lấy tỷ lệ phân bố người dùng theo từng vai trò'),

        -- === 3.8. NHÓM API CẤU HÌNH EMAIL GATEWAY (EMAIL_GATEWAY) ===
        ('EMAIL_GATEWAY_VIEW', 'GET', '/api/v1/email-configs/current', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Lấy cấu hình Email Gateway đang kích hoạt'),
        ('EMAIL_GATEWAY_VIEW', 'GET', '/api/v1/email-configs/{configId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xem chi tiết một bản ghi cấu hình email'),
        ('EMAIL_GATEWAY_UPDATE', 'POST', '/api/v1/email-configs', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Thêm mới cấu hình email'),
        ('EMAIL_GATEWAY_UPDATE', 'PUT', '/api/v1/email-configs/{configId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Cập nhật cấu hình email'),
        ('EMAIL_GATEWAY_UPDATE', 'DELETE', '/api/v1/email-configs/{configId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xóa cấu hình email'),

        -- === 3.9. NHÓM API TIẾN TRÌNH XUẤT DỮ LIỆU (EXPORT) ===
        ('EXPORT_JOB_VIEW', 'GET', '/api/v1/exports', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Tra cứu danh sách các tiến trình xuất file'),
        ('EXPORT_JOB_VIEW', 'GET', '/api/v1/exports/*', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xem trạng thái tiến trình xuất file'),
        ('EXPORT_JOB_VIEW', 'GET', '/api/v1/exports/{jobId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xem chi tiết tiến trình xuất file'),
        ('EXPORT_JOB_VIEW', 'GET', '/api/v1/exports/*/download', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Lấy link presigned download file kết quả'),
        ('EXPORT_JOB_VIEW', 'GET', '/api/v1/exports/{jobId}/download', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Tải file kết quả export'),
        ('EXPORT_JOB_CANCEL', 'POST', '/api/v1/exports/*/cancel', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Hủy tiến trình export đang chạy'),
        ('EXPORT_JOB_CANCEL', 'POST', '/api/v1/exports/{jobId}/cancel', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Hủy tiến trình export theo ID'),

        -- === 3.10. NHÓM API QUẢN TRỊ NÂNG CAO (MENU & PERMISSION) ===
        ('MENU_VIEW', 'GET', '/api/v1/admin/rbac/menus', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Tìm kiếm danh sách menu'),
        ('MENU_VIEW', 'GET', '/api/v1/admin/rbac/menus/tree', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Lấy toàn bộ cây menu'),
        ('MENU_VIEW', 'GET', '/api/v1/admin/rbac/menus/{menuId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xem thông tin menu theo ID'),
        ('MENU_VIEW', 'GET', '/api/v1/admin/rbac/menus/code/{code}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xem menu theo code'),
        ('MENU_CREATE', 'POST', '/api/v1/admin/rbac/menus', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Tạo mới menu'),
        ('MENU_UPDATE', 'PUT', '/api/v1/admin/rbac/menus/{menuId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Cập nhật thông tin menu'),
        ('MENU_UPDATE', 'PATCH', '/api/v1/admin/rbac/menus/{menuId}/status', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Thay đổi trạng thái menu'),
        ('MENU_DELETE', 'DELETE', '/api/v1/admin/rbac/menus/{menuId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xóa menu'),
        ('MENU_VIEW', 'GET', '/api/v1/admin/rbac/menu-permissions/menus/{menuId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xem quyền liên kết với menu'),
        ('MENU_VIEW', 'GET', '/api/v1/admin/rbac/menu-permissions/users/{userId}/authorized-menus', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xem authorized menu của người dùng'),
        ('MENU_UPDATE', 'POST', '/api/v1/admin/rbac/menu-permissions', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Gán quyền cho menu'),
        ('MENU_UPDATE', 'DELETE', '/api/v1/admin/rbac/menu-permissions/{menuPermissionId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Hủy gán quyền cho menu'),

        ('PERMISSION_VIEW', 'GET', '/api/v1/admin/rbac/permissions', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Tìm kiếm danh mục quyền'),
        ('PERMISSION_VIEW', 'GET', '/api/v1/admin/rbac/permissions/{permissionId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xem chi tiết định nghĩa quyền'),
        ('PERMISSION_VIEW', 'GET', '/api/v1/admin/rbac/permissions/code/{code}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xem quyền theo code'),
        ('PERMISSION_CREATE', 'POST', '/api/v1/admin/rbac/permissions', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Tạo định nghĩa quyền mới'),
        ('PERMISSION_UPDATE', 'PUT', '/api/v1/admin/rbac/permissions/{permissionId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Cập nhật định nghĩa quyền'),
        ('PERMISSION_UPDATE', 'PATCH', '/api/v1/admin/rbac/permissions/{permissionId}/status', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Đổi trạng thái quyền'),
        ('PERMISSION_DELETE', 'DELETE', '/api/v1/admin/rbac/permissions/{permissionId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xóa định nghĩa quyền'),
        ('PERMISSION_VIEW', 'GET', '/api/v1/admin/rbac/permission-apis', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Tìm kiếm cấu hình bảo vệ API'),
        ('PERMISSION_VIEW', 'GET', '/api/v1/admin/rbac/permission-apis/{permissionApiId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xem chi tiết API mapping'),
        ('PERMISSION_UPDATE', 'POST', '/api/v1/admin/rbac/permission-apis', 'EXACT', 'LEDGER_SERVICE', 'ACTIVE', 100, FALSE, 'Tạo mới API mapping'),
        ('PERMISSION_UPDATE', 'PUT', '/api/v1/admin/rbac/permission-apis/{permissionApiId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Cập nhật API mapping'),
        ('PERMISSION_UPDATE', 'DELETE', '/api/v1/admin/rbac/permission-apis/{permissionApiId}', 'ANT_PATH', 'LEDGER_SERVICE', 'ACTIVE', 80, FALSE, 'Xóa API mapping')
) AS src(perm_code, http_method, uri_pattern, match_type, service_code, status, priority, is_allow, description)
JOIN idp_permission_definitions p ON p.code = src.perm_code
ON CONFLICT (permission_id, http_method, uri_pattern, service_code) DO UPDATE
SET match_type = EXCLUDED.match_type,
    status = EXCLUDED.status,
    priority = EXCLUDED.priority,
    is_allow = EXCLUDED.is_allow,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';


-- ----------------------------------------------------------------------------------------------------
-- PHẦN 4: BẢNG ÁNH XẠ TĨNH MENU ↔ QUYỀN (sys_menu_permissions)
-- Khi Role được gán quyền nguyên tử, hệ thống tự động suy diễn Menu hiển thị trên Sidebar kèm Action
-- ----------------------------------------------------------------------------------------------------

INSERT INTO sys_menu_permissions (
    id, menu_id, permission_id, display_action, status, description, created_at, updated_at, created_by, updated_by
)
SELECT
    LOWER(SUBSTRING(md5(m.id || ':' || p.id) FROM 1 FOR 8) || '-' ||
          SUBSTRING(md5(m.id || ':' || p.id) FROM 9 FOR 4) || '-' ||
          SUBSTRING(md5(m.id || ':' || p.id) FROM 13 FOR 4) || '-' ||
          SUBSTRING(md5(m.id || ':' || p.id) FROM 17 FOR 4) || '-' ||
          SUBSTRING(md5(m.id || ':' || p.id) FROM 21 FOR 12)),
    m.id,
    p.id,
    src.display_action,
    'ACTIVE',
    src.description,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
FROM (
    VALUES
        -- Menu: HOME (Trang chủ)
        ('HOME', 'HOME_VIEW', 'VIEW', 'Hiển thị menu Trang chủ trên sidebar'),

        -- Menu: Báo cáo & Thống kê cha (DASHBOARD)
        ('DASHBOARD', 'DASHBOARD_READ', 'READ', 'Hiển thị cụm menu Báo cáo & Thống kê'),

        -- Menu: Dashboard doanh thu (DASHBOARD_REVENUE)
        ('DASHBOARD_REVENUE', 'DASHBOARD_REVENUE_VIEW', 'VIEW', 'Hiển thị menu Dashboard doanh thu'),
        ('DASHBOARD_REVENUE', 'DASHBOARD_REVENUE_EXPORT_EXCEL', 'EXPORT_EXCEL', 'Nút xuất Excel báo cáo doanh thu'),
        ('DASHBOARD_REVENUE', 'DASHBOARD_REVENUE_EXPORT_PDF', 'EXPORT_PDF', 'Nút xuất PDF báo cáo doanh thu'),

        -- Menu: Dashboard sản lượng (DASHBOARD_VOLUME)
        ('DASHBOARD_VOLUME', 'DASHBOARD_VOLUME_VIEW', 'VIEW', 'Hiển thị menu Dashboard sản lượng'),
        ('DASHBOARD_VOLUME', 'DASHBOARD_VOLUME_EXPORT_EXCEL', 'EXPORT_EXCEL', 'Nút xuất Excel báo cáo sản lượng'),
        ('DASHBOARD_VOLUME', 'DASHBOARD_VOLUME_EXPORT_PDF', 'EXPORT_PDF', 'Nút xuất PDF báo cáo sản lượng'),

        -- Menu: Dashboard công nợ (DASHBOARD_DEBT)
        ('DASHBOARD_DEBT', 'DASHBOARD_DEBT_VIEW', 'VIEW', 'Hiển thị menu Dashboard công nợ'),
        ('DASHBOARD_DEBT', 'DASHBOARD_DEBT_EXPORT_EXCEL', 'EXPORT_EXCEL', 'Nút xuất Excel báo cáo công nợ'),
        ('DASHBOARD_DEBT', 'DASHBOARD_DEBT_EXPORT_PDF', 'EXPORT_PDF', 'Nút xuất PDF báo cáo công nợ'),

        -- Menu: AI Insight (AI_INSIGHT)
        ('AI_INSIGHT', 'AI_INSIGHT_VIEW', 'VIEW', 'Hiển thị menu AI Insight trên sidebar'),
        ('AI_INSIGHT', 'AI_INSIGHT_EXPORT', 'EXPORT', 'Nút xuất dữ liệu phân tích AI'),

        -- Menu: Sản phẩm thủ công (PRODUCT_MANUAL)
        ('PRODUCT_MANUAL', 'PRODUCT_VIEW', 'VIEW', 'Hiển thị menu Sản phẩm thủ công'),
        ('PRODUCT_MANUAL', 'PRODUCT_CREATE', 'CREATE', 'Nút Thêm mới sản phẩm thủ công'),
        ('PRODUCT_MANUAL', 'PRODUCT_UPDATE', 'UPDATE', 'Nút Chỉnh sửa sản phẩm thủ công'),
        ('PRODUCT_MANUAL', 'PRODUCT_DELETE', 'DELETE', 'Nút Xóa sản phẩm thủ công'),
        ('PRODUCT_MANUAL', 'PRODUCT_HISTORY_VIEW', 'HISTORY_VIEW', 'Nút Xem lịch sử sản phẩm'),

        -- Menu: Lấy dữ liệu hệ thống ngoài (PRODUCT_EXTERNAL)
        ('PRODUCT_EXTERNAL', 'PRODUCT_VIEW', 'VIEW', 'Hiển thị menu Lấy dữ liệu hệ thống ngoài'),
        ('PRODUCT_EXTERNAL', 'PRODUCT_EXTERNAL_FETCH', 'FETCH', 'Nút Kéo dữ liệu từ hệ thống ngoài'),

        -- Menu: Đồng bộ dữ liệu sản phẩm (PRODUCT_SYNC)
        ('PRODUCT_SYNC', 'PRODUCT_VIEW', 'VIEW', 'Hiển thị menu Đồng bộ dữ liệu sản phẩm'),
        ('PRODUCT_SYNC', 'PRODUCT_SYNC', 'SYNC', 'Nút Kích hoạt đồng bộ sản phẩm'),

        -- Menu: Import dữ liệu sản phẩm (PRODUCT_IMPORT)
        ('PRODUCT_IMPORT', 'PRODUCT_VIEW', 'VIEW', 'Hiển thị menu Import dữ liệu sản phẩm'),
        ('PRODUCT_IMPORT', 'PRODUCT_IMPORT', 'IMPORT', 'Nút Tải lên file Excel sản phẩm'),

        -- Menu: Nhật ký hoạt động (ACTIVITY_LOG)
        ('ACTIVITY_LOG', 'ACTIVITY_LOG_VIEW', 'VIEW', 'Hiển thị menu Nhật ký hoạt động'),
        ('ACTIVITY_LOG', 'ACTIVITY_LOG_EXPORT', 'EXPORT', 'Nút Xuất Excel lịch sử nhật ký'),

        -- Menu: Quản lý người dùng (USER_MANAGEMENT)
        ('USER_MANAGEMENT', 'USER_VIEW', 'VIEW', 'Hiển thị menu Quản lý người dùng trên sidebar'),
        ('USER_MANAGEMENT', 'USER_CREATE', 'CREATE', 'Nút + Thêm mới tài khoản người dùng'),
        ('USER_MANAGEMENT', 'USER_UPDATE', 'UPDATE', 'Nút Chỉnh sửa thông tin tài khoản'),
        ('USER_MANAGEMENT', 'USER_DELETE', 'DELETE', 'Nút Xóa tài khoản người dùng an toàn'),
        ('USER_MANAGEMENT', 'USER_LOCK', 'LOCK', 'Nút Khóa hoặc Mở khóa tài khoản'),
        ('USER_MANAGEMENT', 'USER_EXPORT', 'EXPORT', 'Nút Xuất Excel danh sách người dùng'),
        ('USER_MANAGEMENT', 'USER_ASSIGN_ROLE', 'ASSIGN_ROLE', 'Chức năng Gán vai trò cho người dùng'),

        -- Menu: Quản lý phòng ban (DEPARTMENT_MANAGEMENT)
        ('DEPARTMENT_MANAGEMENT', 'DEPARTMENT_VIEW', 'VIEW', 'Hiển thị menu Quản lý phòng ban'),
        ('DEPARTMENT_MANAGEMENT', 'DEPARTMENT_CREATE', 'CREATE', 'Nút + Thêm phòng ban mới'),
        ('DEPARTMENT_MANAGEMENT', 'DEPARTMENT_UPDATE', 'UPDATE', 'Nút Chỉnh sửa phòng ban'),
        ('DEPARTMENT_MANAGEMENT', 'DEPARTMENT_EXPORT', 'EXPORT', 'Nút Xuất Excel danh sách phòng ban'),
        ('DEPARTMENT_MANAGEMENT', 'DEPARTMENT_ASSIGN_ROLE', 'ASSIGN_ROLE', 'Chức năng Gán vai trò nội bộ phòng ban'),

        -- Menu: Quản lý nhóm quyền (ROLE_MANAGEMENT)
        ('ROLE_MANAGEMENT', 'ROLE_VIEW', 'VIEW', 'Hiển thị menu Quản lý nhóm quyền trên sidebar'),
        ('ROLE_MANAGEMENT', 'ROLE_CREATE', 'CREATE', 'Nút + Thêm vai trò mới'),
        ('ROLE_MANAGEMENT', 'ROLE_UPDATE', 'UPDATE', 'Nút Lưu thay đổi phân quyền ma trận'),
        ('ROLE_MANAGEMENT', 'ROLE_DELETE', 'DELETE', 'Nút Xóa vai trò'),

        -- Menu: Quản lý công ty / tổ chức (ORGANIZATION_MANAGEMENT)
        ('ORGANIZATION_MANAGEMENT', 'ORGANIZATION_VIEW', 'VIEW', 'Hiển thị menu Quản lý công ty / tổ chức'),
        ('ORGANIZATION_MANAGEMENT', 'ORGANIZATION_VIEW_TREE', 'VIEW_TREE', 'Chức năng xem cây phân cấp cơ cấu tổ chức'),
        ('ORGANIZATION_MANAGEMENT', 'ORGANIZATION_CREATE', 'CREATE', 'Nút Thêm mới công ty / chi nhánh'),
        ('ORGANIZATION_MANAGEMENT', 'ORGANIZATION_UPDATE', 'UPDATE', 'Nút Cập nhật thông tin công ty'),
        ('ORGANIZATION_MANAGEMENT', 'ORGANIZATION_CHANGE_STATUS', 'CHANGE_STATUS', 'Nút Thay đổi trạng thái công ty'),
        ('ORGANIZATION_MANAGEMENT', 'ORGANIZATION_ASSIGN_DEPARTMENT', 'ASSIGN_DEPARTMENT', 'Chức năng Gán phòng ban cho công ty'),
        ('ORGANIZATION_MANAGEMENT', 'ORGANIZATION_ASSIGN_ROLE', 'ASSIGN_ROLE', 'Chức năng Gán vai trò cấp công ty'),

        -- Menu: Cấu hình Email Gateway (EMAIL_GATEWAY)
        ('EMAIL_GATEWAY', 'EMAIL_GATEWAY_VIEW', 'VIEW', 'Hiển thị menu Cấu hình Email Gateway'),
        ('EMAIL_GATEWAY', 'EMAIL_GATEWAY_UPDATE', 'UPDATE', 'Nút Lưu cấu hình máy chủ gửi mail')
) AS src(menu_code, perm_code, display_action, description)
JOIN sys_menus m ON m.code = src.menu_code
JOIN idp_permission_definitions p ON p.code = src.perm_code
ON CONFLICT (menu_id, permission_id) DO UPDATE
SET display_action = EXCLUDED.display_action,
    status = EXCLUDED.status,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';


-- ----------------------------------------------------------------------------------------------------
-- PHẦN 5: KHỞI TẠO VAI TRÒ MẶC ĐỊNH & GÁN TOÀN BỘ QUYỀN CHO SUPER ADMIN
-- ----------------------------------------------------------------------------------------------------

-- 5.1. Khởi tạo 5 nhóm quyền chuẩn hệ thống theo thiết kế Figma
INSERT INTO idp_groups (
    id, code, name, is_default, is_super_admin, status, sort_order, description, created_at, updated_at, created_by, updated_by
)
VALUES
    ('8f2ac001-0000-0000-0000-000000000001', 'SUPER_ADMIN', 'Quản trị viên cấp cao', FALSE, TRUE, 'ACTIVE', 1, 'Toàn quyền tối cao đối với tất cả module và API hệ thống', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('8f2ac001-0000-0000-0000-000000000002', 'ADMIN', 'Quản trị viên', FALSE, FALSE, 'ACTIVE', 2, 'Quản trị vận hành người dùng, phòng ban, vai trò và cấu hình', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('8f2ac001-0000-0000-0000-000000000003', 'TRUONG_PHONG', 'Trưởng phòng', FALSE, FALSE, 'ACTIVE', 3, 'Quản lý nghiệp vụ phòng ban và xem thống kê báo cáo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('8f2ac001-0000-0000-0000-000000000004', 'KE_TOAN', 'Kế toán', FALSE, FALSE, 'ACTIVE', 4, 'Quản lý báo cáo doanh thu, sản lượng và công nợ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'),
    ('8f2ac001-0000-0000-0000-000000000005', 'NHAN_VIEN', 'Nhân viên', TRUE, FALSE, 'ACTIVE', 5, 'Vai trò mặc định truy cập các tính năng nghiệp vụ cơ bản', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system')
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    is_super_admin = EXCLUDED.is_super_admin,
    sort_order = EXCLUDED.sort_order,
    status = EXCLUDED.status,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

-- 5.2. Gán toàn bộ quyền (Full System Permissions) cho SUPER_ADMIN và ADMIN
INSERT INTO idp_role_permissions (
    id, group_id, permission_id, status, effective_from, description, created_at, updated_at, created_by, updated_by
)
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
    'Toàn quyền hệ thống được cấp cho ' || g.name,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
FROM idp_groups g
CROSS JOIN idp_permission_definitions p
WHERE g.code IN ('SUPER_ADMIN', 'ADMIN')
  AND p.status = 'ACTIVE'
ON CONFLICT (group_id, permission_id) DO UPDATE
SET status = 'ACTIVE',
    effective_to = NULL,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

-- 5.3. Gán quyền nghiệp vụ mẫu cho Kế toán (KE_TOAN)
INSERT INTO idp_role_permissions (
    id, group_id, permission_id, status, effective_from, description, created_at, updated_at, created_by, updated_by
)
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
    'Quyền kế toán được cấp cho ' || g.name,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'system',
    'system'
FROM idp_groups g
JOIN idp_permission_definitions p ON p.code IN (
    'HOME_VIEW', 'COMMON_SYSTEM', 'USER_VIEW',
    'DASHBOARD_READ', 'DASHBOARD_REVENUE_VIEW', 'DASHBOARD_REVENUE_EXPORT_EXCEL',
    'DASHBOARD_VOLUME_VIEW', 'DASHBOARD_VOLUME_EXPORT_EXCEL',
    'DASHBOARD_DEBT_VIEW', 'DASHBOARD_DEBT_EXPORT_EXCEL',
    'EXPORT_JOB_VIEW'
)
WHERE g.code = 'KE_TOAN'
ON CONFLICT (group_id, permission_id) DO UPDATE
SET status = 'ACTIVE',
    effective_to = NULL,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

-- 5.4. Tự động gán tài khoản 'admin' vào vai trò SUPER_ADMIN (nếu tài khoản admin đã tồn tại)
INSERT INTO idp_user_groups (
    id, group_id, user_id, effective_from, status
)
SELECT
    LOWER(SUBSTRING(md5(u.id || ':' || g.id) FROM 1 FOR 8) || '-' ||
          SUBSTRING(md5(u.id || ':' || g.id) FROM 9 FOR 4) || '-' ||
          SUBSTRING(md5(u.id || ':' || g.id) FROM 13 FOR 4) || '-' ||
          SUBSTRING(md5(u.id || ':' || g.id) FROM 17 FOR 4) || '-' ||
          SUBSTRING(md5(u.id || ':' || g.id) FROM 21 FOR 12)),
    g.id,
    u.id,
    CURRENT_TIMESTAMP,
    'ACTIVE'
FROM idp_users u
JOIN idp_groups g ON g.code = 'SUPER_ADMIN'
WHERE u.user_name = 'admin'
ON CONFLICT (user_id, group_id) DO UPDATE
SET status = 'ACTIVE';

-- ====================================================================================================
-- KẾT THÚC SCRIPT KHỞI TẠO RBAC & MENU
-- ====================================================================================================
