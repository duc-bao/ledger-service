package com.ledger.ledgerservice.model.dto.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuSeedItem {
    private String code;
    private String path;
    private String icon;
    private Integer offset;
    private String parentCode;
    private List<String> actions = new ArrayList<>();
}
