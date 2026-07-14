package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.Permission;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PermissionRepository extends JpaRepository<Permission, String>, JpaSpecificationExecutor<Permission> {
    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, String id);

    Optional<Permission> findByCodeIgnoreCase(String code);

    Optional<Permission> findByIdAndStatus(String id, RecordStatus status);

    List<Permission> findAllByIdIn(Collection<String> ids);

    List<Permission> findByCodeIn(Collection<String> codes);

    @Query("""
            SELECT p FROM Permission p
            JOIN RolePermission rp ON rp.permissionId = p.id
            JOIN Group g ON g.id = rp.groupId
            WHERE rp.groupId = :roleId
              AND rp.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND g.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND p.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
            ORDER BY p.moduleCode ASC, p.actionCode ASC, p.code ASC
            """)
    List<Permission> findActivePermissionsByRoleId(@Param("roleId") String roleId);

    @Query("""
            SELECT DISTINCT p FROM Permission p
            JOIN RolePermission rp ON rp.permissionId = p.id
            JOIN Group g ON g.id = rp.groupId
            JOIN DepartmentUserRole dur ON dur.groupId = g.id
            JOIN DepartmentUserEntity du ON du.id = dur.departmentUserId
            WHERE du.userId = :userId
              AND du.departmentId = :departmentId
              AND du.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND dur.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND g.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND rp.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND p.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
            ORDER BY p.moduleCode ASC, p.actionCode ASC, p.code ASC
            """)
    List<Permission> findActiveDepartmentPermissions(@Param("userId") String userId,
                                                     @Param("departmentId") String departmentId);

    @Query("""
            SELECT DISTINCT p.code
            FROM UserGroup ug
            JOIN Group g ON g.id = ug.groupId
            JOIN RolePermission rp ON rp.groupId = g.id
            JOIN Permission p ON p.id = rp.permissionId
            WHERE ug.userId = :userId
              AND ug.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND g.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND rp.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND p.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
            """)
    Set<String> findActiveGlobalPermissionCodes(@Param("userId") String userId);

    @Query("""
            SELECT DISTINCT p.code
            FROM DepartmentUserEntity du
            JOIN DepartmentUserRole dur ON dur.departmentUserId = du.id
            JOIN Group g ON g.id = dur.groupId
            JOIN RolePermission rp ON rp.groupId = g.id
            JOIN Permission p ON p.id = rp.permissionId
            WHERE du.userId = :userId
              AND du.departmentId = :departmentId
              AND du.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND dur.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND g.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND rp.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND p.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
            """)
    Set<String> findActiveDepartmentPermissionCodes(@Param("userId") String userId,
                                                    @Param("departmentId") String departmentId);

    @Query("""
            SELECT CASE WHEN COUNT(p.id) > 0 THEN TRUE ELSE FALSE END
            FROM UserGroup ug
            JOIN Group g ON g.id = ug.groupId
            JOIN RolePermission rp ON rp.groupId = g.id
            JOIN Permission p ON p.id = rp.permissionId
            WHERE ug.userId = :userId
              AND ug.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND g.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND rp.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND p.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND UPPER(p.code) = UPPER(:permissionCode)
            """)
    boolean existsActiveGlobalPermission(@Param("userId") String userId,
                                         @Param("permissionCode") String permissionCode);

    @Query("""
            SELECT CASE WHEN COUNT(p.id) > 0 THEN TRUE ELSE FALSE END
            FROM DepartmentUserEntity du
            JOIN DepartmentUserRole dur ON dur.departmentUserId = du.id
            JOIN Group g ON g.id = dur.groupId
            JOIN RolePermission rp ON rp.groupId = g.id
            JOIN Permission p ON p.id = rp.permissionId
            WHERE du.userId = :userId
              AND du.departmentId = :departmentId
              AND du.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND dur.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND g.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND rp.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND p.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND UPPER(p.code) = UPPER(:permissionCode)
            """)
    boolean existsActiveDepartmentPermission(@Param("userId") String userId,
                                             @Param("departmentId") String departmentId,
                                             @Param("permissionCode") String permissionCode);

    @Query("""
            SELECT CASE WHEN COUNT(p.id) > 0 THEN TRUE ELSE FALSE END
            FROM UserGroup ug
            JOIN Group g ON g.id = ug.groupId
            JOIN RolePermission rp ON rp.groupId = g.id
            JOIN Permission p ON p.id = rp.permissionId
            WHERE ug.userId = :userId
              AND ug.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND g.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND rp.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND p.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND p.id IN :permissionIds
            """)
    boolean existsAnyActiveGlobalPermission(@Param("userId") String userId,
                                            @Param("permissionIds") Collection<String> permissionIds);

    @Query("""
            SELECT CASE WHEN COUNT(p.id) > 0 THEN TRUE ELSE FALSE END
            FROM DepartmentUserEntity du
            JOIN DepartmentUserRole dur ON dur.departmentUserId = du.id
            JOIN Group g ON g.id = dur.groupId
            JOIN RolePermission rp ON rp.groupId = g.id
            JOIN Permission p ON p.id = rp.permissionId
            WHERE du.userId = :userId
              AND du.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND dur.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND g.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND rp.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND p.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
              AND p.id IN :permissionIds
            """)
    boolean existsAnyActiveDepartmentPermission(@Param("userId") String userId,
                                                @Param("permissionIds") Collection<String> permissionIds);

    @Query("""
            SELECT p.code
            FROM Permission p
            WHERE p.id IN :permissionIds
              AND p.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
            """)
    Set<String> findAllActiveCodesByIds(@Param("permissionIds") Collection<String> permissionIds);
}
