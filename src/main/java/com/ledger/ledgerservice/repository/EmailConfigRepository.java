package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.EmailConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailConfigRepository extends JpaRepository<EmailConfigEntity, String> {
    Optional<EmailConfigEntity> findByCodeIgnoreCase(String code);

    Optional<EmailConfigEntity> findFirstByEnabledTrueAndIsDefaultTrue();

    boolean existsByEnabledTrueAndIsDefaultTrue();

    List<EmailConfigEntity> findByProviderTypeIgnoreCaseAndEnabledTrue(String providerType);

    List<EmailConfigEntity> findByEnabledTrueOrderByIsDefaultDescNameAsc();
}
