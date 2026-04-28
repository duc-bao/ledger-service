package com.ledger.ledgerservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MenuActionRequest {
    @NotBlank
    private String menuId;

    @NotEmpty
    private List<String> actions;
}
