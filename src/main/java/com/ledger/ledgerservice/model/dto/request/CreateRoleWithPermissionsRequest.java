package com.ledger.ledgerservice.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateRoleWithPermissionsRequest {
    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String description;

    @NotEmpty
    @Valid
    private List<MenuActionRequest> menuActions;
}
