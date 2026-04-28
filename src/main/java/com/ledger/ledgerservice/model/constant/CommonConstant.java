package com.ledger.ledgerservice.model.constant;

public class CommonConstant {
    public static final String EMPTY = "";
    public static final String USERNAME_SYSTEM = "system";
    public static final String LOCAL_IP = "127.0.0.1";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String PATTERN_USERNAME = "(^[a-z0-9_.-]*$)";
    public static final String PATTERN_EMAIL = "(^([a-zA-Z0-9_.+\\-])+@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$)";
    public static final String PATTERN_CITIZEN_NUMBER = "((^\\d{9,12}$))";
    public static final String PATTERN_PHONE_NUMBER = "(^\\d{10,15}$)";
    public static final String PATTERN_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d])[A-Za-z\\d[^A-Za-z\\d]]{8,}$";
    public static final String PATTERN_CODE = "^[a-zA-Z0-9_.-]*$";
    public static final String PATTERN_TAX_CODE = "^\\d{10,15}$";
    public static final int LOG_ROUNDS = 10;
}