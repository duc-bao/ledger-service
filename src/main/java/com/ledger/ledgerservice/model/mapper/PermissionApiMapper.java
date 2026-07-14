package com.ledger.ledgerservice.model.mapper;

import com.ledger.ledgerservice.model.dto.request.PermissionApiCreateRequest;
import com.ledger.ledgerservice.model.dto.request.PermissionApiUpdateRequest;
import com.ledger.ledgerservice.model.dto.response.PermissionApiResponse;
import com.ledger.ledgerservice.model.entity.PermissionApi;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PermissionApiMapper {
    PermissionApi toEntity(PermissionApiCreateRequest request);

    PermissionApiResponse toResponse(PermissionApi entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(PermissionApiUpdateRequest request, @MappingTarget PermissionApi entity);
}