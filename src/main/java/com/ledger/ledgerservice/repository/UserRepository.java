package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByPhone(String phone);

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
}
