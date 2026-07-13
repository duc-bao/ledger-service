package com.ledger.ledgerservice.model.enums;

import lombok.Getter;

@Getter
public enum CompanyType {
    TONG_CONG_TY("Tổng công ty"),
    CHI_NHANH("Chi nhánh"),
    CONG_TY_ME("Công ty mẹ"),
    XI_NGHIEP("Xí nghiệp"),
    PHONG("Phòng")
    ;
    private final String name;
    CompanyType(String name) {
        this.name = name;
    }
}
