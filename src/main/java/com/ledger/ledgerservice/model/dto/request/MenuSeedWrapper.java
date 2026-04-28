package com.ledger.ledgerservice.model.dto.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuSeedWrapper {
    private List<MenuSeedItem> menus = new ArrayList<>();
}
