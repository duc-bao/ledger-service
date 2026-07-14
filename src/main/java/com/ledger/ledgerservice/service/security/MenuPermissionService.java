package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.model.dto.request.MenuPermissionCreateRequest;
import com.ledger.ledgerservice.model.dto.response.AuthorizedMenuResponse;
import com.ledger.ledgerservice.model.dto.response.MenuPermissionResponse;

import java.util.List;

public interface MenuPermissionService {
    MenuPermissionResponse assign(MenuPermissionCreateRequest request);
    void revoke(String menuPermissionId);
    List<MenuPermissionResponse> getByMenu(String menuId);
    List<AuthorizedMenuResponse> getAuthorizedMenus(String userId);
}