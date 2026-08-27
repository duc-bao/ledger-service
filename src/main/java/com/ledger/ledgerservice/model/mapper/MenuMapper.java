package com.ledger.ledgerservice.model.mapper;

import com.ledger.ledgerservice.model.dto.request.MenuCreateRequest;
import com.ledger.ledgerservice.model.dto.request.MenuUpdateRequest;
import com.ledger.ledgerservice.model.dto.response.MenuResponse;
import com.ledger.ledgerservice.model.entity.Menu;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MenuMapper {
    Menu toEntity(MenuCreateRequest request);

    MenuResponse toResponse(Menu entity);

    List<MenuResponse> toResponses(List<Menu> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "ancestors", ignore = true)
    void updateEntity(MenuUpdateRequest request, @MappingTarget Menu entity);
}
