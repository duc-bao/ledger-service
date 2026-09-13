# Bộ Kịch Bản Kiểm Thử & Hướng Dẫn Tích Hợp: Phân Quyền RBAC & Menu Động

> **Dự án**: `ledger-service` (Backend Spring Boot 3)  
> **Tài liệu kiểm thử**: Luồng phân quyền Vai trò (Role) ↔ Quyền hạn (Permission) ↔ Menu Sidebar (Dynamic Navigation)  
> **Đối tượng sử dụng**: Backend Dev, Frontend Dev, QA/Tester, Tech Lead  
> **Mục tiêu**: Đảm bảo người dùng được cấp quyền nào thì hiển thị đúng Menu đó, không bị thiếu Menu và không bị lỗi `403 Forbidden` khi truy cập.

---

## 1. Kiến Trúc & Nguyên Lý Hoạt Động

```
  ┌─────────────────┐       (1) Gán Quyền       ┌────────────────────────────┐
  │   VAI TRÒ       │ ────────────────────────► │     QUYỀN NGUYÊN TỬ        │
  │  (idp_groups)   │                           │(idp_permission_definitions)│
  └────────┬────────┘                           └─────────────┬──────────────┘
           │                                                  │
           │ (2) Gán User vào Role                            │ (3) Mapping Cố Định Hệ Thống
           ▼                                                  ▼     (sys_menu_permissions)
  ┌─────────────────┐       (4) Tự động sinh Menu     ┌────────────────────────────┐
  │   NGƯỜI DÙNG    │ ──────────────────────────────► │       MENU GIAO DIỆN       │
  │   (idp_users)   │   GET /authorized-menus         │        (sys_menus)         │
  └─────────────────┘                                 └────────────────────────────┘
```

### Nguyên tắc 3 KHÔNG:
1. **KHÔNG cấp Menu trực tiếp cho Role/User**: Admin chỉ thao tác cấp Permission cho Role trên bảng ma trận Figma.
2. **KHÔNG hiển thị Menu nếu không có quyền**: Nếu user không có bất kỳ quyền nào thuộc module đó, Menu sẽ tự động ẩn khỏi Sidebar.
3. **KHÔNG bao giờ cấp quyền con (`ADD/EDIT/DELETE`) mà thiếu quyền cha (`VIEW`)**: Quyền `VIEW` là điều kiện tiên quyết để vào được màn hình.

---

## 2. Danh Sách API Tham Gia Vào Luồng

| STT | API Endpoint | Method | Chức năng | DTO Request / Response |
|:---:|---|:---:|---|---|
| 1 | `/api/v1/admin/rbac/permissions` | `GET` | Lấy danh mục quyền để dựng bảng ma trận | Res: `Page<PermissionResponse>` |
| 2 | `/api/v1/admin/rbac/roles` | `POST` | Tạo vai trò mới | Req: `CreateRoleRequest`<br>Res: `RoleResponse` |
| 3 | `/api/v1/admin/rbac/role-permissions/roles/{roleId}/permissions` | `GET` | Lấy các quyền đang có của vai trò | Res: `List<PermissionResponse>` |
| 4 | `/api/v1/admin/rbac/role-permissions/roles/{roleId}/permissions` | `PUT` | Cập nhật ma trận quyền cho vai trò (Diff & Replace) | Req: `RolePermissionReplaceRequest`<br>Res: `BaseResponse<Object>` |
| 5 | `/api/v1/admin/access/users` | `POST` | Tạo user và gán vai trò + phòng ban | Req: `AdminCreateUserRequest`<br>Res: `AdminCreateUserResponse` |
| 6 | `/api/v1/admin/access-control/users/assign-role` | `POST` | Gán vai trò cho user hiện có | Req: `AssignUserRoleRequest`<br>Res: `UserRoleResponse` |
| 7 | `/api/v1/admin/rbac/menu-permissions/me/authorized-menus` | `GET` | Lấy cây Menu động cho user đang đăng nhập | Header: `Bearer Token`<br>Res: `List<AuthorizedMenuResponse>` |
| 8 | `/api/v1/admin/rbac/roles/{roleId}` | `DELETE` | Xóa vai trò (Chặn xóa nếu còn user) | Req: `DeleteRoleRequest`<br>Res: `BaseResponse<Object>` |
| 9 | `/api/v1/admin/rbac/menu-permissions` | `POST` | Cấu hình liên kết Menu ↔ Permission (Dành cho Dev/System) | Req: `MenuPermissionCreateRequest`<br>Res: `MenuPermissionResponse` |

---

## 3. Chi Tiết Các Kịch Bản Kiểm Thử (Test Cases)

### TC-RBAC-01: Cấp quyền XEM (VIEW) cho Vai trò -> Menu hiển thị chế độ Chỉ Xem
* **Mục tiêu**: Kiểm tra khi chỉ cấp quyền Xem, Menu xuất hiện và không bị lỗi 403.
* **Tiền điều kiện**: Tạo Role `TEST_VIEWER`. Tạo user `user_viewer` gán vào role này.
* **Các bước thực hiện**:
  1. Gọi `PUT /api/v1/admin/rbac/role-permissions/roles/{roleId}/permissions` với danh sách chỉ có quyền `USER_VIEW`.
  2. Đăng nhập bằng tài khoản `user_viewer`.
  3. Gọi `GET /api/v1/admin/rbac/menu-permissions/me/authorized-menus`.
  4. Mở trang Quản lý người dùng: gọi `POST /api/v1/admin/users/search`.
  5. Thử gọi API tạo tài khoản: `POST /api/v1/admin/access/users`.
* **Kết quả kỳ vọng**:
  * Bước 3: Trả về HTTP 200, trong mảng `data` có menu `MANAGER_USER` (hoặc `USER_MANAGEMENT`). Trường `actions` không có action `CREATE` hay `DELETE`.
  * Bước 4: Gọi tìm kiếm thành công HTTP 200, hiển thị bảng danh sách.
  * Bước 5: Bị chặn với mã **HTTP 403 Forbidden**. Trên UI các nút "+ Thêm người dùng" bị ẩn hoàn toàn.

---

### TC-RBAC-02: Cấp ĐẦY ĐỦ quyền (VIEW, CREATE, UPDATE, DELETE, EXPORT)
* **Mục tiêu**: Kiểm tra người dùng có toàn quyền quản trị module.
* **Tiền điều kiện**: Role `TEST_MANAGER`. User `user_manager`.
* **Các bước thực hiện**:
  1. Gọi `PUT /api/v1/admin/rbac/role-permissions/roles/{roleId}/permissions` với:
     `["USER_VIEW", "USER_CREATE", "USER_UPDATE", "USER_DELETE", "USER_EXPORT"]`.
  2. Đăng nhập bằng `user_manager`.
  3. Gọi `GET /api/v1/admin/rbac/menu-permissions/me/authorized-menus`.
* **Kết quả kỳ vọng**:
  * Menu `Quản lý người dùng` xuất hiện.
  * Mảng `actions` của menu chứa đầy đủ các actions: `CREATE`, `UPDATE`, `DELETE`, `EXPORT`.
  * Frontend hiển thị đầy đủ các nút: "+ Thêm mới", "Chỉnh sửa", "Xóa", "Xuất Excel". Thao tác các nút đều thành công (HTTP 200/201).

---

### TC-RBAC-03: Kiểm tra phòng ngừa lỗi 403 (Quyền Thao Tác thiếu quyền XEM)
* **Mục tiêu**: Đảm bảo không xảy ra tình trạng "Menu hiện nhưng bấm vào bị 403".
* **Kịch bản lỗi**: Người cấu hình cố tình chỉ gán `USER_DELETE` mà không gán `USER_VIEW`.
* **Quy tắc xử lý**:
  * **Phía Frontend**: Giao diện ma trận phân quyền Figma tự động tích kèm ô `XEM` khi người dùng tích bất kỳ ô `THÊM/SỬA/XÓA/XUẤT`.
  * **Phía Backend**: Trong bảng `sys_menu_permissions`, Menu được kích hoạt chủ đạo bởi quyền `VIEW`. Nếu chỉ có quyền con mà không có quyền `VIEW`, Frontend điều hướng về trang thông báo "Bạn không có quyền truy cập trang này" thay vì làm crash app.

---

### TC-RBAC-04: Thu hồi toàn bộ quyền của Module -> Menu tự động biến mất
* **Mục tiêu**: Kiểm tra cơ chế Dynamic Menu khi bị rút quyền.
* **Tiền điều kiện**: Role đang có quyền `USER_VIEW`. Menu đang hiển thị.
* **Các bước thực hiện**:
  1. Admin gọi `PUT /api/v1/admin/rbac/role-permissions/roles/{roleId}/permissions` với mảng rỗng `[]` (hoặc chỉ còn quyền module khác như `DEPARTMENT_VIEW`).
  2. User đang đăng nhập tải lại trang (gọi lại `GET /authorized-menus`).
  3. User cố tình nhập trực tiếp URL trên trình duyệt: `http://localhost:3000/manager-user`.
* **Kết quả kỳ vọng**:
  * Menu `Quản lý người dùng` biến mất hoàn toàn khỏi Sidebar.
  * Khi truy cập URL trực tiếp, Router Frontend chặn lại và điều hướng về trang 403 hoặc trang chủ.

---

### TC-RBAC-05: Người dùng có NHIỀU VAI TRÒ (Multi-Role Inheritance)
* **Mục tiêu**: Kiểm tra cơ chế hợp nhất quyền (Union).
* **Tiền điều kiện**: User A được gán 2 vai trò:
  * Role 1 (`ROLE_PRODUCT`): có quyền module Sản phẩm.
  * Role 2 (`ROLE_ACCOUNTANT`): có quyền module Công nợ & Doanh thu.
* **Các bước thực hiện**:
  1. Đăng nhập User A.
  2. Gọi `GET /api/v1/admin/rbac/menu-permissions/me/authorized-menus`.
* **Kết quả kỳ vọng**:
  * Cây Menu trả về chứa **cả 2 nhóm menu**: Sản phẩm VÀ Công nợ/Doanh thu.
  * Không có sự ghi đè hay mất mát quyền giữa các vai trò.

---

### TC-RBAC-06: Chặn XÓA Vai trò khi ĐANG CÓ người dùng hoạt động (Figma Node `2176:1851`)
* **Mục tiêu**: Đảm bảo toàn vẹn dữ liệu, không làm mất vai trò của user đang hoạt động.
* **Tiền điều kiện**: Role `TRUONG_PHONG` đang có 3 user hoạt động (`userCount = 3`).
* **Các bước thực hiện**:
  1. Gọi `DELETE /api/v1/admin/rbac/roles/{roleId}`.
* **Kết quả kỳ vọng**:
  * Backend ném ngoại lệ HTTP **409 Conflict**.
  * Response body chứa:
    ```json
    {
      "code": "ROLE_HAS_USERS",
      "data": {
        "code": "ROLE_HAS_USERS",
        "roleId": "{roleId}",
        "roleName": "Trưởng phòng",
        "userCount": 3
      }
    }
    ```
  * Giao diện hiển thị đúng Modal cảnh báo Figma `2176:1851`: *"Vai trò này hiện không thể xóa. Hiện đang có [3] người dùng thuộc vai trò [Trưởng phòng]"* và nút "Hủy".

---

### TC-RBAC-07: XÁC NHẬN XÓA Vai trò khi KHÔNG CÒN người dùng (Figma Node `2176:1495`)
* **Mục tiêu**: Xóa vai trò an toàn kèm lý do xóa.
* **Tiền điều kiện**: Role `ROLE_TEST_EMPTY` không có user nào (`userCount = 0`).
* **Các bước thực hiện**:
  1. Gửi request:
     ```http
     DELETE /api/v1/admin/rbac/roles/{roleId}
     Content-Type: application/json

     {
       "deleteReason": "Tái cơ cấu tổ chức phòng ban năm 2026"
     }
     ```
* **Kết quả kỳ vọng**:
  * HTTP 200 OK.
  * Trong Database (`idp_groups`): `status = 'INACTIVE'`, cột `delete_reason` được lưu chính xác lý do đã nhập.
  * Gọi lại `GET /api/v1/admin/rbac/roles` thì role này không còn xuất hiện trong danh sách active.

---

## 4. Hướng Dẫn Chạy Thử Nghiệm Bằng cURL (Copy & Run)

### 1. Đăng nhập Admin lấy Token:
```bash
curl -X POST http://localhost:8091/api/v1/login/request-otp \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456a@"
  }'
```

### 2. Cấp ma trận quyền cho Vai trò:
```bash
curl -X PUT http://localhost:8091/api/v1/admin/rbac/role-permissions/roles/{ROLE_ID}/permissions \
  -H "Authorization: Bearer {ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "permissionIds": [
      "8f2aa001-1111-4444-8888-000000000001",
      "8f2aa001-1111-4444-8888-000000000002"
    ]
  }'
```

### 3. Lấy Menu Sidebar của Người dùng hiện tại:
```bash
curl -X GET http://localhost:8091/api/v1/admin/rbac/menu-permissions/me/authorized-menus \
  -H "Authorization: Bearer {USER_TOKEN}"
```

### 4. Thử Xóa Vai trò (Test chặn khi có user / xóa an toàn):
```bash
curl -X DELETE http://localhost:8091/api/v1/admin/rbac/roles/{ROLE_ID} \
  -H "Authorization: Bearer {ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "deleteReason": "Ly do xoa vai tro kiem thu"
  }'
```
