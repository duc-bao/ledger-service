package com.ledger.ledgerservice.service.excel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.ledgerservice.exception.BusinessExportException;
import com.ledger.ledgerservice.model.dto.excel.ColumnDefinition;
import com.ledger.ledgerservice.model.dto.excel.ExcelTemplateDefinition;
import com.ledger.ledgerservice.model.enums.MessageCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelTemplateService {
    private final ObjectMapper objectMapper;
    private final Map<String, ExcelTemplateDefinition> templateCache = new ConcurrentHashMap<>();

    private static final Set<String> FORBIDDEN_FIELDS = Set.of(
            "password", "passwd", "token", "secret", "hash", "key", "credential", "salt"
    );

    public ExcelTemplateDefinition getTemplate(String templateCode) {
        return templateCache.computeIfAbsent(templateCode, this::loadAndValidateTemplate);
    }

    private ExcelTemplateDefinition loadAndValidateTemplate(String templateCode) {
        try {
            String path = "excel-template/" + templateCode + ".json";
            ClassPathResource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                throw new BusinessExportException(MessageCode.EXCEL_EXPORT_TEMPLATE_INVALID, templateCode);
            }
            try (InputStream is = resource.getInputStream()) {
                ExcelTemplateDefinition template = objectMapper.readValue(is, ExcelTemplateDefinition.class);
                validateTemplate(template);
                return template;
            }
        } catch (BusinessExportException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to load Excel template: {}", templateCode, e);
            throw new BusinessExportException(MessageCode.EXCEL_EXPORT_TEMPLATE_INVALID, templateCode);
        }
    }

    private void validateTemplate(ExcelTemplateDefinition template) {
        if (template == null || template.getColumns() == null || template.getColumns().isEmpty()) {
            throw new BusinessExportException(MessageCode.EXCEL_EXPORT_TEMPLATE_INVALID, "Empty template");
        }
        for (ColumnDefinition col : template.getColumns()) {
            String field = col.getField();
            if (field != null) {
                String lowerField = field.toLowerCase();
                for (String forbidden : FORBIDDEN_FIELDS) {
                    if (lowerField.contains(forbidden)) {
                        log.warn("Blocked template loading due to sensitive field: {}", field);
                        throw new BusinessExportException(MessageCode.EXCEL_EXPORT_TEMPLATE_INVALID, "Forbidden field: " + field);
                    }
                }
            }
        }
    }
}
