package com.ledger.ledgerservice.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;

@Getter
@RequiredArgsConstructor
public enum MessageCode {

    // ===== COMMON =====
    SUCCESS("SUCCESS", "success"),
    FAIL("FAIL", "fail"),
    INPUT_INVALID("INPUT_INVALID", "error.invalid"),
    TEXT_MIN_LENGTH("INPUT_INVALID", "error.text.minLength"),
    TEXT_MAX_LENGTH("INPUT_INVALID", "error.text.maxLength"),
    TEXT_LENGTH_BETWEEN("INPUT_INVALID", "error.text.lengthBetween"),
    DIGIT_MIN_VALUE("INPUT_INVALID", "error.digit.minValue"),
    DIGIT_MAX_VALUE("INPUT_INVALID", "error.digit.maxValue"),
    DIGIT_VALUE_BETWEEN("INPUT_INVALID", "error.digit.valueBetween"),

    // ===== SPECIFIC =====
    TOKEN_EXPIRED("TOKEN_EXPIRED", "error.expired"),
    TOKEN_INVALID("TOKEN_INVALID", "error.invalid"),
    NOT_EXISTS("NOT_EXISTS", "error.notExists"),
    EXISTED("EXISTED", "error.existed"),
    CODE_INVALID("INVALID", "error.code.invalid"),
    NAME_INVALID("INVALID", "error.name.invalid"),
    START_DATE_EXCEEDS_END_DATE("INVALID", "error.startDate.exceedsEndDate"),
    SEARCH_DATE_RANGE_INVALID("INVALID", "error.searchDateRange.invalid"),
    NEW_PASSWORD_DIFFERENT_REQUIRED("REQUIRED", "error.newPassword.differentRequired"),
    CONFIRM_PASSWORD_NOT_MATCH("INVALID", "error.confirmPassword.notMatch"),
    OTP_NO_ATTEMPTS_LEFT("INVALID", "error.otp.noAttemptsLeft"),
    OTP_INVALID("OTP_INVALID", "error.otp.invalid"),
    USER_NOT_FOUND("USER_NOT_FOUND", "error.auth.userNotFound"),
    USERNAME_EXISTS("EXISTED", "error.auth.usernameExists"),
    USER_EMAIL_EXISTS("EXISTED", "error.auth.userEmailExists"),
    USER_PHONE_EXISTS("EXISTED", "error.auth.userPhoneExists"),
    PASSWORD_INVALID("PASSWORD_INVALID", "error.auth.passwordInvalid"),
    USER_EMAIL_REQUIRED("USER_EMAIL_REQUIRED", "error.auth.userEmailRequired"),
    GROUP_NOT_FOUND("GROUP_NOT_FOUND", "error.access.groupNotFound"),
    GROUP_CODE_EXISTS("EXISTED", "error.access.groupCodeExists"),
    GROUP_NAME_EXISTS("EXISTED", "error.access.groupNameExists"),
    MENU_NOT_FOUND("MENU_NOT_FOUND", "error.access.menuNotFound"),
    USER_ID_REQUIRED("INPUT_INVALID", "error.access.userIdRequired"),
    GROUP_ID_REQUIRED("INPUT_INVALID", "error.access.groupIdRequired"),
    OTP_SENT("OTP_SENT", "success.otpSent"),
    USER_PROFILE_FETCHED("SUCCESS", "success.user.profileFetched"),
    USER_PROFILE_UPDATED("SUCCESS", "success.user.profileUpdated"),
    PASSWORD_CHANGED("SUCCESS", "success.user.passwordChanged"),
    LOGOUT_SUCCESS("SUCCESS", "success.auth.logout"),
    EXCEL_UPLOAD_SUCCESS("SUCCESS", "success.excel.upload"),
    EXCEL_EXPORT_SUCCESS("SUCCESS", "success.excel.export"),
    EXCEL_EXPORT_REQUEST_ACCEPTED("SUCCESS", "success.excel.export.requestAccepted"),
    EXCEL_FILE_EMPTY("INPUT_INVALID", "error.excel.fileEmpty"),
    EXCEL_FILE_INVALID_TYPE("INPUT_INVALID", "error.excel.invalidFileType"),
    EXCEL_EXPORT_JOB_NOT_FOUND("NOT_FOUND", "error.excel.export.jobNotFound"),
    EXCEL_EXPORT_NOT_READY("INVALID", "error.excel.export.notReady"),
    EXCEL_EXPORT_FAILED("INVALID", "error.excel.export.failed"),

    // ===== HTTP STATUS =====
    BAD_REQUEST("BAD_REQUEST", "error.http.badRequest"),
    UNAUTHORIZED("UNAUTHORIZED", "error.http.unauthorized"),
    FORBIDDEN("FORBIDDEN", "error.http.forbidden"),
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED", "error.http.methodNotAllowed"),
    CONFLICT("CONFLICT", "error.http.conflict"),
    UNSUPPORTED_MEDIA_TYPE("UNSUPPORTED_MEDIA_TYPE", "error.http.unsupportedMediaType"),
    TOO_MANY_REQUESTS("TOO_MANY_REQUESTS", "error.http.tooManyRequests"),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "error.http.internalServerError"),
    BAD_GATEWAY("BAD_GATEWAY", "error.http.badGateway"),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "error.http.serviceUnavailable"),
    GATEWAY_TIMEOUT("GATEWAY_TIMEOUT", "error.http.gatewayTimeout"),
    NOT_FOUND("NOT_FOUND", "error.http.notFound");

    private final String code;
    private final String key;

    public static MessageCode get(String name) {
        return EnumUtils.getEnum(MessageCode.class, name);
    }

}
