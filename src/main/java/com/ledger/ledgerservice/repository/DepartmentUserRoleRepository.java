package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.DepartmentUserRole;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DepartmentUserRoleRepository extends JpaRepository<DepartmentUserRole, String> {
    List<DepartmentUserRole> findByDepartmentUserId(String departmentUserId);

    List<DepartmentUserRole> findByDepartmentUserIdAndStatus(String departmentUserId, RecordStatus status);

    List<DepartmentUserRole> findByDepartmentUserIdIn(Collection<String> departmentUserIds);

    Optional<DepartmentUserRole> findByDepartmentUserIdAndGroupId(String departmentUserId, String groupId);

    boolean existsByDepartmentUserIdAndGroupId(String departmentUserId, String groupId);

    @Query("""
            SELECT dur FROM DepartmentUserRole dur
            JOIN DepartmentUserEntity du ON du.id = dur.departmentUserId
            WHERE du.userId = :userId
              AND du.departmentId = :departmentId
            ORDER BY dur.createdAt DESC
            """)
    List<DepartmentUserRole> findByUserIdAndDepartmentId(@Param("userId") String userId,
                                                         @Param("departmentId") String departmentId);

    boolean existsByGroupId(String groupId);
}