package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthorizedMenuResponse {
    private String menuId;
    private String code;
    private String displayName;
    private String path;
    private String icon;
    private String parentId;
    private Integer offset;
    private List<String> actions;
}
