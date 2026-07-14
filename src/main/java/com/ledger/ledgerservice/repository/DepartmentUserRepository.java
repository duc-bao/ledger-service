package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.DepartmentUserEntity;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DepartmentUserRepository extends JpaRepository<DepartmentUserEntity, String> {
    List<DepartmentUserEntity> findByDepartmentId(String departmentId);

    List<DepartmentUserEntity> findByUserId(String userId);

    Optional<DepartmentUserEntity> findByDepartmentIdAndUserId(String departmentId, String userId);

    Optional<DepartmentUserEntity> findByIdAndStatus(String id, RecordStatus status);

    Optional<DepartmentUserEntity> findByDepartmentIdAndUserIdAndStatus(String departmentId, String userId, RecordStatus status);

    boolean existsByDepartmentIdAndUserId(String departmentId, String userId);

    void deleteByDepartmentId(String departmentId);

    void deleteByUserId(String userId);

    @Query("""
            SELECT COUNT(du) > 0 FROM DepartmentUserEntity du
            WHERE du.departmentId = :departmentId
              AND du.userId = :userId
              AND du.status = :status
            """)
    boolean existsActiveMembership(@Param("departmentId") String departmentId,
                                   @Param("userId") String userId,
                                   @Param("status") RecordStatus status);

    List<DepartmentUserEntity> findByUserIdAndStatus(String userId, RecordStatus status);

    @Query("""
            SELECT DISTINCT du.departmentId FROM DepartmentUserEntity du
            WHERE du.userId = :userId
              AND du.status = com.ledger.ledgerservice.model.enums.RecordStatus.ACTIVE
            """)
    Set<String> findActiveDepartmentIdsByUserId(@Param("userId") String userId);
}