package com.ledger.ledgerservice.model.mapper;

import com.ledger.ledgerservice.model.dto.response.DepartmentUserRoleResponse;
import com.ledger.ledgerservice.model.entity.DepartmentUserRole;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DepartmentUserRoleMapper {
    DepartmentUserRoleResponse toResponse(DepartmentUserRole entity);
}