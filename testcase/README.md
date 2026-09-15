# Thư Mục Test Cases - Ledger Service

Thư mục này chứa các kịch bản kiểm thử (Test Cases), ma trận đối soát và tài liệu hướng dẫn tích hợp giữa Giao diện người dùng (Figma Frontend) và Hệ thống Backend (`ledger-service`).

## Danh mục tài liệu:

1. [rbac_permission_menu_testcases.md](file:///d:/ledger-service/testcase/rbac_permission_menu_testcases.md):
   - Kiến trúc phân quyền 3 tầng: Role ↔ Permission ↔ Menu.
   - Hướng dẫn gọi API phân quyền và hiển thị menu tự động.
   - 8 kịch bản kiểm thử chi tiết (TC-RBAC-01 đến TC-RBAC-08).

2. [rbac_curl_guide.md](file:///d:/ledger-service/testcase/rbac_curl_guide.md):
   - Chuỗi 13 bước lệnh cURL chạy từ A - Z (Bash & Windows PowerShell).
   - Kiểm thử toàn diện luồng Admin tạo Role, gán Permission, tạo User, kiểm tra Menu động và chặn 403.

3. [init_rbac_menus_permissions.sql](file:///d:/ledger-service/src/main/resources/seed/init_rbac_menus_permissions.sql):
   - Script SQL khởi tạo dữ liệu tĩnh RBAC & Menu toàn diện phục vụ triển khai database mới cho khách hàng.
   - Gồm 5 phần: Menu hệ thống (`sys_menus`), Định nghĩa quyền (`idp_permission_definitions`), Bảo vệ API (`idp_permission_apis`), Ánh xạ menu-quyền (`sys_menu_permissions`), và Khởi tạo vai trò mặc định (`idp_groups`, `idp_role_permissions`).

