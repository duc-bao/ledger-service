package com.ledger.ledgerservice.service.email;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.request.email.CreateEmailConfigRequest;
import com.ledger.ledgerservice.model.dto.request.email.UpdateEmailConfigRequest;
import com.ledger.ledgerservice.model.dto.response.email.EmailConfigResponse;
import com.ledger.ledgerservice.model.entity.EmailConfigEntity;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.mapper.EmailConfigMapper;
import com.ledger.ledgerservice.repository.EmailConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailConfigService {

    private final EmailConfigRepository emailConfigRepository;
    private final EmailConfigMapper emailConfigMapper;

    @Transactional(readOnly = true)
    public EmailConfigEntity getActiveConfig() {
        EmailConfigEntity entity = emailConfigRepository.findFirstByEnabledTrueAndIsDefaultTrue()
                .orElseThrow(() -> new BusinessException(MessageCode.EMAIL_GATEWAY_SUCCESS, HttpStatus.NOT_FOUND));
        log.info("Get Active email config: code={}, host={}, port={}", entity.getCode(), entity.getHost(), entity.getPort());

        return entity;
    }

    @Transactional(readOnly = true)
    public EmailConfigResponse getCurrentConfig() {
        EmailConfigEntity entity = getActiveConfig();
        log.info("Current email config: code={}, host={}, port={}", entity.getCode(), entity.getHost(), entity.getPort());
        return emailConfigMapper.toEmailConfigResponse(entity);
    }

    @Transactional(readOnly = true)
    public EmailConfigResponse getConfigById(String configId) {
        EmailConfigEntity entity = getEntityById(configId);
        return emailConfigMapper.toEmailConfigResponse(entity);
    }

    public void testConnection(CreateEmailConfigRequest request) {
        EmailConfigEntity entity = emailConfigMapper.toEmailConfigEntity(request);
        testConnection(entity);
    }

    public void testConnection(EmailConfigEntity config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getHost());
        if (config.getPort() != null) {
            mailSender.setPort(config.getPort());
        }
        mailSender.setUsername(config.getUsername());
        mailSender.setPassword(config.getPassword());
        if (StringUtils.hasText(config.getProtocol())) {
            mailSender.setProtocol(config.getProtocol());
        }
        if (StringUtils.hasText(config.getEncoding())) {
            mailSender.setDefaultEncoding(config.getEncoding());
        }

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", String.valueOf(Boolean.TRUE.equals(config.getAuthEnabled())));
        props.put("mail.smtp.starttls.enable", String.valueOf(Boolean.TRUE.equals(config.getStarttlsEnabled())));
        if (Boolean.TRUE.equals(config.getSslEnabled())) {
            props.put("mail.smtp.socketFactory.class", "jakarta.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", String.valueOf(config.getPort()));
        }
        int timeoutMs = config.getTimeoutMs() != null ? config.getTimeoutMs() : 5000;
        props.put("mail.smtp.timeout", String.valueOf(timeoutMs));
        props.put("mail.smtp.connectiontimeout", String.valueOf(timeoutMs));
        props.put("mail.smtp.writetimeout", String.valueOf(timeoutMs));

        try {
            mailSender.testConnection();
            log.info("Email gateway connection test successful for host={}:{}", config.getHost(), config.getPort());
        } catch (Exception ex) {
            log.error("Failed to connect to email gateway host={}:{} username={}", config.getHost(), config.getPort(), config.getUsername(), ex);
            throw new BusinessException(MessageCode.EMAIL_GATEWAY_CONNECTION_FAILED, HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public EmailConfigResponse createConfig(CreateEmailConfigRequest request) {
        log.info("Create email config: code={}, host={}, port={}, fromAddress={}", request.getCode(), request.getHost(), request.getPort(), request.getFromAddress());
        if (emailConfigRepository.existsByEnabledTrueAndIsDefaultTrue()) {
            throw new BusinessException(MessageCode.EMAIL_GATEWAY_EXISTS, HttpStatus.CONFLICT);
        }
        if (emailConfigRepository.findByCodeIgnoreCase(request.getCode().trim()).isPresent()) {
            throw new BusinessException(MessageCode.EMAIL_GATEWAY_EXISTS, HttpStatus.CONFLICT);
        }

        request.setCode(request.getCode().trim().toUpperCase());

        EmailConfigEntity entity =
                emailConfigMapper.toEmailConfigEntity(request);
        entity.setEnabled(Boolean.TRUE);
        entity.setIsDefault(Boolean.TRUE);

        testConnection(entity);

        entity = emailConfigRepository.save(entity);
        return emailConfigMapper.toEmailConfigResponse(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public EmailConfigResponse updateConfig(String configId, UpdateEmailConfigRequest request) {
        log.info("Update email config with id={}: host={}, port={}, fromAddress={}", configId, request.getHost(), request.getPort(), request.getFromAddress());
        EmailConfigEntity entity = getEntityById(configId);
        emailConfigMapper.updateEmailConfigEntity(request, entity);

        testConnection(entity);

        entity = emailConfigRepository.save(entity);
        return emailConfigMapper.toEmailConfigResponse(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(String configId) {
        EmailConfigEntity entity = getEntityById(configId);
        emailConfigRepository.delete(entity);
    }

    private EmailConfigEntity getEntityById(String configId) {
        if (!StringUtils.hasText(configId)) {
            throw new BusinessException(MessageCode.EMAIL_GATEWAY_NOT_CONFIGURED, HttpStatus.BAD_REQUEST);
        }
        return emailConfigRepository.findById(configId)
                .orElseThrow(() -> new BusinessException(MessageCode.EMAIL_GATEWAY_NOT_CONFIGURED, HttpStatus.NOT_FOUND));
    }


}