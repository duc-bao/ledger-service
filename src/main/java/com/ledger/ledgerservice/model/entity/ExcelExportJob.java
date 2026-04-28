package com.ledger.ledgerservice.model.entity;

import com.ledger.ledgerservice.model.enums.ExcelExportStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "excel_export_jobs",
        indexes = {
                @Index(columnList = "status"),
                @Index(columnList = "requested_by"),
                @Index(columnList = "requested_at")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ExcelExportJob extends EntityBase {
    @Column(name = "report_type", nullable = false, length = 100)
    private String reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ExcelExportStatus status;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_path", length = 1000)
    private String filePath;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "requested_by", length = 100)
    private String requestedBy;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
