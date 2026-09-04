package com.ledger.ledgerservice.service.menu;

import com.ledger.ledgerservice.model.dto.request.MenuCreateRequest;
import com.ledger.ledgerservice.model.dto.request.MenuSearchRequest;
import com.ledger.ledgerservice.model.dto.request.MenuUpdateRequest;
import com.ledger.ledgerservice.model.dto.response.MenuResponse;
import com.ledger.ledgerservice.model.dto.response.PageResponse;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MenuService {
    MenuResponse create(MenuCreateRequest request);

    MenuResponse update(String menuId, MenuUpdateRequest request);

    MenuResponse getById(String menuId);

    MenuResponse getByCode(String code);

    PageResponse<MenuResponse> search(MenuSearchRequest request, Pageable pageable);

    List<MenuResponse> getTree(RecordStatus status, Boolean visibleOnly);

    void changeStatus(String menuId, RecordStatus status);

    void delete(String menuId);
}
