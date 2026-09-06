package com.ledger.ledgerservice.service.excel;

import com.ledger.ledgerservice.config.properties.AppSettingProperty;
import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.response.ExcelExportJobResponse;
import com.ledger.ledgerservice.model.dto.response.ExcelUploadResponse;
import com.ledger.ledgerservice.model.entity.ExcelExportJob;
import com.ledger.ledgerservice.model.enums.ExcelExportStatus;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.security.JwtUserPrincipal;
import com.ledger.ledgerservice.repository.ExcelExportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelServiceImpl implements ExcelService {
    private static final String XLSX_EXT = ".xlsx";
    private static final String XLS_EXT = ".xls";
    private static final String DEFAULT_UPLOAD_DIR = "./storage/excel";
    private static final DateTimeFormatter FILE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AppSettingProperty appSettingProperty;
    private final ExcelExportJobRepository excelExportJobRepository;
    private final ExcelExportAsyncService excelExportAsyncService;

    @Override
    public ExcelUploadResponse uploadExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(MessageCode.EXCEL_FILE_EMPTY, HttpStatus.BAD_REQUEST);
        }

        String originalName = file.getOriginalFilename();
        if (!isExcelFile(originalName)) {
            throw new BusinessException(MessageCode.EXCEL_FILE_INVALID_TYPE, HttpStatus.BAD_REQUEST);
        }

        try {
            Path uploadDir = resolveUploadDir();
            Files.createDirectories(uploadDir);

            String storedFileName = buildStoredFileName(originalName);
            Path targetFile = uploadDir.resolve(storedFileName).normalize();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return ExcelUploadResponse.builder()
                    .originalFileName(originalName)
                    .storedFileName(storedFileName)
                    .storedPath("temp/" + storedFileName)
                    .size(file.getSize())
                    .build();
        } catch (IOException ex) {
            log.error("Cannot store excel file", ex);
            throw new BusinessException(MessageCode.SERVICE_UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    public ExcelExportJobResponse requestBusinessReportExport() {
        String username = getCurrentUsername();
        ExcelExportJob job = ExcelExportJob.builder()
                .reportType("BUSINESS_REPORT")
                .status(ExcelExportStatus.PENDING)
                .requestedBy(username)
                .requestedAt(LocalDateTime.now())
                .createdBy(username)
                .updatedBy(username)
                .build();
        ExcelExportJob saved = excelExportJobRepository.save(job);
        excelExportAsyncService.processBusinessReportExport(saved.getId());

        return ExcelExportJobResponse.builder()
                .jobId(saved.getId())
                .reportType(saved.getReportType())
                .status(saved.getStatus())
                .fileName(saved.getFileName())
                .requestedBy(saved.getRequestedBy())
                .requestedAt(saved.getRequestedAt())
                .build();
    }

    @Override
    public ExportFilePayload downloadExportedFile(String jobId) {
        String username = getCurrentUsername();
        ExcelExportJob job = excelExportJobRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException(MessageCode.EXCEL_EXPORT_JOB_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (!username.equalsIgnoreCase(job.getRequestedBy())) {
            throw new BusinessException(MessageCode.FORBIDDEN, HttpStatus.FORBIDDEN);
        }

        if (job.getStatus() == ExcelExportStatus.PENDING || job.getStatus() == ExcelExportStatus.PROCESSING) {
            throw new BusinessException(MessageCode.EXCEL_EXPORT_NOT_READY, HttpStatus.CONFLICT);
        }

        if (job.getStatus() == ExcelExportStatus.FAILED) {
            throw new BusinessException(MessageCode.EXCEL_EXPORT_FAILED, HttpStatus.BAD_REQUEST);
        }

        if (!StringUtils.hasText(job.getFilePath())) {
            throw new BusinessException(MessageCode.NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        try {
            Path filePath = Paths.get(job.getFilePath()).toAbsolutePath().normalize();
            if (!Files.exists(filePath)) {
                throw new BusinessException(MessageCode.NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            long size = Files.size(filePath);
            Resource resource = new FileSystemResource(filePath.toFile());
            return ExportFilePayload.builder()
                    .fileName(job.getFileName())
                    .resource(resource)
                    .contentLength(size)
                    .build();
        } catch (IOException ex) {
            log.error("Cannot read exported file. jobId={}", jobId, ex);
            throw new BusinessException(MessageCode.SERVICE_UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private Path resolveUploadDir() {
        String configured = appSettingProperty.getTempDirectory();
        String targetDir = StringUtils.hasText(configured) ? configured.trim() : DEFAULT_UPLOAD_DIR;
        return Paths.get(targetDir).toAbsolutePath().normalize();
    }

    private boolean isExcelFile(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return false;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.endsWith(XLSX_EXT) || lower.endsWith(XLS_EXT);
    }

    private String buildStoredFileName(String originalName) {
        String clean = StringUtils.hasText(originalName) ? originalName.trim() : "excel-file";
        String ext = clean.toLowerCase(Locale.ROOT).endsWith(XLS_EXT) ? XLS_EXT : XLSX_EXT;
        String baseName = clean;
        if (clean.toLowerCase(Locale.ROOT).endsWith(XLSX_EXT)) {
            baseName = clean.substring(0, clean.length() - XLSX_EXT.length());
        } else if (clean.toLowerCase(Locale.ROOT).endsWith(XLS_EXT)) {
            baseName = clean.substring(0, clean.length() - XLS_EXT.length());
        }

        String normalized = Normalizer.normalize(baseName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        if (!StringUtils.hasText(normalized)) {
            normalized = "excel_file";
        }
        return normalized + "_" + FILE_TIME_FORMAT.format(LocalDateTime.now()) + "_" + UUID.randomUUID() + ext;
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
