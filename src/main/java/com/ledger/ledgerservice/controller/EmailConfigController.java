package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.email.CreateEmailConfigRequest;
import com.ledger.ledgerservice.model.dto.request.email.UpdateEmailConfigRequest;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.email.EmailConfigResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.email.EmailConfigService;
import com.ledger.ledgerservice.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/email-configs")
@RequiredArgsConstructor
@Tag(name = "Email Config", description = "APIs for email configuration management")
@SecurityRequirement(name = "bearerAuth")
public class EmailConfigController {

    private final EmailConfigService emailConfigService;
    private final ResponseHelper responseHelper;

    @GetMapping("/current")
    @Operation(summary = "Get current active email config")
    public ResponseEntity<BaseResponse<EmailConfigResponse>> getCurrentConfig() {
        EmailConfigResponse response = emailConfigService.getCurrentConfig();
        return responseHelper.ok(MessageCode.SUCCESS, response, HttpStatus.OK);
    }

    @GetMapping("/{configId}")
    @Operation(summary = "Get email config by id")
    public ResponseEntity<BaseResponse<EmailConfigResponse>> getConfigById(@PathVariable String configId) {
        EmailConfigResponse response = emailConfigService.getConfigById(configId);
        return responseHelper.ok(MessageCode.SUCCESS, response, HttpStatus.OK);
    }

    @PostMapping("/test-connection")
    @Operation(summary = "Test SMTP connection", description = "Verify that SMTP server credentials and connectivity are valid before saving")
    public ResponseEntity<BaseResponse<String>> testConnection(@Valid @RequestBody CreateEmailConfigRequest request) {
        emailConfigService.testConnection(request);
        return responseHelper.ok(MessageCode.EMAIL_GATEWAY_SUCCESS, "Connection successful", HttpStatus.OK);
    }

    @PostMapping
    @Operation(summary = "Create email config")
    public ResponseEntity<BaseResponse<EmailConfigResponse>> createConfig(@Valid @RequestBody CreateEmailConfigRequest request) {
        EmailConfigResponse response = emailConfigService.createConfig(request);
        return responseHelper.ok(MessageCode.SUCCESS, response, HttpStatus.CREATED);
    }

    @PutMapping("/{configId}")
    @Operation(summary = "Update email config")
    public ResponseEntity<BaseResponse<EmailConfigResponse>> updateConfig(
            @PathVariable String configId,
            @Valid @RequestBody UpdateEmailConfigRequest request
    ) {
        EmailConfigResponse response = emailConfigService.updateConfig(configId, request);
        return responseHelper.ok(MessageCode.SUCCESS, response, HttpStatus.OK);
    }

    @DeleteMapping("/{configId}")
    @Operation(summary = "Delete email config")
    public ResponseEntity<BaseResponse<Object>> deleteConfig(@PathVariable String configId) {
        emailConfigService.deleteConfig(configId);
        return responseHelper.ok(MessageCode.SUCCESS, HttpStatus.OK);
    }
}
