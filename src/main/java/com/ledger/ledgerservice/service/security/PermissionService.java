package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.model.dto.request.PermissionCreateRequest;
import com.ledger.ledgerservice.model.dto.request.PermissionSearchRequest;
import com.ledger.ledgerservice.model.dto.request.PermissionUpdateRequest;
import com.ledger.ledgerservice.model.dto.response.PageResponse;
import com.ledger.ledgerservice.model.dto.response.PermissionResponse;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import org.springframework.data.domain.Pageable;

public interface PermissionService {
    PermissionResponse create(PermissionCreateRequest request);
    PermissionResponse update(String permissionId, PermissionUpdateRequest request);
    PermissionResponse getById(String permissionId);
    PermissionResponse getByCode(String code);
    PageResponse<PermissionResponse> search(PermissionSearchRequest request, Pageable pageable);
    void delete(String permissionId);
    void changeStatus(String permissionId, RecordStatus status);
}