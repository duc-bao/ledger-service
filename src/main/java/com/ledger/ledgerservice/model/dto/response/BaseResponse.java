package com.ledger.ledgerservice.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BaseResponse<T> {
    private String requestId;
    private String description;
    private String code;
    private T data;
    private Boolean success;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private OffsetDateTime timestamp;
    private Integer statusCode;
    private MetaDataResp metaData;

    public static <T> BaseResponse<T> success(T data, String requestId, String description, String code, Integer statusCode) {
        return BaseResponse.<T>builder().success(true).data(data)
                .timestamp(OffsetDateTime.now())
                .requestId(requestId)
                .description(description)
                .code(code)
                .data(data)
                .statusCode(statusCode)
                .build();
    }

    public static <T> BaseResponse<T> success(String requestId, String description, String code, Integer statusCode) {
        return BaseResponse.<T>builder()
                .success(true)
                .timestamp(OffsetDateTime.now())
                .statusCode(statusCode)
                .requestId(requestId)
                .description(description)
                .code(code)
                .build();
    }

    public static <T> BaseResponse<T> error(String requestId, String description, String code, Integer statusCode) {
        return BaseResponse.<T>builder()
                .success(false)
                .timestamp(OffsetDateTime.now())
                .statusCode(statusCode)
                .requestId(requestId)
                .description(description)
                .code(code)
                .build();
    }
}
