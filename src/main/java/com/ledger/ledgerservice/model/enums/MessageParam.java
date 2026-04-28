package com.ledger.ledgerservice.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum MessageParam {

    KEYWORD("keyword", "param.keyword"),
    PAGE_NUMBER("pageNumber", "param.pageNumber"),
    PAGE_SIZE("pageSize", "param.pageSize"),
    SORTS("sorts", "param.sorts"),
    ID("id", "param.id"),
    CODE("code", "param.code"),
    NAME("name", "param.name"),
    DESCRIPTION("description", "param.description"),
    STATUS("status", "param.status"),
    IS_DEFAULT("isDefault", "param.isDefault"),
    USERNAME("username", "param.username"),
    PASSWORD("password", "param.password"),
    PAGEABLE("pageable", "param.pageable"),
    FILE("file", "param.file"),
    OBJECT_TYPE("objectType", "param.objectType"),
    VALUE("value", "param.value"),
    START_DATE("startDate", "param.startDate"),
    COOKIE("cookie", "param.cookie"),
    REFRESH_TOKEN("refreshToken", "param.refreshToken"),
    TOKEN_PAYLOAD("tokenPayload", "param.tokenPayload"),
    TOKEN_SIGNATURE("tokenSignature", "param.tokenSignature"),
    TOKEN_ENCRYPTION("tokenEncryption", "param.tokenEncryption");

    private static final Map<String, MessageParam> CODE_ENUM = initMapOfCodeEnum();
    private final String code;
    private final String key;

    public static String getKey(String code) {
        return Optional.ofNullable(CODE_ENUM.get(code)).map(MessageParam::getKey).orElse(null);
    }

    private static Map<String, MessageParam> initMapOfCodeEnum() {
        return Arrays.stream(MessageParam.values()).collect(Collectors.toMap(MessageParam::getCode, param -> param));
    }

}
