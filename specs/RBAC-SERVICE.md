# PROMPT IMPLEMENT RBAC SERVICE, DTO, MAPPER VÀ API THEO SCHEMA MỚI

Đọc kỹ tài liệu `specs/RBAC.md`, migration RBAC hiện tại và toàn bộ code liên quan đến permission, role, menu, user và department trong project.

Hãy **implement đầy đủ**, không chỉ lập kế hoạch.

## 1. Phạm vi schema chính thức

Chỉ sử dụng các bảng RBAC mới:

1. `idp_permission_definitions`
2. `idp_role_permissions`
3. `idp_permission_apis`
4. `org_department_user_roles`
5. `sys_menu_permissions`

Tiếp tục sử dụng:

- `idp_users`
- `idp_groups`
- `idp_user_groups`
- `org_departments`
- `org_department_users`
- `sys_menus`

Không triển khai, không tham chiếu và không tạo lại:

- `org_department_allowed_roles`
- `idp_authorization_audits`

Nếu entity, repository, service hoặc test của hai bảng này đã tồn tại thì xóa toàn bộ reference và bảo đảm project compile thành công.

## 2. Chuẩn hóa entity Permission

Entity `Permission` cũ mapping bảng legacy `idp_permissions` không còn được sử dụng. Hãy thay thế bằng một entity duy nhất mapping bảng:

```java
@Table(
    name = "idp_permission_definitions",
    indexes = {
        @Index(name = "idx_idp_permission_definitions_module_code", columnList = "module_code"),
        @Index(name = "idx_idp_permission_definitions_status", columnList = "status")
    }
)
```

Không tạo đồng thời `Permission` và `PermissionDefinition` nếu cùng đại diện cho bảng `idp_permission_definitions`.

Ưu tiên tên entity:

```java
Permission
```

Nếu project đang có `PermissionDefinition`, hãy kiểm tra toàn bộ reference rồi đổi thống nhất thành `Permission`, trừ khi việc đổi tên gây breaking change không cần thiết. Dù chọn tên nào, chỉ được có một entity và một repository cho bảng này.

Entity tối thiểu:

```java
@Entity
@Table(
    name = "idp_permission_definitions",
    indexes = {
        @Index(name = "idx_idp_permission_definitions_module_code", columnList = "module_code"),
        @Index(name = "idx_idp_permission_definitions_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Permission extends EntityBase {

    @Column(name = "code", length = 100, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "module_code", length = 50, nullable = false)
    private String moduleCode;

    @Column(name = "action_code", length = 50, nullable = false)
    private String actionCode;

    @Column(name = "resource_type", length = 50)
    private String resourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private CommonStatus status;
}
```

Điều chỉnh enum theo convention thực tế của project nhưng phải mapping đúng DDL.

Không giữ các field legacy:

- `menuId`
- `groupId`
- `userId`
- `discriminator`
- `actions`

## 3. Entity và repository cần giữ

Kiểm tra, sửa hoặc tạo đầy đủ:

- `Permission`
- `RolePermission`
- `PermissionApi`
- `DepartmentUserRole`
- `MenuPermission`
- `Group` hoặc `Role`
- `UserGroup` hoặc `UserRole`
- `Menu`
- `DepartmentUserEntity`

Không tạo:

- `DepartmentAllowedRole`
- `AuthorizationAudit`

Repository permission phải thống nhất, ví dụ:

```java
public interface PermissionRepository
        extends JpaRepository<Permission, String>,
                JpaSpecificationExecutor<Permission> {
}
```

Không giữ đồng thời `PermissionRepository` và `PermissionDefinitionRepository` nếu cùng quản lý `idp_permission_definitions`.

## 4. BusinessException và MessageCode

Mọi trường hợp không tìm thấy dữ liệu bắt buộc phải throw theo convention:

```java
throw new BusinessException(
    MessageCode.PERMISSION_NOT_FOUND,
    HttpStatus.NOT_FOUND
);
```

Không dùng:

- `RuntimeException`
- `IllegalArgumentException`
- `NoSuchElementException`
- trả `null`
- `.orElseThrow()` không truyền `BusinessException`

Bổ sung tối thiểu:

```java
PERMISSION_NOT_FOUND("NOT_FOUND", "error.access.permissionNotFound"),
PERMISSION_CODE_EXISTS("EXISTED", "error.access.permissionCodeExists"),
PERMISSION_IN_USE("CONFLICT", "error.access.permissionInUse"),
ROLE_PERMISSION_NOT_FOUND("NOT_FOUND", "error.access.rolePermissionNotFound"),
ROLE_PERMISSION_EXISTS("EXISTED", "error.access.rolePermissionExists"),
PERMISSION_API_NOT_FOUND("NOT_FOUND", "error.access.permissionApiNotFound"),
PERMISSION_API_EXISTS("EXISTED", "error.access.permissionApiExists"),
DEPARTMENT_USER_ROLE_NOT_FOUND("NOT_FOUND", "error.access.departmentUserRoleNotFound"),
DEPARTMENT_USER_ROLE_EXISTS("EXISTED", "error.access.departmentUserRoleExists"),
MENU_PERMISSION_NOT_FOUND("NOT_FOUND", "error.access.menuPermissionNotFound"),
MENU_PERMISSION_EXISTS("EXISTED", "error.access.menuPermissionExists"),
DEPARTMENT_USER_NOT_FOUND("NOT_FOUND", "error.department.userNotFound"),
ROLE_SCOPE_INVALID("INVALID", "error.access.roleScopeInvalid"),
PERMISSION_STATUS_INVALID("INVALID", "error.access.permissionStatusInvalid"),
EFFECTIVE_TIME_RANGE_INVALID("INVALID", "error.access.effectiveTimeRangeInvalid")
```

Bổ sung message key vào file i18n hiện tại.

## 5. Permission service

Tạo hoặc chuẩn hóa:

```java
PermissionService
PermissionServiceImpl
```

Nghiệp vụ tối thiểu:

```java
PermissionResponse create(PermissionCreateRequest request);
PermissionResponse update(String id, PermissionUpdateRequest request);
PermissionResponse getById(String id);
PermissionResponse getByCode(String code);
PageResponse<PermissionResponse> search(PermissionSearchRequest request, Pageable pageable);
void delete(String id);
void changeStatus(String id, CommonStatus status);
```

Quy tắc:

- `code`: trim, uppercase, chỉ nhận `[A-Z0-9_]`.
- Create phải kiểm tra `existsByCodeIgnoreCase`.
- Update phải kiểm tra duplicate code loại trừ chính entity hiện tại.
- Không tìm thấy phải throw `PERMISSION_NOT_FOUND`.
- Không hard delete permission đang được tham chiếu ở:
    - `idp_role_permissions`
    - `idp_permission_apis`
    - `sys_menu_permissions`
- Khi permission đang được sử dụng, throw `PERMISSION_IN_USE`.
- Ưu tiên chuyển status sang `INACTIVE`.
- Không tự ý xóa mapping liên quan khi update.

## 6. DTO Permission

Tạo:

- `PermissionCreateRequest`
- `PermissionUpdateRequest`
- `PermissionSearchRequest`
- `PermissionResponse`

Các field:

```java
String code;
String name;
String moduleCode;
String actionCode;
String resourceType;
CommonStatus status;
String description;
```

Response bổ sung:

```java
String id;
LocalDateTime createdAt;
String createdBy;
LocalDateTime updatedAt;
String updatedBy;
```

Dùng Jakarta Validation:

- `@NotBlank`
- `@Size`
- `@Pattern` cho code nếu phù hợp

Không trả entity trực tiếp từ controller.

## 7. MapStruct PermissionMapper

Dùng MapStruct đơn giản:

```java
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PermissionMapper {

    Permission toEntity(PermissionCreateRequest request);

    PermissionResponse toResponse(Permission entity);

    List<PermissionResponse> toResponses(List<Permission> entities);

    @BeanMapping(
        nullValuePropertyMappingStrategy =
            NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(
        PermissionUpdateRequest request,
        @MappingTarget Permission entity
    );
}
```

Nếu `EntityBase` có `version`, ignore khi update.

Normalize code trong service hoặc utility, không nhét expression phức tạp vào mapper.

## 8. PermissionRepository và Specification

Repository tối thiểu:

```java
boolean existsByCodeIgnoreCase(String code);

boolean existsByCodeIgnoreCaseAndIdNot(String code, String id);

Optional<Permission> findByCodeIgnoreCase(String code);

Optional<Permission> findByIdAndStatus(String id, CommonStatus status);

List<Permission> findAllByIdIn(Collection<String> ids);
```

Tạo `PermissionSpecification` hỗ trợ:

- keyword theo `code` hoặc `name`;
- `moduleCode`;
- `actionCode`;
- `resourceType`;
- `status`.

Không dùng native query cho search đơn giản.

## 9. RolePermission service

Tạo hoặc sửa:

```java
RolePermissionService
RolePermissionServiceImpl
```

Nghiệp vụ:

```java
RolePermissionResponse assign(RolePermissionCreateRequest request);
void revoke(String rolePermissionId);
List<PermissionResponse> getPermissionsByRole(String roleId);
void replaceRolePermissions(String roleId, RolePermissionReplaceRequest request);
```

Quy tắc:

- Role không tồn tại: `GROUP_NOT_FOUND`.
- Permission không tồn tại: `PERMISSION_NOT_FOUND`.
- Duplicate `(groupId, permissionId)`: `ROLE_PERMISSION_EXISTS`.
- Không tìm thấy mapping: `ROLE_PERMISSION_NOT_FOUND`.
- `effectiveTo` phải lớn hơn `effectiveFrom`.
- Không gán permission inactive.
- `replaceRolePermissions` chạy transaction, validate toàn bộ permission trước khi thay đổi.
- Không query từng permission trong vòng lặp.
- Không delete toàn bộ rồi insert lại nếu có thể tính phần thêm, giữ và loại bỏ.

Tạo DTO và mapper:

- `RolePermissionCreateRequest`
- `RolePermissionReplaceRequest`
- `RolePermissionResponse`
- `RolePermissionMapper`

## 10. PermissionApi service

Tạo hoặc sửa:

```java
PermissionApiService
PermissionApiServiceImpl
```

Nghiệp vụ:

```java
PermissionApiResponse create(PermissionApiCreateRequest request);
PermissionApiResponse update(String id, PermissionApiUpdateRequest request);
PermissionApiResponse getById(String id);
PageResponse<PermissionApiResponse> search(PermissionApiSearchRequest request, Pageable pageable);
void delete(String id);
```

Validation:

- Permission phải tồn tại.
- HTTP method hợp lệ và normalize uppercase.
- `uriPattern` không rỗng, không chứa query string.
- Không trùng `(permissionId, httpMethod, uriPattern, serviceCode)`.
- `matchType` phải là enum hợp lệ.
- Không dùng `startsWith()` để match URL runtime.

Không tìm thấy mapping phải throw:

```java
throw new BusinessException(
    MessageCode.PERMISSION_API_NOT_FOUND,
    HttpStatus.NOT_FOUND
);
```

Tạo:

- `PermissionApiCreateRequest`
- `PermissionApiUpdateRequest`
- `PermissionApiSearchRequest`
- `PermissionApiResponse`
- `PermissionApiMapper`

## 11. DepartmentUserRole service

Tạo hoặc sửa:

```java
DepartmentUserRoleService
DepartmentUserRoleServiceImpl
```

Nghiệp vụ:

```java
DepartmentUserRoleResponse assign(DepartmentUserRoleCreateRequest request);
void revoke(String id);
List<DepartmentUserRoleResponse> getByUserAndDepartment(String userId, String departmentId);
List<PermissionResponse> getEffectiveDepartmentPermissions(String userId, String departmentId);
```

Quy tắc:

- Tìm membership bằng `departmentUserId`.
- Không tìm thấy: `DEPARTMENT_USER_NOT_FOUND`.
- Membership phải active.
- Role phải tồn tại.
- Nếu role có `scopeType`, chỉ cho phép `DEPARTMENT` hoặc `BOTH`.
- Không duplicate `(departmentUserId, groupId)`.
- Validate effective time.
- Không dùng hoặc query bảng `org_department_allowed_roles`.

Tạo:

- `DepartmentUserRoleCreateRequest`
- `DepartmentUserRoleResponse`
- `DepartmentUserRoleMapper`

## 12. MenuPermission service

Tạo hoặc sửa:

```java
MenuPermissionService
MenuPermissionServiceImpl
```

Nghiệp vụ:

```java
MenuPermissionResponse assign(MenuPermissionCreateRequest request);
void revoke(String id);
List<MenuPermissionResponse> getByMenu(String menuId);
List<AuthorizedMenuResponse> getAuthorizedMenus(String userId, String departmentId);
```

Quy tắc:

- Menu phải tồn tại.
- Permission phải tồn tại.
- Không duplicate `(menuId, permissionId)`.
- Menu permission chỉ điều khiển UI.
- Không dùng menu permission để thay thế API authorization.

Tạo:

- `MenuPermissionCreateRequest`
- `MenuPermissionResponse`
- `AuthorizedMenuResponse`
- `MenuActionResponse`
- `MenuPermissionMapper`

Response menu trả permission code đầy đủ, ví dụ:

```json
{
  "id": "menu-id",
  "code": "USER_MANAGEMENT",
  "name": "Quản lý người dùng",
  "path": "/users",
  "permissions": [
    "USER_VIEW",
    "USER_CREATE"
  ],
  "actions": [
    {
      "code": "VIEW",
      "permissionCode": "USER_VIEW"
    }
  ],
  "children": []
}
```

## 13. Logic resolve quyền hiệu lực

Tạo hoặc chuẩn hóa:

```java
AccessControlQueryService
PermissionResolutionService
```

Không biến quyền department thành quyền global.

Dùng class thông thường, không dùng `record`:

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorizationContext {

    private Set<String> globalPermissions;

    private Map<String, Set<String>> departmentPermissions;

    private Set<String> legacyAuthorities;
}
```

Nghiệp vụ:

```java
Set<String> getGlobalPermissions(String userId);

Set<String> getDepartmentPermissions(
    String userId,
    String departmentId
);

boolean hasPermission(
    String userId,
    String permissionCode,
    String departmentId
);

boolean hasPermission(
    String userId,
    String method,
    String uri
);
```

Không tạo hoặc sử dụng hàm `getEffectivePermissions`.

Không kiểm tra `effectiveFrom` hoặc `effectiveTo` trong phase hiện tại. Chỉ cần các bản ghi liên quan có `status = ACTIVE`.

## Luồng kiểm tra permission code

```java
public boolean hasPermission(
        String userId,
        String permissionCode,
        String departmentId
) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(
            MessageCode.USER_NOT_FOUND,
            HttpStatus.NOT_FOUND
        ));

    if (!UserStatus.ACTIVE.equals(user.getStatus())) {
        return false;
    }

    boolean hasGlobalPermission =
        permissionRepository.existsActiveGlobalPermission(
            userId,
            permissionCode
        );

    if (hasGlobalPermission) {
        return true;
    }

    if (departmentId == null || departmentId.isBlank()) {
        return false;
    }

    return permissionRepository.existsActiveDepartmentPermission(
        userId,
        departmentId,
        permissionCode
    );
}
```

## Luồng kiểm tra quyền truy cập API

Sửa và implement đầy đủ hàm:

```java
public boolean hasPermission(
        String userId,
        String method,
        String uri
) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(
            MessageCode.USER_NOT_FOUND,
            HttpStatus.NOT_FOUND
        ));

    if (!UserStatus.ACTIVE.equals(user.getStatus())) {
        return false;
    }

    String normalizedMethod = normalizeHttpMethod(method);
    String normalizedUri = normalizeUri(uri);

    List<PermissionApi> apis =
        permissionApiRepository.findActiveByMethodAndUri(
            normalizedMethod,
            normalizedUri
        );

    if (apis == null || apis.isEmpty()) {
        return false;
    }

    Set<String> permissionCodes = apis.stream()
        .map(PermissionApi::getPermissionId)
        .filter(Objects::nonNull)
        .collect(Collectors.collectingAndThen(
            Collectors.toSet(),
            permissionIds ->
                permissionRepository.findAllActiveCodesByIds(
                    permissionIds
                )
        ));

    if (permissionCodes.isEmpty()) {
        return false;
    }

    boolean hasGlobalPermission =
        permissionRepository.existsAnyActiveGlobalPermission(
            userId,
            permissionCodes
        );

    if (hasGlobalPermission) {
        return true;
    }

    Set<String> departmentIds =
        departmentUserRepository.findActiveDepartmentIdsByUserId(
            userId
        );

    if (departmentIds == null || departmentIds.isEmpty()) {
        return false;
    }

    return permissionRepository.existsAnyActiveDepartmentPermission(
        userId,
        departmentIds,
        permissionCodes
    );
}
```

Có thể tối ưu bằng một query duy nhất thay vì load nhiều dữ liệu trung gian, nhưng logic bắt buộc phải tương đương:

1. Kiểm tra user tồn tại.
2. Kiểm tra user active.
3. Normalize HTTP method.
4. Normalize URI, loại bỏ query string và context path nếu project có.
5. Tìm các `PermissionApi` active phù hợp với method và URI.
6. Lấy các permission active được gắn với API.
7. Kiểm tra quyền global qua:
    - `idp_user_groups`
    - `idp_groups`
    - `idp_role_permissions`
    - `idp_permission_definitions`
8. Nếu không có quyền global, kiểm tra quyền theo phòng ban qua:
    - `org_department_users`
    - `org_department_user_roles`
    - `idp_groups`
    - `idp_role_permissions`
    - `idp_permission_definitions`
9. Chỉ chấp nhận các bản ghi có `status = ACTIVE`.
10. Không kiểm tra `effectiveFrom` và `effectiveTo`.
11. Nếu user có ít nhất một global role hoặc department role chứa permission yêu cầu thì trả `true`.
12. Nếu không có mapping API hoặc không có permission phù hợp thì trả `false`.
13. Không được dùng legacy menu authority để quyết định quyền truy cập API mới.
14. Không được dùng `startsWith()` đơn giản để match URI.

## Match URI

`findActiveByMethodAndUri(method, uri)` phải hỗ trợ `matchType`:

- `EXACT`
- `ANT_PATH`

Có thể triển khai theo hướng:

```java
List<PermissionApi> candidates =
    permissionApiRepository.findAllByHttpMethodAndStatus(
        normalizedMethod,
        CommonStatus.ACTIVE
    );

AntPathMatcher antPathMatcher = new AntPathMatcher();

List<PermissionApi> matchedApis = candidates.stream()
    .filter(api -> {
        if (PermissionApiMatchType.EXACT.equals(api.getMatchType())) {
            return normalizedUri.equals(api.getUriPattern());
        }

        if (PermissionApiMatchType.ANT_PATH.equals(api.getMatchType())) {
            return antPathMatcher.match(
                api.getUriPattern(),
                normalizedUri
            );
        }

        return false;
    })
    .toList();
```

Không dùng `record`; nếu cần class nội bộ hoặc DTO thì dùng class Lombok thông thường.

## Query repository cần có

Có thể đặt ở repository phù hợp, nhưng phải có các query tương đương:

```java
@Query("""
    select case when count(p.id) > 0 then true else false end
    from UserGroup ug
    join Group g on g.id = ug.groupId
    join RolePermission rp on rp.groupId = g.id
    join Permission p on p.id = rp.permissionId
    where ug.userId = :userId
      and ug.status = 'ACTIVE'
      and g.status = 'ACTIVE'
      and rp.status = 'ACTIVE'
      and p.status = 'ACTIVE'
      and p.code = :permissionCode
""")
boolean existsActiveGlobalPermission(
    String userId,
    String permissionCode
);
```

```java
@Query("""
    select case when count(p.id) > 0 then true else false end
    from DepartmentUserEntity du
    join DepartmentUserRole dur
      on dur.departmentUserId = du.id
    join Group g
      on g.id = dur.groupId
    join RolePermission rp
      on rp.groupId = g.id
    join Permission p
      on p.id = rp.permissionId
    where du.userId = :userId
      and du.departmentId = :departmentId
      and du.status = 'ACTIVE'
      and dur.status = 'ACTIVE'
      and g.status = 'ACTIVE'
      and rp.status = 'ACTIVE'
      and p.status = 'ACTIVE'
      and p.code = :permissionCode
""")
boolean existsActiveDepartmentPermission(
    String userId,
    String departmentId,
    String permissionCode
);
```

Có thể viết query `IN (:permissionCodes)` để kiểm tra nhiều permission API trong một lần.

Quy tắc:

- Department permission chỉ áp dụng cho user có membership active.
- Không cần `getEffectivePermissions`.
- Không kiểm tra thời gian hiệu lực.
- Role, role-permission, assignment, permission và membership phải active.
- User không tồn tại: `USER_NOT_FOUND`.
- Không có quyền: trả `false`, không throw exception.
- Không có API mapping: trả `false`.
- Không union legacy authority thành permission mới.

## 14. Query tối ưu

Không loại bỏ quan hệ RBAC chỉ vì nhiều bảng. Tối ưu bằng:

- index;
- projection;
- batch query;
- cache;
- tránh N+1;
- không query trong vòng lặp.

Repository resolve permission nên trả trực tiếp code:

```java
@Query("""
    select distinct p.code
    from UserGroup ug
    join Group g on g.id = ug.groupId
    join RolePermission rp on rp.groupId = g.id
    join Permission p on p.id = rp.permissionId
    where ug.userId = :userId
      and ug.status = :activeStatus
      and g.status = :activeStatus
      and rp.status = :activeStatus
      and p.status = :activeStatus
      and (ug.effectiveFrom is null or ug.effectiveFrom <= :now)
      and (ug.effectiveTo is null or ug.effectiveTo > :now)
      and (rp.effectiveFrom is null or rp.effectiveFrom <= :now)
      and (rp.effectiveTo is null or rp.effectiveTo > :now)
""")
Set<String> findGlobalPermissionCodes(...);
```

Điều chỉnh tên entity và enum theo code thực tế.

## 15. Controller Permission và Swagger

Tạo hoặc sửa controller đầy đủ:

```text
POST   /api/v1/permissions
PUT    /api/v1/permissions/{id}
GET    /api/v1/permissions/{id}
GET    /api/v1/permissions
PATCH  /api/v1/permissions/{id}/status
DELETE /api/v1/permissions/{id}
```

Controller phải khai báo Swagger/OpenAPI đầy đủ bằng `io.swagger.v3.oas.annotations`.

Ví dụ:

```java
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Tag(
    name = "RBAC Permission",
    description = "API quản lý danh mục quyền nghiệp vụ RBAC"
)
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @Operation(
        summary = "Tạo mới quyền",
        description = "Tạo một permission nghiệp vụ mới trong bảng idp_permission_definitions"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Tạo permission thành công"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dữ liệu đầu vào không hợp lệ"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Mã permission đã tồn tại"
        )
    })
    public ResponseEntity<ApiResponse<PermissionResponse>> create(
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                required = true,
                description = "Thông tin permission cần tạo"
            )
            PermissionCreateRequest request
    ) {
        PermissionResponse response =
            permissionService.create(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Cập nhật quyền",
        description = "Cập nhật thông tin permission theo ID"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Cập nhật thành công"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy permission"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Mã permission bị trùng"
        )
    })
    public ResponseEntity<ApiResponse<PermissionResponse>> update(
            @Parameter(
                description = "ID của permission",
                required = true
            )
            @PathVariable String id,
            @Valid @RequestBody PermissionUpdateRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(
                permissionService.update(id, request)
            )
        );
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Lấy chi tiết quyền",
        description = "Lấy permission theo ID"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lấy dữ liệu thành công"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy permission"
        )
    })
    public ResponseEntity<ApiResponse<PermissionResponse>> getById(
            @Parameter(
                description = "ID của permission",
                required = true
            )
            @PathVariable String id
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(
                permissionService.getById(id)
            )
        );
    }

    @GetMapping
    @Operation(
        summary = "Tìm kiếm danh sách quyền",
        description = "Tìm kiếm permission theo keyword, module, action, resourceType và status"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Tìm kiếm thành công"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Tham số phân trang hoặc tìm kiếm không hợp lệ"
        )
    })
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponse>>> search(
            @ParameterObject PermissionSearchRequest request,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(
                permissionService.search(request, pageable)
            )
        );
    }

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Thay đổi trạng thái quyền",
        description = "Chuyển permission sang ACTIVE hoặc INACTIVE"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Thay đổi trạng thái thành công"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy permission"
        )
    })
    public ResponseEntity<ApiResponse<Void>> changeStatus(
            @Parameter(
                description = "ID của permission",
                required = true
            )
            @PathVariable String id,
            @Valid @RequestBody PermissionStatusRequest request
    ) {
        permissionService.changeStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Xóa quyền",
        description = "Xóa hoặc vô hiệu hóa permission. Không cho phép xóa permission đang được sử dụng"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Xóa thành công"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy permission"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Permission đang được sử dụng"
        )
    })
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(
                description = "ID của permission",
                required = true
            )
            @PathVariable String id
    ) {
        permissionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
```

Điều chỉnh `ApiResponse`, `PageResponse` và response factory theo convention thực tế của project.

Swagger DTO phải dùng thêm:

- `@Schema(description = "...", example = "...")`
- `@Schema(requiredMode = Schema.RequiredMode.REQUIRED)` cho field bắt buộc
- `@ParameterObject` cho search request và pageable
- `@Parameter` cho path/query parameter
- `@Operation`
- `@ApiResponses`
- `@ApiResponse`
- `@Tag`

Các controller RBAC khác cũng phải có Swagger đầy đủ:

- `RolePermissionController`
- `PermissionApiController`
- `DepartmentUserRoleController`
- `MenuPermissionController`

Yêu cầu chung:

- `@Valid` cho request.
- Không trả entity trực tiếp.
- Dùng response wrapper hiện tại.
- Không bắt `BusinessException` trong controller nếu global exception handler đã xử lý.
- Mô tả rõ response code 200, 201, 400, 404 và 409 tùy API.
- Swagger phải thể hiện rõ request body, path variable, query parameter và mô tả nghiệp vụ.

## 16. Transaction

Dùng `@Transactional` cho:

- create/update/delete permission;
- assign/revoke role permission;
- replace role permissions;
- assign/revoke department role;
- assign/revoke menu permission.

Dùng `@Transactional(readOnly = true)` cho query.

## 17. Test bắt buộc

### Permission

- Create thành công.
- Trùng code.
- Get ID không tồn tại.
- Update không tồn tại.
- Search theo keyword/module/action/status.
- Không hard delete permission đang được sử dụng.

### RolePermission

- Assign thành công.
- Role không tồn tại.
- Permission không tồn tại.
- Duplicate.
- Effective range không hợp lệ.
- Role hoặc permission inactive.

### DepartmentUserRole

- Assign thành công.
- Membership không tồn tại.
- Membership inactive.
- Role scope không hợp lệ.
- Duplicate.
- Quyền chỉ hiệu lực tại đúng department.

### PermissionApi

- Create thành công.
- Duplicate.
- Normalize HTTP method.
- URI pattern invalid.
- Permission không tồn tại.

### MenuPermission

- Assign thành công.
- Duplicate.
- Menu không tồn tại.
- Permission không tồn tại.
- Authorized menu trả đúng action.

### Permission resolution

- Resolve global permission.
- Resolve department permission.
- Không xuất hiện ở department khác.
- Assignment hết hạn không được tính.
- Role permission hết hạn không được tính.
- Membership inactive không được tính.
- Legacy authority không trở thành global permission mới.


## 17.1. Test riêng cho kiểm tra truy cập API

Bổ sung test cho hàm:

```java
boolean hasPermission(String userId, String method, String uri);
```

Tối thiểu gồm:

1. User không tồn tại:
    - throw `USER_NOT_FOUND`.

2. User inactive:
    - trả `false`.

3. Không có API mapping:
    - trả `false`.

4. API mapping inactive:
    - trả `false`.

5. Permission inactive:
    - trả `false`.

6. User có global role chứa permission của API:
    - trả `true`.

7. User không có global role nhưng có `DepartmentUserRole` active chứa permission:
    - trả `true`.

8. User có membership inactive:
    - trả `false`.

9. Department user role inactive:
    - trả `false`.

10. Role inactive:
    - trả `false`.

11. RolePermission inactive:
    - trả `false`.

12. URI `EXACT` match đúng:
    - trả `true`.

13. URI `EXACT` không match:
    - trả `false`.

14. URI `ANT_PATH` match đúng:
    - trả `true`.

15. URI có query string:
    - normalize trước khi match.

16. HTTP method lowercase:
    - normalize uppercase trước khi query.

17. User có legacy authority nhưng không có permission RBAC:
    - trả `false`.

18. Nhiều PermissionApi cùng match:
    - chỉ cần user có ít nhất một permission tương ứng thì trả `true`.

19. User có nhiều department role:
    - kiểm tra toàn bộ department role active của user.

20. Không kiểm tra effectiveFrom/effectiveTo:
    - bản ghi active vẫn được tính bất kể hai field này.

## 18. Kiểm tra cuối

Sau khi implement:

1. Xóa import/class/repository không còn dùng.
2. Chạy format.
3. Chạy compile.
4. Chạy unit test.
5. Chạy integration test.
6. Kiểm tra không còn reference đến:
    - `org_department_allowed_roles`
    - `idp_authorization_audits`
    - entity permission legacy mapping `idp_permissions`
7. Chỉ có một entity mapping `idp_permission_definitions`.
8. Chỉ có một repository quản lý Permission.
9. MapStruct generate thành công.
10. Sửa toàn bộ lỗi compile/test phát sinh.

## 19. Báo cáo đầu ra

Sau khi implement, báo cáo:

1. Entity đã xóa.
2. Entity đã đổi tên.
3. Entity đã tạo hoặc cập nhật.
4. Repository đã xóa, tạo hoặc cập nhật.
5. Service đã tạo hoặc cập nhật.
6. DTO request/response đã tạo.
7. Mapper đã tạo.
8. Controller/API đã tạo hoặc cập nhật.
9. MessageCode đã bổ sung.
10. File i18n đã bổ sung.
11. Logic legacy còn giữ.
12. Logic legacy đã loại bỏ.
13. Query resolve permission.
14. Test đã thêm.
15. Kết quả compile.
16. Kết quả test.
17. Technical debt còn lại.

## 20. Nguyên tắc bắt buộc

- Không dừng ở việc phân tích; hãy implement đầy đủ.
- Không tạo lại hai bảng đã loại bỏ.
- Không tạo duplicate Permission entity.
- Không dùng bảng legacy `idp_permissions` làm permission catalog.
- Không trả entity trực tiếp từ controller.
- Dùng MapStruct đơn giản.
- Không trả null khi dữ liệu bắt buộc phải tồn tại.
- Không tìm thấy phải throw `BusinessException` với `MessageCode` phù hợp và `HttpStatus.NOT_FOUND`.
- Không dùng exception chung chung.
- Không biến quyền department thành quyền global.
- Không dùng menu permission để bảo vệ backend API.
- Không tự ý thay đổi ngoài phạm vi RBAC.
- Không xóa migration hoặc dữ liệu production cũ.