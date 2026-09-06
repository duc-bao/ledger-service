package com.ledger.ledgerservice.model.mapper;

import com.ledger.ledgerservice.model.dto.request.email.CreateEmailConfigRequest;
import com.ledger.ledgerservice.model.dto.request.email.UpdateEmailConfigRequest;
import com.ledger.ledgerservice.model.dto.response.email.EmailConfigResponse;
import com.ledger.ledgerservice.model.entity.EmailConfigEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface EmailConfigMapper {

    @Mapping(target = "passwordConfigured", expression = "java(emailConfigEntity.getPassword() != null && !emailConfigEntity.getPassword().isBlank())")
    EmailConfigResponse toEmailConfigResponse(EmailConfigEntity emailConfigEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "isDefault", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EmailConfigEntity toEmailConfigEntity(CreateEmailConfigRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "password", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmailConfigEntity(UpdateEmailConfigRequest request, @MappingTarget EmailConfigEntity emailConfigEntity);

}
