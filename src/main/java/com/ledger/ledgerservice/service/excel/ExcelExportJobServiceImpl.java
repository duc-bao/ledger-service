package com.ledger.ledgerservice.service.excel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.ledgerservice.config.properties.AppSettingProperty;
import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.exception.BusinessExportException;
import com.ledger.ledgerservice.model.dto.excel.ExcelTemplateDefinition;
import com.ledger.ledgerservice.model.dto.excel.ExportDateRange;
import com.ledger.ledgerservice.model.dto.excel.ExportDateRangeRequest;
import com.ledger.ledgerservice.model.dto.excel.ExportJobResponse;
import com.ledger.ledgerservice.model.entity.ExcelExportJob;
import com.ledger.ledgerservice.model.enums.ExcelExportStatus;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.security.JwtUserPrincipal;
import com.ledger.ledgerservice.repository.ExcelExportJobRepository;
import com.ledger.ledgerservice.service.security.PermissionResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelExportJobServiceImpl implements ExcelExportJobService {
    private final ExcelExportJobRepository jobRepository;
    private final ExcelExportReportRegistry reportRegistry;
    private final ExcelTemplateService templateService;
    private final ExportObjectStorage objectStorage;
    private final PermissionResolutionService permissionResolutionService;
    private final AppSettingProperty appSettingProperty;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Value("${minio.bucket-name:ledger-exports}")
    private String bucketName;

    @Override
    @Transactional
    public ExportJobResponse requestExport(String reportType, ExportDateRangeRequest request) {
        String username = getCurrentUsername();
        AbstractExcelExportService<?> reportService = reportRegistry.getReportService(reportType);
        ExportDateRange normalizedDateRange = reportService.normalizeDateRange(ExportDateRange.builder()
                .startDate(request != null ? request.getStartDate() : null)
                .endDate(request != null ? request.getEndDate() : null)
                .build());

        String templateCode = reportType.toLowerCase().replace("_", "-") + "-default";
        ExcelTemplateDefinition template = templateService.getTemplate(templateCode);
        String fileTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("%s_Export_%s.xlsx", reportType, fileTimestamp);

        String filtersJson;
        String templateJson;
        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("startDate", normalizedDateRange.getStartDate());
            snapshot.put("endDate", normalizedDateRange.getEndDate());
            snapshot.put("requestedBy", username);
            filtersJson = objectMapper.writeValueAsString(snapshot);
            templateJson = objectMapper.writeValueAsString(template);
        } catch (Exception e) {
            log.error("Failed to serialize export snapshot for reportType={}", reportType, e);
            throw new BusinessException(MessageCode.EXCEL_EXPORT_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ExcelExportJob saved = jobRepository.saveAndFlush(ExcelExportJob.builder()
                .reportType(reportType.toUpperCase())
                .status(ExcelExportStatus.PENDING)
                .templateCode(templateCode)
                .fileName(fileName)
                .filterSnapshot(filtersJson)
                .templateSnapshot(templateJson)
                .requestedBy(username)
                .requestedAt(LocalDateTime.now())
                .attemptCount(0)
                .createdBy(username)
                .updatedBy(username)
                .build());

        eventPublisher.publishEvent(new ExcelExportRequestEvent(this, saved.getId()));
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ExportJobResponse getJobStatus(String jobId) {
        ExcelExportJob job = getJobAndValidateOwnership(jobId);
        return mapToResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExportJobResponse> searchJobs(String reportType, ExcelExportStatus status, Pageable pageable) {
        String userId = getCurrentUserId();
        String username = getCurrentUsername();

        boolean viewAll = username.equalsIgnoreCase(appSettingProperty.getSuperUser())
                || permissionResolutionService.getGlobalPermissions(userId).contains("EXPORT_JOB_VIEW_ALL");

        Page<ExcelExportJob> jobs;
        if (viewAll) {
            if (reportType != null && status != null) {
                jobs = jobRepository.findByReportTypeAndStatus(reportType.toUpperCase(), status, pageable);
            } else if (reportType != null) {
                jobs = jobRepository.findByReportType(reportType.toUpperCase(), pageable);
            } else if (status != null) {
                jobs = jobRepository.findByStatus(status, pageable);
            } else {
                jobs = jobRepository.findAll(pageable);
            }
        } else {
            if (reportType != null && status != null) {
                jobs = jobRepository.findByRequestedByAndReportTypeAndStatus(username, reportType.toUpperCase(), status, pageable);
            } else if (reportType != null) {
                jobs = jobRepository.findByRequestedByAndReportType(username, reportType.toUpperCase(), pageable);
            } else if (status != null) {
                jobs = jobRepository.findByRequestedByAndStatus(username, status, pageable);
            } else {
                jobs = jobRepository.findByRequestedBy(username, pageable);
            }
        }

        return jobs.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public String downloadFile(String jobId) {
        ExcelExportJob job = getJobAndValidateOwnership(jobId);

        if (job.getStatus() == ExcelExportStatus.PENDING || job.getStatus() == ExcelExportStatus.PROCESSING) {
            throw new BusinessExportException(MessageCode.EXCEL_EXPORT_NOT_READY);
        }

        if (job.getStatus() == ExcelExportStatus.FAILED) {
            throw new BusinessExportException(MessageCode.EXCEL_EXPORT_FAILED);
        }

        if (job.getStatus() == ExcelExportStatus.EXPIRED) {
            throw new BusinessExportException(MessageCode.EXCEL_EXPORT_FILE_EXPIRED);
        }

        if (job.getExpiredAt() != null && job.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BusinessExportException(MessageCode.EXCEL_EXPORT_FILE_EXPIRED);
        }

        return objectStorage.generatePresignedUrl(job.getStorageBucket(), job.getStorageObjectKey(), 5);
    }

    @Override
    @Transactional
    public void cancelJob(String jobId) {
        ExcelExportJob job = getJobAndValidateOwnership(jobId);
        if (job.getStatus() == ExcelExportStatus.COMPLETED
                || job.getStatus() == ExcelExportStatus.FAILED
                || job.getStatus() == ExcelExportStatus.EXPIRED
                || job.getStatus() == ExcelExportStatus.CANCELLED) {
            throw new BusinessException(MessageCode.INPUT_INVALID, HttpStatus.BAD_REQUEST);
        }

        job.setStatus(ExcelExportStatus.CANCELLED);
        job.setCompletedAt(LocalDateTime.now());
        job.setWorkerId(null);
        jobRepository.saveAndFlush(job);
        log.info("Job {} has been cancelled by user {}.", jobId, getCurrentUsername());
    }

    private ExcelExportJob getJobAndValidateOwnership(String jobId) {
        ExcelExportJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException(MessageCode.EXCEL_EXPORT_JOB_NOT_FOUND, HttpStatus.NOT_FOUND));

        String userId = getCurrentUserId();
        String username = getCurrentUsername();

        boolean hasViewAll = username.equalsIgnoreCase(appSettingProperty.getSuperUser())
                || permissionResolutionService.getGlobalPermissions(userId).contains("EXPORT_JOB_VIEW_ALL");

        if (!hasViewAll && !username.equalsIgnoreCase(job.getRequestedBy())) {
            throw new BusinessException(MessageCode.ACCESS_DENIED, HttpStatus.FORBIDDEN);
        }

        return job;
    }

    private ExportJobResponse mapToResponse(ExcelExportJob job) {
        long total = job.getTotalRows() != null ? job.getTotalRows() : 0L;
        long processed = job.getProcessedRows() != null ? job.getProcessedRows() : 0L;
        int progress = total > 0 ? (int) (processed * 100 / total) : 0;

        return ExportJobResponse.builder()
                .id(job.getId())
                .reportType(job.getReportType())
                .status(job.getStatus())
                .statusUrl("/api/v1/exports/" + job.getId())
                .totalRows(job.getTotalRows())
                .processedRows(job.getProcessedRows())
                .progressPercent(progress)
                .fileName(job.getFileName())
                .errorMessage(job.getErrorMessage())
                .requestedAt(job.getRequestedAt())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .expiredAt(job.getExpiredAt())
                .build();
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException(MessageCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
            return jwtUserPrincipal.userId();
        }
        throw new BusinessException(MessageCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException(MessageCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
            return jwtUserPrincipal.username();
        }
        return authentication.getName();
    }
}
