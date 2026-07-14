Đặc tả cơ sở dữ liệu RBAC

Phiên bản: 0.1 (bản review)Phạm vi: Quản lý người dùng, phòng ban, nhóm quyền, quyền API và menu UI.Nguyên tắc: Backend là lớp quyết định bảo mật. Menu/nút chỉ là dữ liệu trình bày được suy ra từ quyền backend.

1. Quyết định thiết kế

Giữ nguyên các bảng hiện hữu idp_users, idp_groups -> idp_roles, idp_user_groups-> idp_user_roles, idp_permissions, org_departments, org_department_users, sys_menus để giảm ảnh hưởng migration.

Trong tài liệu, Group = Role. Thay đổi luôn tên bảng idp_groups = idp_roles, nhưng class/API mới nên dùng thuật ngữ Role để tránh nhầm với "nhóm người dùng".

idp_permissions hiện tại lưu trực tiếp menu_id, role_id, user_id, actions. Cách này trộn nhiều loại quyền trong một bảng và không phù hợp để kiểm soát API. Bảng này nên ngừng dùng cho RBAC mới; dữ liệu được migrate sang các bảng chuẩn bên dưới.

Role chỉ được cấp trực tiếp cho một user: cấp toàn hệ thống qua idp_user_roles, hoặc cấp trong một phòng ban qua bảng mới org_department_user_roles.

Không tự động cấp toàn bộ role của phòng ban cho mọi user trong phòng ban.

sys_menus và quyền API tách riêng. UI lấy menu/action từ quyền hiệu lực; request API luôn được kiểm tra ở backend.

2. Lược đồ quan hệ

erDiagram
IDP_USERS ||--o{ IDP_USER_GROUPS : "global role"
IDP_GROUPS ||--o{ IDP_USER_GROUPS : "assigned"
IDP_USERS ||--o{ ORG_DEPARTMENT_USERS : "belongs to"
ORG_DEPARTMENTS ||--o{ ORG_DEPARTMENT_USERS : "has member"
ORG_DEPARTMENT_USERS ||--o{ ORG_DEPARTMENT_USER_ROLES : "role in department"
IDP_GROUPS ||--o{ ORG_DEPARTMENT_USER_ROLES : "assigned"
IDP_GROUPS ||--o{ IDP_ROLE_PERMISSIONS : "has"
IDP_PERMISSIONS ||--o{ IDP_ROLE_PERMISSIONS : "included"
IDP_PERMISSIONS ||--o{ IDP_PERMISSION_APIS : "protects"
SYS_MENUS ||--o{ SYS_MENU_PERMISSIONS : "requires"
IDP_PERMISSIONS ||--o{ SYS_MENU_PERMISSIONS : "enables"

3. Quy ước chung

id là VARCHAR(36) / UUID theo GenerationType.UUID, giữ đúng EntityBase hiện hữu.

Các cột audit: created_at, created_by, updated_at, updated_by, description kế thừa từ EntityBase, trừ khi bảng mapping tối giản không kế thừa.

Khuyến nghị dùng foreign key ở database. Nếu hệ thống đang chưa dùng FK vì lý do chia module hoặc migration, vẫn phải giữ các unique constraint/index và validate tính tồn tại ở service.

Không xóa cứng role, permission, menu đã có liên kết. Dùng status = ACTIVE/INACTIVE; chỉ được hard delete khi không còn bản ghi tham chiếu.

Mã (code) dùng chữ hoa, dấu gạch dưới: USER_VIEW, ROLE_ASSIGN; không đổi sau khi đã phát hành.

4. Các bảng hiện hữu giữ lại và điều chỉnh

4.1 idp_users — giữ nguyên

Giữ nguyên toàn bộ cột hiện tại. Bổ sung 2 cột để hỗ trợ vòng đời và khóa tài khoản:

Cột

Kiểu gợi ý

Null

Mặc định

Ghi chú

last_login_at

TIMESTAMP

Có



Lần đăng nhập thành công gần nhất

locked_until

TIMESTAMP

Có



Khóa tạm thời sau nhiều lần đăng nhập sai; NULL là không khóa

Giữ status với enum tối thiểu: ACTIVE, INACTIVE, LOCKED. user_type chỉ giữ khi có ý nghĩa nghiệp vụ rõ ràng; không dùng user_type để thay RBAC.

4.2 idp_groups -> idp_roles — giữ bảng, đổi nghĩa nghiệp vụ thành Role

Tên vật lý giữ nguyên để tránh ảnh hưởng code cũ. Tên entity/API mới đề xuất: Role.

Cột

Trạng thái

Ghi chú

id + audit + description

Giữ

Từ EntityBase

code

Giữ, NOT NULL, unique

Mã role, ví dụ SUPER_ADMIN

name

Giữ, NOT NULL, unique

Tên hiển thị

is_default

Giữ

Chỉ dùng để gợi ý role khi tạo user; không tự gán nếu chưa có rule rõ ràng

is_super_admin

Giữ

Chỉ SUPER_ADMIN có giá trị true; không cho sửa qua API thông thường

status

Bổ sung

ACTIVE / INACTIVE

sort_order

Bổ sung

Thứ tự hiển thị role, mặc định 0

Ràng buộc: UNIQUE(code), UNIQUE(name); index (status).

4.3 idp_user_groups -> idp_user_roles — giữ, xác định là role toàn hệ thống

Đây là bảng user_roles ở tầng nghiệp vụ: role không phụ thuộc vào một phòng ban cụ thể.

Cột

Trạng thái

Ghi chú

id, group_id, user_id

Giữ

group_id tham chiếu idp_groups.id

effective_from

Bổ sung

Bắt đầu hiệu lực, mặc định thời điểm gán

effective_to

Bổ sung

Kết thúc hiệu lực, NULL là chưa hết hạn

status

Bổ sung

ACTIVE / INACTIVE

Ràng buộc: UNIQUE(user_id, group_id); FK tới idp_users, idp_groups; index (user_id, status) và (group_id, status).

4.4 org_departments — giữ nguyên

Giữ cấu trúc cây hiện tại (parent_id, ancestors, tree_level). manager_user_id tham chiếu user quản lý phòng ban về mặt tổ chức, không tự sinh quyền quản trị.

Nên bổ sung FK tự tham chiếu cho parent_id, FK cho manager_user_id (nếu database đang cho phép), và unique/index cho code đã có.

4.5 org_department_users — giữ nguyên

Đây là quan hệ membership giữa user và phòng ban. Không lưu role ở bảng này để một user có thể có nhiều role tại cùng phòng ban.

Cột

Trạng thái

Ghi chú

Tất cả cột hiện tại

Giữ



status

Bổ sung

ACTIVE / INACTIVE; left_date không thay thế status

Ràng buộc cần thêm: UNIQUE(department_id, user_id). Nếu một user chỉ có một phòng ban chính, enforce duy nhất is_primary = true theo user_id bằng partial unique index (PostgreSQL).

4.6 sys_menus — giữ, thu gọn trách nhiệm về UI navigation

sys_menus không còn là nơi quyết định quyền. Cột actions hiện tại nên deprecate vì action phải được trả về theo permission hiệu lực, không phải được gán thẳng vào menu.

Cột

Trạng thái

Ghi chú

id, audit, code, path, parent_id, ancestors, icon, offset

Giữ

offset nên đổi tên Java property thành menuOrder, vẫn map cột cũ để tránh migration

actions

Ngừng dùng dần

Có thể giữ tạm để tương thích UI cũ; không dùng trong authorize

name

Bổ sung

Tên hiển thị, NOT NULL, tối đa 150

menu_type

Bổ sung

MODULE, MENU, BUTTON; mặc định MENU

component

Bổ sung

Tên component/route frontend, nullable

visible

Bổ sung

Hiển thị trong sidebar, mặc định true

status

Bổ sung

ACTIVE / INACTIVE

external_url

Bổ sung (tùy chọn)

URL ngoài nếu menu_type = MENU

Ràng buộc: UNIQUE(code), index (parent_id, offset), (status, visible).

4.7 idp_permissions — thay thế mục đích, migrate dữ liệu

Thiết kế hiện tại (menu_id, group_id, user_id, discriminator, actions) làm cả ba việc: định nghĩa quyền, gán quyền cho role/user và gắn quyền UI. Điều này gây khó cho kiểm tra API, unique và audit.

Đề xuất: tạo bảng mới idp_permission_api cho định nghĩa quyền; sau khi migration hoàn tất

4.8 org_department_permissions — ngừng dùng cho RBAC mới

Bảng hiện tại cấp trực tiếp action theo menu cho toàn phòng ban. Điều này mâu thuẫn với quyết định không tự động cấp quyền cho tất cả user của phòng ban.

Không ghi mới vào bảng này.

Dữ liệu chỉ dùng để tham khảo/migrate thành org_department_allowed_roles hoặc thành role cụ thể sau khi xác nhận nghiệp vụ.

Không xóa ngay trong release đầu tiên; đánh dấu deprecated và loại bỏ sau khi API/UI cũ đã chuyển đổi.

5. Bảng mới bắt buộc

5.1 idp_permission

Định nghĩa một quyền nghiệp vụ nguyên tử. Đây là bảng được gọi là permissions ở tầng nghiệp vụ.

Cột

Kiểu gợi ý

Null

Mặc định

Ghi chú

id

VARCHAR(36)

Không

UUID

PK, từ EntityBase

code

VARCHAR(100)

Không



Unique; ví dụ USER_CREATE

name

VARCHAR(200)

Không



Tên hiển thị

module_code

VARCHAR(50)

Không



Ví dụ USER_MANAGEMENT

action_code

VARCHAR(50)

Không



VIEW, CREATE, UPDATE, DELETE, EXPORT, ASSIGN_ROLE

resource_type

VARCHAR(50)

Có



Ví dụ USER, DEPARTMENT; hỗ trợ kiểm soát phạm vi sau này

status

VARCHAR(20)

Không

ACTIVE

ACTIVE / INACTIVE

Audit + description







Từ EntityBase

Ràng buộc/index: UNIQUE(code), UNIQUE(module_code, action_code), index (module_code), (status).

5.2 idp_role_permissions

Gán permission cho role.

Cột

Kiểu gợi ý

Null

Ghi chú

id

VARCHAR(36)

Không

PK + audit từ EntityBase

group_id

VARCHAR(36)

Không

FK idp_groups.id

permission_id

VARCHAR(36)

Không

FK idp_permission_definitions.id

status

VARCHAR(20)

Không

ACTIVE / INACTIVE

effective_from

TIMESTAMP

Có

Thời điểm bắt đầu hiệu lực

effective_to

TIMESTAMP

Có

Thời điểm hết hiệu lực

Ràng buộc/index: UNIQUE(group_id, permission_id), index (group_id, status), (permission_id, status).

5.3 idp_permission_apis

Map permission nghiệp vụ với API endpoint. Một permission có thể map nhiều API; một API có thể yêu cầu nhiều permission khi chính sách cần đồng thời nhiều điều kiện.

Cột

Kiểu gợi ý

Null

Mặc định

Ghi chú

id

VARCHAR(36)

Không

UUID

PK + audit

permission_id

VARCHAR(36)

Không



FK permission

http_method

VARCHAR(10)

Không



GET, POST, PUT, PATCH, DELETE

uri_pattern

VARCHAR(300)

Không



Mẫu route Spring: /api/v1/users/{id}

match_type

VARCHAR(20)

Không

EXACT

EXACT hoặc ANT_PATH; ưu tiên exact pattern đã chuẩn hóa

service_code

VARCHAR(50)

Không

LEDGER_SERVICE

Cần thiết nếu database dùng chung nhiều service

status

VARCHAR(20)

Không

ACTIVE



priority

INT

Không

0

Dùng khi có route pattern chồng nhau

Thêm cột isAllow nếu api đó là public api. 

Ràng buộc/index: UNIQUE(permission_id, http_method, uri_pattern, service_code), index (service_code, http_method, status), (uri_pattern).

Quy tắc route: lưu URI đã bỏ context path và query string; không dùng URI thực tế như /users/123; dùng /users/{id}. Endpoint public (/auth/login, health check) được lưu thêm cột isAllow 

5.4 org_department_user_roles

Gán role cho user trong đúng một phòng ban. Đây là bảng thay cho ý tưởng department_roles tự cấp quyền toàn phòng ban.

Cột

Kiểu gợi ý

Null

Ghi chú

id

VARCHAR(36)

Không

PK + audit

department_user_id

VARCHAR(36)

Không

FK org_department_users.id

group_id

VARCHAR(36)

Không

FK idp_groups.id

status

VARCHAR(20)

Không

ACTIVE / INACTIVE

effective_from

TIMESTAMP

Có



effective_to

TIMESTAMP

Có



Ràng buộc/index: UNIQUE(department_user_id, group_id), index (department_user_id, status), (group_id, status).

5.5 sys_menu_permissions

Map menu/nút UI với permission. Không dùng bảng này để authorize API.

Cột

Kiểu gợi ý

Null

Mặc định

Ghi chú

id

VARCHAR(36)

Không

UUID

PK + audit

menu_id

VARCHAR(36)

Không



FK sys_menus.id

permission_id

VARCHAR(36)

Không



FK permission

display_action

VARCHAR(50)

Có



Action frontend: VIEW, CREATE, EXPORT; null cho menu chỉ cần một quyền

status

VARCHAR(20)

Không

ACTIVE



Ràng buộc/index: UNIQUE(menu_id, permission_id), index (menu_id, status), (permission_id, status).

6. DDL minh họa (PostgreSQL)

DDL dưới đây chỉ cho các bảng/cột bổ sung. Kiểu VARCHAR(36) phù hợp với UUID String theo entity hiện tại.

alter table idp_users add column if not exists last_login_at timestamp;
alter table idp_users add column if not exists locked_until timestamp;

alter table idp_groups add column if not exists status varchar(20) not null default 'ACTIVE';
alter table idp_groups add column if not exists sort_order integer not null default 0;
alter table idp_groups alter column code set not null;
alter table idp_groups alter column name set not null;

alter table idp_user_groups add column if not exists effective_from timestamp;
alter table idp_user_groups add column if not exists effective_to timestamp;
alter table idp_user_groups add column if not exists status varchar(20) not null default 'ACTIVE';
create unique index if not exists uk_idp_user_groups_user_group on idp_user_groups(user_id, group_id);

alter table org_department_users add column if not exists status varchar(20) not null default 'ACTIVE';
create unique index if not exists uk_org_department_users_department_user
on org_department_users(department_id, user_id);
create unique index if not exists uk_org_department_users_primary
on org_department_users(user_id) where is_primary = true and status = 'ACTIVE';

alter table sys_menus add column if not exists name varchar(150);
alter table sys_menus add column if not exists menu_type varchar(20) not null default 'MENU';
alter table sys_menus add column if not exists component varchar(200);
alter table sys_menus add column if not exists visible boolean not null default true;
alter table sys_menus add column if not exists status varchar(20) not null default 'ACTIVE';
alter table sys_menus add column if not exists external_url varchar(500);

create table if not exists idp_permission_definitions (
id varchar(36) primary key,
code varchar(100) not null unique,
name varchar(200) not null,
module_code varchar(50) not null,
action_code varchar(50) not null,
resource_type varchar(50),
status varchar(20) not null default 'ACTIVE',
description varchar(500),
created_at timestamp not null default current_timestamp,
created_by varchar(100),
updated_at timestamp not null default current_timestamp,
updated_by varchar(100),
constraint uk_permission_module_action unique (module_code, action_code)
);
create index if not exists idx_permission_module on idp_permission_definitions(module_code);
create index if not exists idx_permission_status on idp_permission_definitions(status);

create table if not exists idp_role_permissions (
id varchar(36) primary key,
group_id varchar(36) not null references idp_groups(id),
permission_id varchar(36) not null references idp_permission_definitions(id),
status varchar(20) not null default 'ACTIVE',
effective_from timestamp,
effective_to timestamp,
description varchar(500),
created_at timestamp not null default current_timestamp,
created_by varchar(100),
updated_at timestamp not null default current_timestamp,
updated_by varchar(100),
constraint uk_role_permission unique (group_id, permission_id)
);

create table if not exists idp_permission_apis (
id varchar(36) primary key,
permission_id varchar(36) not null references idp_permission_definitions(id),
http_method varchar(10) not null,
uri_pattern varchar(300) not null,
match_type varchar(20) not null default 'EXACT',
service_code varchar(50) not null default 'LEDGER_SERVICE',
status varchar(20) not null default 'ACTIVE',
priority integer not null default 0,
description varchar(500),
created_at timestamp not null default current_timestamp,
created_by varchar(100),
updated_at timestamp not null default current_timestamp,
updated_by varchar(100),
constraint uk_permission_api unique (permission_id, http_method, uri_pattern, service_code)
);
create index if not exists idx_permission_apis_lookup
on idp_permission_apis(service_code, http_method, status);

create table if not exists org_department_user_roles (
id varchar(36) primary key,
department_user_id varchar(36) not null references org_department_users(id),
group_id varchar(36) not null references idp_groups(id),
status varchar(20) not null default 'ACTIVE',
effective_from timestamp,
effective_to timestamp,
description varchar(500),
created_at timestamp not null default current_timestamp,
created_by varchar(100),
updated_at timestamp not null default current_timestamp,
updated_by varchar(100),
constraint uk_department_user_role unique (department_user_id, group_id)
);

create table if not exists sys_menu_permissions (
id varchar(36) primary key,
menu_id varchar(36) not null references sys_menus(id),
permission_id varchar(36) not null references idp_permission_definitions(id),
display_action varchar(50),
status varchar(20) not null default 'ACTIVE',
description varchar(500),
created_at timestamp not null default current_timestamp,
created_by varchar(100),
updated_at timestamp not null default current_timestamp,
updated_by varchar(100),
constraint uk_menu_permission unique (menu_id, permission_id)
);

8. Cách kiểm tra quyền runtime

8.1 API authorization

Xác thực JWT/session và lấy user_id.

Từ method + route pattern, tìm permission trong idp_permission_apis đang ACTIVE.

Lấy role hiệu lực từ:

idp_user_groups đang active, chưa hết hạn;

org_department_user_roles của các org_department_users đang active, chưa hết hạn.

Qua idp_role_permissions, lấy permission hiệu lực của các role.

User có đủ permission yêu cầu thì cho qua; không có trả 403 Forbidden.

Với API có phạm vi dữ liệu phòng ban, service tiếp tục kiểm tra record đích thuộc phòng ban nào. RBAC xác định được làm gì; kiểm tra scope xác định được làm trên dữ liệu nào.

8.2 Trả menu cho frontend

API GET /api/v1/me/menus lấy permission hiệu lực của user, join sys_menu_permissions và sys_menus, sau đó:

Trả menu active/visible mà user có ít nhất một permission liên quan.

Trả actions dựa trên display_action của permission user có trên menu đó.

Trả đủ parent menu của menu con để frontend dựng cây điều hướng.

Không trả permission/role nội bộ không cần thiết cho frontend.

9. Dữ liệu mẫu

Role: USER_ADMIN
├─ USER_VIEW
├─ USER_CREATE
├─ USER_UPDATE
├─ USER_DELETE
├─ USER_ASSIGN_ROLE
└─ USER_EXPORT

Permission: USER_CREATE
└─ POST /api/v1/users

Menu: USER_MANAGEMENT
├─ USER_VIEW       → action VIEW
├─ USER_CREATE     → action CREATE
├─ USER_UPDATE     → action UPDATE
├─ USER_DELETE     → action DELETE
├─ USER_ASSIGN_ROLE → action ASSIGN_ROLE
└─ USER_EXPORT     → action EXPORT

10. Kế hoạch migration an toàn

Tạo bảng/cột mới, chưa thay đổi luồng authorize cũ.

Tạo permission definitions từ tổ hợp menu + action hiện có. Ví dụ USER_MANAGEMENT + CREATE thành USER_CREATE.

Tạo role permissions từ các record idp_permissions có group_id.

Cấu hình idp_permission_apis cho toàn bộ endpoint cần bảo vệ và kiểm thử bằng danh sách route từ Spring Actuator/OpenAPI.

Chuyển phân quyền user trực tiếp cũ (idp_permissions.user_id) thành role riêng nếu thực sự cần; tránh tiếp tục cấp permission trực tiếp user vì sẽ làm lệch RBAC. Ví dụ tạo role CUSTOM_USER_ABC chỉ khi có ngoại lệ đã được phê duyệt.

Diễn giải dữ liệu org_department_permissions cùng nghiệp vụ: migrate thành role theo user hoặc danh sách role được phép của phòng ban; không tự chuyển thành quyền của tất cả user.

Chạy song song cơ chế cũ/mới ở môi trường test; đối chiếu quyền thực tế và menu trả về.

Chuyển Security Filter/Interceptor sang idp_permission_apis + role permissions.

Ngừng ghi vào idp_permissions, org_department_permissions, sys_menus.actions; giữ read-only trong một chu kỳ phát hành rồi mới xóa/migrate bảng legacy.

11. Điểm cần chốt trước khi implement

Một user có thể thuộc nhiều phòng ban không? Tạm thời thì 1 user chỉ thuộc 1 phòng ban và 1 phòng ban thì có nhiều user, và 1 phòng ban thì có phòng ban con ở đó.  Bản spec hỗ trợ có.

Một user có thể có nhiều phòng ban chính không? Đề xuất: không.

Role ở phòng ban khác có cho phép gọi API dùng dữ liệu toàn hệ thống không? Đề xuất: không; cần kiểm tra data scope theo department.

Có cần cấp quyền ngoại lệ trực tiếp cho user không? Đề xuất: không ở phiên bản đầu; tạo role riêng thay vì user_permissions.

Có bao nhiêu service cùng dùng database quyền? Nếu nhiều service, bắt buộc quản lý service_code trong idp_permission_apis.