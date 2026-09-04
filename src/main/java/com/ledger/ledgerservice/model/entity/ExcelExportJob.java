package com.ledger.ledgerservice.model.entity;

import com.ledger.ledgerservice.model.enums.ExcelExportStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "excel_export_jobs",
        indexes = {
                @Index(columnList = "status"),
                @Index(columnList = "requested_by"),
                @Index(columnList = "requested_at"),
                @Index(columnList = "requested_by, requested_at"),
                @Index(columnList = "report_type, status")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ExcelExportJob extends EntityBase {
    @Column(name = "report_type", nullable = false, length = 100)
    private String reportType;

    @Column(name = "template_code", length = 100)
    private String templateCode;

    @Column(name = "template_snapshot", columnDefinition = "TEXT")
    private String templateSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ExcelExportStatus status;

    @Column(name = "filter_snapshot", columnDefinition = "TEXT")
    private String filterSnapshot;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_path", length = 1000)
    private String filePath;

    @Column(name = "content_type", length = 150)
    private String contentType;

    @Column(name = "total_rows")
    private Long totalRows;

    @Builder.Default
    @Column(name = "processed_rows", nullable = false)
    private Long processedRows = 0L;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Builder.Default
    @Column(name = "storage_provider", nullable = false, length = 30)
    private String storageProvider = "MINIO";

    @Column(name = "storage_bucket", length = 100)
    private String storageBucket;

    @Column(name = "storage_object_key", length = 500)
    private String storageObjectKey;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "requested_by", length = 100)
    private String requestedBy;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Builder.Default
    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @Column(name = "worker_id", length = 100)
    private String workerId;

    @Version
    @Builder.Default
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}
