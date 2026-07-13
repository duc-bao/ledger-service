package com.ledger.ledgerservice.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum NotificationReadStatus {
    UNREAD("UNREAD"),
    READ("READ");

    private final String code;

    public static NotificationReadStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown notification read status: " + code));
    }
}
