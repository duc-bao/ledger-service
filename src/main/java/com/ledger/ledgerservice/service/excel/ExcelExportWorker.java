package com.ledger.ledgerservice.service.excel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.ledgerservice.exception.BusinessExportException;
import com.ledger.ledgerservice.model.dto.excel.ExcelTemplateDefinition;
import com.ledger.ledgerservice.model.dto.excel.ExportExecutionContext;
import com.ledger.ledgerservice.model.entity.ExcelExportJob;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.enums.ExcelExportStatus;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExcelExportWorker {
    private final ExcelExportJobStateService jobStateService;
    private final ExcelExportReportRegistry reportRegistry;
    private final ExcelTemplateService templateService;
    private final ExportObjectStorage objectStorage;
    private final ExportNotificationMessageResolver notificationMessageResolver;
    private final com.ledger.ledgerservice.repository.NotificationRepository notificationRepository;
    private final com.ledger.ledgerservice.repository.NotificationRecipientRepository notificationRecipientRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final TransactionOperations transactionOperations;

    @Value("${minio.bucket-name:ledger-exports}")
    private String bucketName;

    @Value("${app-setting.export-retention-days:7}")
    private int retentionDays;

    public void processJob(String jobId) {
        String workerId = UUID.randomUUID().toString();
        if (!jobStateService.claimJob(jobId, workerId)) {
            log.info("Job {} was already claimed or is no longer pending. Skipping.", jobId);
            return;
        }

        ExcelExportJob job = jobStateService.findJob(jobId).orElse(null);
        if (job == null) {
            log.error("Job {} not found after claim.", jobId);
            return;
        }

        File tempFile = null;
        try {
            Map<String, Object> snapshot = job.getFilterSnapshot() == null
                    ? Map.of()
                    : objectMapper.readValue(job.getFilterSnapshot(), new TypeReference<Map<String, Object>>() {});

            ExcelTemplateDefinition template = job.getTemplateSnapshot() != null
                    ? objectMapper.readValue(job.getTemplateSnapshot(), ExcelTemplateDefinition.class)
                    : templateService.getTemplate(job.getTemplateCode());

            ExportExecutionContext context = ExportExecutionContext.builder()
                    .jobId(jobId)
                    .reportType(job.getReportType())
                    .requestedBy(job.getRequestedBy())
                    .startDate(objectMapper.convertValue(snapshot.get("startDate"), LocalDateTime.class))
                    .endDate(objectMapper.convertValue(snapshot.get("endDate"), LocalDateTime.class))
                    .template(template)
                    .locale(Locale.getDefault())
                    .build();

            if (jobStateService.isCancelled(jobId)) {
                log.info("Job {} was cancelled before execution started.", jobId);
                return;
            }

            AbstractExcelExportService<?> reportService = reportRegistry.getReportService(job.getReportType());
            transactionOperations.executeWithoutResult(status -> {
                try {
                    reportService.executeExport(context);
                } catch (RuntimeException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new ExportExecutionException(ex);
                }
            });

            tempFile = context.getTempFile();
            if (tempFile == null || !tempFile.exists()) {
                throw new IllegalStateException("Temporary export file was not created successfully.");
            }

            if (jobStateService.isCancelled(jobId)) {
                log.info("Job {} was cancelled after file generation. Skipping upload and completion.", jobId);
                return;
            }

            String objectKey = String.format("%s/%s-%s.xlsx", job.getReportType().toLowerCase(), jobId, UUID.randomUUID());
            objectStorage.upload(bucketName, objectKey, tempFile, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            if (jobStateService.isCancelled(jobId)) {
                log.info("Job {} was cancelled after upload. Skipping completion.", jobId);
                return;
            }

            jobStateService.markCompleted(jobId, "MINIO", bucketName, objectKey, tempFile.length(), context.getTotalRows(), retentionDays);
            sendNotification(jobStateService.findJob(jobId).orElse(job));
        } catch (Exception e) {
            log.error("Error occurred while processing job {}", jobId, e);
            String errorCode = e instanceof BusinessExportException be ? be.getCode() : MessageCode.EXCEL_EXPORT_FAILED.getCode();
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown internal error";
            jobStateService.markFailed(jobId, errorCode, errorMessage);
            jobStateService.findJob(jobId).ifPresent(this::sendNotification);
        } finally {
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                log.warn("Failed to delete temporary export file: {}", tempFile.getAbsolutePath());
            }
        }
    }

    private void sendNotification(ExcelExportJob job) {
        try {
            com.ledger.ledgerservice.model.dto.request.CreateNotificationRequest request = notificationMessageResolver.resolveNotification(job, Locale.getDefault());
            com.ledger.ledgerservice.model.entity.NotificationEntity notification = com.ledger.ledgerservice.model.entity.NotificationEntity.builder()
                    .senderUserId(null)
                    .senderName("System")
                    .title(request.getTitle())
                    .content(request.getContent())
                    .type(request.getType())
                    .status("ACTIVE")
                    .priority("NORMAL")
                    .targetUrl(request.getTargetUrl())
                    .fileName(request.getFileName())
                    .relatedEntityType(request.getRelatedEntityType())
                    .relatedEntityId(request.getRelatedEntityId())
                    .isPinned(false)
                    .isDismissible(true)
                    .metadataJson(request.getMetadataJson() != null ? request.getMetadataJson() : new java.util.LinkedHashMap<>())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            com.ledger.ledgerservice.model.entity.NotificationEntity savedNotification = notificationRepository.save(notification);

            for (String recipient : request.getRecipientUserIds()) {
                com.ledger.ledgerservice.model.entity.NotificationRecipientEntity recipientEntity = com.ledger.ledgerservice.model.entity.NotificationRecipientEntity.builder()
                        .id(UUID.randomUUID().toString())
                        .notificationId(savedNotification.getId())
                        .recipientUserId(resolveRecipientUserId(recipient))
                        .readStatus(com.ledger.ledgerservice.model.enums.NotificationReadStatus.UNREAD)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRecipientRepository.save(recipientEntity);
            }
        } catch (Exception ex) {
            log.error("Failed to send export result notification for job {}", job.getId(), ex);
        }
    }

    private String resolveRecipientUserId(String recipient) {
        return userRepository.findByUsername(recipient)
                .map(User::getId)
                .orElse(recipient);
    }

    private static class ExportExecutionException extends RuntimeException {
        private ExportExecutionException(Exception cause) {
            super(cause.getMessage(), cause);
        }
    }
}
