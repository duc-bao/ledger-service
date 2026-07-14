package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.PermissionApi;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PermissionApiRepository extends JpaRepository<PermissionApi, String>, JpaSpecificationExecutor<PermissionApi> {
    boolean existsByPermissionIdAndHttpMethodIgnoreCaseAndUriPatternAndServiceCodeIgnoreCase(String permissionId,
                                                                                             String httpMethod,
                                                                                             String uriPattern,
                                                                                             String serviceCode);

    boolean existsByPermissionIdAndHttpMethodIgnoreCaseAndUriPatternAndServiceCodeIgnoreCaseAndIdNot(String permissionId,
                                                                                                      String httpMethod,
                                                                                                      String uriPattern,
                                                                                                      String serviceCode,
                                                                                                      String id);

    Optional<PermissionApi> findByIdAndStatus(String id, RecordStatus status);

    List<PermissionApi> findAllByHttpMethodIgnoreCaseAndStatusOrderByPriorityDescIdAsc(String httpMethod,
                                                                                        RecordStatus status);

    boolean existsByPermissionId(String permissionId);
}