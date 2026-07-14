package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.model.dto.request.PermissionApiCreateRequest;
import com.ledger.ledgerservice.model.dto.request.PermissionApiSearchRequest;
import com.ledger.ledgerservice.model.dto.request.PermissionApiUpdateRequest;
import com.ledger.ledgerservice.model.dto.response.PageResponse;
import com.ledger.ledgerservice.model.dto.response.PermissionApiResponse;
import org.springframework.data.domain.Pageable;

public interface PermissionApiService {
    PermissionApiResponse create(PermissionApiCreateRequest request);
    PermissionApiResponse update(String permissionApiId, PermissionApiUpdateRequest request);
    PermissionApiResponse getById(String permissionApiId);
    PageResponse<PermissionApiResponse> search(PermissionApiSearchRequest request, Pageable pageable);
    void delete(String permissionApiId);
}