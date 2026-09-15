# Bộ Lệnh cURL Đầy Đủ: Kiểm Thử Toàn Diện Luồng Phân Quyền RBAC & Menu Động

> **Base URL**: `http://localhost:8091`  
> **Tài khoản Admin mặc định**: `admin` / `123456a@`

---

## 1. Biến Môi Trường Dùng Chung

Khi chạy bằng Bash (Terminal/Git Bash), bạn có thể set các biến sau:

```bash
export BASE_URL="http://localhost:8091"
export ADMIN_TOKEN=""
export USER_TOKEN=""
export NEW_ROLE_ID=""
export NEW_USER_ID=""
```

Đối với Windows PowerShell:

```powershell
$BASE_URL = "http://localhost:8091"
$ADMIN_TOKEN = ""
$USER_TOKEN = ""
$NEW_ROLE_ID = ""
$NEW_USER_ID = ""
```

---

## 2. Chuỗi Các Lệnh cURL Thực Thi Từng Bước (Step-by-Step)

### BƯỚC 1: Đăng nhập tài khoản Admin lấy Token
```bash
curl -s -X POST "$BASE_URL/api/v1/login/request-otp" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456a@"
  }'
```
> **Phản hồi mẫu**: Lấy giá trị `accessToken` lưu vào biến `ADMIN_TOKEN`.
```json
{
  "code": "SUCCESS",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsIn...",
    "tokenType": "Bearer",
    "expiresInSeconds": 3600,
    "twoFactorRequired": false,
    "fullName": "Administrator",
    "roleName": "Super Admin"
  }
}
```

---

### BƯỚC 2: Lấy danh mục tất cả Permission để dựng Ma trận quyền
```bash
curl -s -X GET "$BASE_URL/api/v1/admin/rbac/permissions?page=0&size=100" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```
> **Mục đích**: Frontend dùng danh sách này để nhóm theo `moduleCode` (Hàng) và `actionCode` (Cột: XEM, THÊM, SỬA, XÓA, XUẤT).  
> Lưu lại các UUID quyền, ví dụ:
> - `USER_VIEW`: `8f2aa001-0000-0000-0000-000000000001`
> - `USER_CREATE`: `8f2aa001-0000-0000-0000-000000000002`

---

### BƯỚC 3: Tạo một Vai trò (Role) mới
```bash
curl -s -X POST "$BASE_URL/api/v1/admin/rbac/roles" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "KE_TOAN_TRUONG",
    "name": "Kế toán trưởng",
    "description": "Quản lý công nợ và xem danh sách người dùng",
    "sortOrder": 10
  }'
```
> **Phản hồi mẫu**: Lưu `id` trả về vào biến `NEW_ROLE_ID`.
```json
{
  "code": "SUCCESS",
  "data": {
    "id": "c0a80101-91ea-1234-8888-abcdef012345",
    "code": "KE_TOAN_TRUONG",
    "name": "Kế toán trưởng",
    "description": "Quản lý công nợ và xem danh sách người dùng",
    "userCount": 0,
    "status": "ACTIVE"
  }
}
```

---

### BƯỚC 4: Kiểm tra danh sách Vai trò & Số lượng người dùng (`userCount`)
```bash
curl -s -X GET "$BASE_URL/api/v1/admin/rbac/roles" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```
> **Kiểm tra**: Role `Kế toán trưởng` có trường `"userCount": 0`.

---

### BƯỚC 5: Gán Ma trận Quyền cho Vai trò (Diff & Replace)
> Giả sử gán quyền: Xem người dùng (`USER_VIEW`), Thêm người dùng (`USER_CREATE`), và Xem phòng ban (`DEPARTMENT_VIEW`):
```bash
curl -s -X PUT "$BASE_URL/api/v1/admin/rbac/role-permissions/roles/$NEW_ROLE_ID/permissions" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "permissionIds": [
      "8f2aa001-0000-0000-0000-000000000001",
      "8f2aa001-0000-0000-0000-000000000002",
      "8f2aa001-1111-4444-8888-000000000002"
    ]
  }'
```
> **Phản hồi**: `{"code":"SUCCESS","data":null}`.

---

### BƯỚC 6: Kiểm tra các Quyền đang có của Vai trò vừa gán
```bash
curl -s -X GET "$BASE_URL/api/v1/admin/rbac/role-permissions/roles/$NEW_ROLE_ID/permissions" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

### BƯỚC 7: Tạo User mới và gán trực tiếp vào Role & Phòng ban
```bash
curl -s -X POST "$BASE_URL/api/v1/admin/access-control/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ketoantruong01",
    "password": "Password123@",
    "email": "ketoan01@ledger.local",
    "phone": "0987654321",
    "fullName": "Trần Thị Mai",
    "roleId": "'"$NEW_ROLE_ID"'",
    "status": "ACTIVE"
  }'
```
> **Phản hồi**: Lưu `userId` vào `NEW_USER_ID`. Trả về `roleName: "Kế toán trưởng"`.

---

### BƯỚC 8: Kiểm tra lại `userCount` của Vai trò sau khi gán
```bash
curl -s -X GET "$BASE_URL/api/v1/admin/rbac/roles?keyword=KE_TOAN_TRUONG" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```
> **Kiểm tra**: Bây giờ `"userCount": 1` (đã tăng từ 0 lên 1).

---

### BƯỚC 9: Đăng nhập bằng User mới (`ketoantruong01`)
```bash
curl -s -X POST "$BASE_URL/api/v1/login/request-otp" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ketoantruong01",
    "password": "Password123@"
  }'
```
> Lưu `accessToken` trả về vào biến `USER_TOKEN`.

---

### BƯỚC 10: ⭐ Lấy Menu Sidebar Động của User (`ketoantruong01`)
```bash
curl -s -X GET "$BASE_URL/api/v1/admin/rbac/menu-permissions/me/authorized-menus" \
  -H "Authorization: Bearer $USER_TOKEN"
```
> **Kết quả kỳ vọng**:
> - Menu **Quản lý người dùng** và **Quản lý tổ chức** tự động xuất hiện.
> - Menu **Quản lý người dùng** có mảng actions chứa `CREATE` (vì có `USER_CREATE`), nhưng KHÔNG có `DELETE`.
> - Các menu khác (nhật ký, cấu hình) KHÔNG xuất hiện vì role này không có quyền.

---

### BƯỚC 11: Kiểm tra thực thi API của User:
#### 11.1. Gọi API có quyền (Search Users) -> Thành công:
```bash
curl -s -X POST "$BASE_URL/api/v1/admin/users/search" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "page": 1,
    "size": 10
  }'
```
> **Kết quả**: HTTP 200 OK.

#### 11.2. Gọi API KHÔNG có quyền (Delete User) -> Bị chặn:
```bash
curl -s -X DELETE "$BASE_URL/api/v1/admin/users/some-user-id" \
  -H "Authorization: Bearer $USER_TOKEN"
```
> **Kết quả**: HTTP 403 Forbidden.

---

### BƯỚC 12: Kiểm tra Nghiệp vụ Xóa Vai trò (Safe Role Deletion)

#### 12.1. Thử xóa Role khi ĐANG CÒN người dùng (`userCount = 1`):
```bash
curl -s -X DELETE "$BASE_URL/api/v1/admin/rbac/roles/$NEW_ROLE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```
> **Kết quả kỳ vọng**: HTTP **409 Conflict** (Khớp Modal cảnh báo Figma `2176:1851`).
```json
{
  "code": "ROLE_HAS_USERS",
  "message": "Vai trò đang có người dùng hoạt động, không thể xóa",
  "data": {
    "code": "ROLE_HAS_USERS",
    "roleId": "c0a80101-91ea-1234-8888-abcdef012345",
    "roleName": "Kế toán trưởng",
    "userCount": 1
  }
}
```

#### 12.2. Khóa tài khoản user rồi thực hiện xóa an toàn:
```bash
# 1. Khóa tài khoản user
curl -s -X PUT "$BASE_URL/api/v1/admin/users/$NEW_USER_ID/lock" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 2. Xóa user an toàn (Xác thực mật khẩu Admin và lý do xóa)
curl -s -X DELETE "$BASE_URL/api/v1/admin/users/$NEW_USER_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "adminPassword": "123456a@",
    "deleteReason": "Xóa tài khoản nhân viên thử việc"
  }'
```

#### 12.3. Xóa vai trò thành công sau khi không còn user (`userCount = 0`):
```bash
curl -s -X DELETE "$BASE_URL/api/v1/admin/rbac/roles/$NEW_ROLE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "deleteReason": "Hủy vai trò do tái cơ cấu phòng ban"
  }'
```
> **Kết quả kỳ vọng**: HTTP **200 OK** (Khớp Modal Figma `2176:1495`).

---

### BƯỚC 13: Cấu hình Menu mới gắn với Permission (Khi Dev tạo thêm module mới)
```bash
curl -s -X POST "$BASE_URL/api/v1/admin/rbac/menu-permissions" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "menuId": "uuid-cua-menu-san-pham",
    "permissionId": "8f2aa001-0000-0000-0000-000000000001",
    "displayAction": "VIEW"
  }'
```
