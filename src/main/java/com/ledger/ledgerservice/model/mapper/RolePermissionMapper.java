package com.ledger.ledgerservice.model.mapper;

import com.ledger.ledgerservice.model.dto.response.RolePermissionResponse;
import com.ledger.ledgerservice.model.entity.RolePermission;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RolePermissionMapper {
    RolePermissionResponse toResponse(RolePermission entity);
}