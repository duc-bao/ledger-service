package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.enums.UserStatus;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByPhone(String phone);
    List<User> findAllByStatus(UserStatus status);

    @Query("""
            SELECT u FROM User u
            WHERE
                (:keyword = '' OR
                 lower(u.username) LIKE concat('%', :keyword, '%') OR
                 lower(coalesce(u.fullName, '')) LIKE concat('%', :keyword, '%') OR
                 lower(coalesce(u.email, '')) LIKE concat('%', :keyword, '%') OR
                 lower(coalesce(u.phone, '')) LIKE concat('%', :keyword, '%'))
                AND (:status IS NULL OR u.status = :status)
            """)
    Page<User> searchUsers(@Param("keyword") String keyword, @Param("status") UserStatus status, Pageable pageable);

    @QueryHints(value = {
        @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE, value = "500"),
        @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_CACHEABLE, value = "false")
    })
    @Query("""
            SELECT new com.ledger.ledgerservice.model.dto.excel.UserExportRow(
                u.username, u.fullName, u.email, u.phone, u.userType, u.status, u.createdAt
            )
            FROM User u
            WHERE u.createdAt >= :startDate
              AND u.createdAt <= :endDate
            ORDER BY u.createdAt DESC
            """)
    java.util.stream.Stream<com.ledger.ledgerservice.model.dto.excel.UserExportRow> streamForExport(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
