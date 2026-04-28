package com.ledger.ledgerservice.util;

import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResponseHelper {

    private final MessageHelper msgHelper;

    public <T> ResponseEntity<BaseResponse<T>> ok(MessageCode code, HttpStatus status) {
        String msg = msgHelper.getMsg(code.getKey());
        BaseResponse<T> response = BaseResponse.success(RequestContextHolder.getRequestId(), msg, code.getCode(), status.value());

        return ResponseEntity.status(status).body(response);
    }

    public <T> ResponseEntity<BaseResponse<T>> ok(MessageCode code, T data, HttpStatus status) {
        String msg = msgHelper.getMsg(code.getKey());
        BaseResponse<T> response = BaseResponse.success(
                data,
                RequestContextHolder.getRequestId(),
                msg,
                code.getCode(),
                status.value()
        );
        return ResponseEntity.status(status).body(response);
    }

    public <T> ResponseEntity<BaseResponse<T>> ok(MessageCode code, T data) {
        return ok(code, data, HttpStatus.OK);
    }

}
