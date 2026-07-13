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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

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
        log.info("Get Active email config: {}", entity.toString());

        return entity;
    }

    @Transactional(readOnly = true)
    public EmailConfigResponse getCurrentConfig() {
        EmailConfigEntity entity = getActiveConfig();
        log.info("Current email config: {}", entity.toString());
        return emailConfigMapper.toEmailConfigResponse(entity);
    }

    @Transactional(readOnly = true)
    public EmailConfigResponse getConfigById(String configId) {
        EmailConfigEntity entity = getEntityById(configId);
        return emailConfigMapper.toEmailConfigResponse(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public EmailConfigResponse createConfig(CreateEmailConfigRequest request) {
        log.info("Create email config: {}", request.toString());
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

        entity = emailConfigRepository.save(entity);
        return emailConfigMapper.toEmailConfigResponse(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public EmailConfigResponse updateConfig(String configId, UpdateEmailConfigRequest request) {
        log.info("Update email config with id={} body= {}", configId, request.toString());
        EmailConfigEntity entity = getEntityById(configId);
        emailConfigMapper.updateEmailConfigEntity(request, entity);
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