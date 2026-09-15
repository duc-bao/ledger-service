package com.ledger.ledgerservice.model.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateMyProfileRequest {
    @Size(max = 15, message = "validation.phone.length")
    @Schema(description = "Số điện thoại mới (không truyền hoặc bỏ trống để giữ nguyên)", example = "0987654321")
    private String phone;

    @Size(max = 100, message = "validation.fullName.length")
    @Schema(description = "Họ và tên mới (không truyền hoặc bỏ trống để giữ nguyên)", example = "Nguyễn Văn A")
    private String fullName;
}
