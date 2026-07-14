package com.ledger.ledgerservice.model.mapper;

import com.ledger.ledgerservice.model.dto.response.MenuPermissionResponse;
import com.ledger.ledgerservice.model.entity.MenuPermission;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MenuPermissionMapper {
    MenuPermissionResponse toResponse(MenuPermission entity);
}