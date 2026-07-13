package com.ledger.ledgerservice.model.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum UnitLevel {
    TONG_CONG_TY("Tổng công ty"),
    CHI_NHANH("Chi nhánh"),
    CONG_TY_ME("Công ty mẹ"),
    XI_NGHIEP("Xí nghiệp"),
    PHONG("Phòng");
    private final String name;

    UnitLevel(String name) {
        this.name = name;
    }

  public static   UnitLevel fromName(String name) {
        return Arrays.stream(UnitLevel.values())
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
