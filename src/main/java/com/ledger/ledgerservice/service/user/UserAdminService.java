package com.ledger.ledgerservice.service.user;

import com.ledger.ledgerservice.model.dto.request.AdminUserSearchRequest;
import com.ledger.ledgerservice.model.dto.response.AdminUserDetailResponse;
import com.ledger.ledgerservice.model.dto.response.AdminUserItemResponse;
import org.springframework.data.domain.Page;

public interface UserAdminService {
    Page<AdminUserItemResponse> getUsers(AdminUserSearchRequest request);

    AdminUserDetailResponse getUserDetail(String userId);

    void lockUser(String userId);

    void unlockUser(String userId);

    void deleteUser(String userId);
}
