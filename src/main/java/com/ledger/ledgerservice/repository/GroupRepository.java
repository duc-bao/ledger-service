package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.Group;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, String> {
    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCase(String name);

    Optional<Group> findByCodeIgnoreCase(String code);

    Optional<Group> findByCodeIgnoreCaseAndStatus(String code, RecordStatus status);

    Optional<Group> findByIdAndStatus(String id, RecordStatus status);

    boolean existsByIdAndStatus(String id, RecordStatus status);

    List<Group> findByStatusOrderBySortOrderAscCodeAsc(RecordStatus status);
}