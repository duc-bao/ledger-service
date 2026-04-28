package com.ledger.ledgerservice.service.audit;

import com.ledger.ledgerservice.model.dto.response.AuditLogItemResponse;
import com.ledger.ledgerservice.model.dto.response.AuditLogResponse;
import com.ledger.ledgerservice.model.entity.ActionLog;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {
    AuditLogItemResponse toItem(ActionLog actionLog);
    AuditLogResponse toDetail(ActionLog actionLog);

    List<AuditLogItemResponse> toItems(List<ActionLog> actionLogs);
}
