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
    USER_LOCKED("FORBIDDEN", "error.auth.userLocked"),
    USERNAME_EXISTS("EXISTED", "error.auth.usernameExists"),
    USER_EMAIL_EXISTS("EXISTED", "error.auth.userEmailExists"),
    USER_PHONE_EXISTS("EXISTED", "error.auth.userPhoneExists"),
    PASSWORD_INVALID("PASSWORD_INVALID", "error.auth.passwordInvalid"),
    USER_EMAIL_REQUIRED("USER_EMAIL_REQUIRED", "error.auth.userEmailRequired"),
    USER_INACTIVE("FORBIDDEN", "error.auth.userInactive"),
    OTP_RESEND_TOO_SOON("TOO_MANY_REQUESTS", "error.otp.resendTooSoon"),
    GROUP_NOT_FOUND("GROUP_NOT_FOUND", "error.access.groupNotFound"),
    GROUP_CODE_EXISTS("EXISTED", "error.access.groupCodeExists"),
    GROUP_NAME_EXISTS("EXISTED", "error.access.groupNameExists"),
    MENU_NOT_FOUND("MENU_NOT_FOUND", "error.access.menuNotFound"),
    MENU_CODE_EXISTS("EXISTED", "error.access.menuCodeExists"),
    MENU_IN_USE("CONFLICT", "error.access.menuInUse"),
    MENU_HAS_CHILDREN("CONFLICT", "error.access.menuHasChildren"),
    MENU_STATUS_INVALID("INVALID", "error.access.menuStatusInvalid"),
    MENU_PARENT_INVALID("INVALID", "error.access.menuParentInvalid"),
    USER_ID_REQUIRED("INPUT_INVALID", "error.access.userIdRequired"),
    GROUP_ID_REQUIRED("INPUT_INVALID", "error.access.groupIdRequired"),
    PERMISSION_NOT_FOUND("PERMISSION_NOT_FOUND", "error.access.permissionNotFound"),
    PERMISSION_CODE_EXISTS("EXISTED", "error.access.permissionCodeExists"),
    PERMISSION_IN_USE("CONFLICT", "error.access.permissionInUse"),
    ROLE_PERMISSION_NOT_FOUND("ROLE_PERMISSION_NOT_FOUND", "error.access.rolePermissionNotFound"),
    ROLE_PERMISSION_EXISTS("EXISTED", "error.access.rolePermissionExists"),
    PERMISSION_API_NOT_FOUND("PERMISSION_API_NOT_FOUND", "error.access.permissionApiNotFound"),
    PERMISSION_API_EXISTS("EXISTED", "error.access.permissionApiExists"),
    DEPARTMENT_USER_ROLE_NOT_FOUND("DEPARTMENT_USER_ROLE_NOT_FOUND", "error.access.departmentUserRoleNotFound"),
    DEPARTMENT_USER_ROLE_EXISTS("EXISTED", "error.access.departmentUserRoleExists"),
    MENU_PERMISSION_NOT_FOUND("MENU_PERMISSION_NOT_FOUND", "error.access.menuPermissionNotFound"),
    MENU_PERMISSION_EXISTS("EXISTED", "error.access.menuPermissionExists"),
    DEPARTMENT_USER_NOT_FOUND("DEPARTMENT_USER_NOT_FOUND", "error.department.userNotFound"),
    ROLE_SCOPE_INVALID("INVALID", "error.access.roleScopeInvalid"),
    PERMISSION_STATUS_INVALID("INVALID", "error.access.permissionStatusInvalid"),
    EFFECTIVE_TIME_RANGE_INVALID("INVALID", "error.access.effectiveTimeRangeInvalid"),
    ACCESS_DENIED("FORBIDDEN", "error.http.forbidden"),
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
    EXCEL_EXPORT_REPORT_NOT_SUPPORTED("INVALID", "error.excel.export.reportNotSupported"),
    EXCEL_EXPORT_TEMPLATE_INVALID("INVALID", "error.excel.export.templateInvalid"),
    EXCEL_EXPORT_FILE_EXPIRED("GONE", "error.excel.export.fileExpired"),
    EXCEL_EXPORT_CANCELLED("INVALID", "error.excel.export.cancelled"),
    COMPANY_NOT_FOUND("COMPANY_NOT_FOUND", "error.company.notFound"),
    COMPANY_CODE_EXISTS("EXISTED", "error.company.codeExists"),
    COMPANY_STATUS_INVALID("INVALID", "error.company.statusInvalid"),
    NOTIFICATION_NOT_FOUND("NOT_FOUND", "error.notification.notFound"),
    NOTIFICATION_RECIPIENT_REQUIRED("INPUT_INVALID", "error.notification.recipientRequired"),
    NOTIFICATION_RECIPIENT_NOT_FOUND("NOT_FOUND", "error.notification.recipientNotFound"),
    NOTIFICATION_LIST_FETCHED("SUCCESS", "success.notification.listFetched"),
    NOTIFICATION_CREATED("SUCCESS", "success.notification.created"),
    NOTIFICATION_UPDATED("SUCCESS", "success.notification.updated"),
    NOTIFICATION_MARKED_AS_READ("SUCCESS", "success.notification.markedAsRead"),
    NOTIFICATION_DELETED("SUCCESS", "success.notification.deleted"),
    EMAIL_GATEWAY_NOT_CONFIGURED("INVALID", "email.gateway.notfound"),
    EMAIL_GATEWAY_SUCCESS("SUCESS", "email.gateway.success"),
    EMAIL_GATEWAY_EXISTS("EXISTED", "email.gateway.exists"),

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

