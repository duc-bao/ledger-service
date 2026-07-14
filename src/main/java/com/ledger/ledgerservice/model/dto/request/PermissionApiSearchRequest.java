package com.ledger.ledgerservice.model.dto.request;

import com.ledger.ledgerservice.model.enums.PermissionMatchType;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Search criteria for permission API mappings")
public class PermissionApiSearchRequest extends SearchBaseRequest {
    @Schema(description = "Permission identifier")
    private String permissionId;

    @Schema(description = "Service code", example = "LEDGER_SERVICE")
    private String serviceCode;

    @Schema(description = "HTTP method", example = "GET")
    private String httpMethod;

    @Schema(description = "URI pattern fragment", example = "/api/v1/users")
    private String uriPattern;

    @Schema(description = "Match type", example = "ANT_PATH")
    private PermissionMatchType matchType;

    @Schema(description = "Status", example = "ACTIVE")
    private RecordStatus status;
}