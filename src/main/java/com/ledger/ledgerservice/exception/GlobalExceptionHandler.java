package com.ledger.ledgerservice.exception;

import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageHelper messageHelper;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<Object>> handleBusinessException(BusinessException ex) {
        String template = resolveMessage(ex.getMessageKey(), ex.getArgs());
        String description = template;

        if (ex.getParamKey() != null && !ex.getParamKey().isBlank()) {
            String paramMsg = resolveMessage(ex.getParamKey());
            description = template.replace("{param}", paramMsg);
        }

        String code = ex.getCode() != null ? ex.getCode() : MessageCode.FAIL.getCode();
        RequestContextHolder.setCtxError(code, description);

        HttpStatus status = ex.getStatusCode() != null ? ex.getStatusCode() : HttpStatus.BAD_REQUEST;
        log.warn("BusinessException handled: code={}, status={}, messageKey={}, requestId={}",
                code, status.value(), ex.getMessageKey(), RequestContextHolder.getRequestId(), ex);
        BaseResponse<Object> body = BaseResponse.error(RequestContextHolder.getRequestId(), description, code, status.value());
        body.setData(ex.getData());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleException(Exception ex) {
        String code = MessageCode.INTERNAL_SERVER_ERROR.getCode();
        String description = resolveMessage(MessageCode.INTERNAL_SERVER_ERROR.getKey());
        RequestContextHolder.setCtxError(code, description);
        log.error("Unhandled exception: requestId={}", RequestContextHolder.getRequestId(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.error(RequestContextHolder.getRequestId(), description, code, HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String key = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(MessageCode.INPUT_INVALID.getKey());

        String code = MessageCode.INPUT_INVALID.getCode();
        String description = resolveMessage(key);
        RequestContextHolder.setCtxError(code, description);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(RequestContextHolder.getRequestId(), description, code, HttpStatus.BAD_REQUEST.value()));
    }

    private String resolveMessage(String key) {
        return resolveMessage(key, new Object[]{});
    }

    private String resolveMessage(String key, Object... args) {
        if (key == null || key.isBlank()) {
            return "";
        }

        try {
            if (args != null && args.length > 0) {
                return messageHelper.getMsg(key, args);
            }
            return messageHelper.getMsg(key, key);
        } catch (Exception ignored) {
            return key;
        }
    }
}
