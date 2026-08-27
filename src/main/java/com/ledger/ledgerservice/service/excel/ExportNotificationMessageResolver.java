package com.ledger.ledgerservice.service.excel;

import com.ledger.ledgerservice.model.dto.request.CreateNotificationRequest;
import com.ledger.ledgerservice.model.entity.ExcelExportJob;
import com.ledger.ledgerservice.model.enums.ExcelExportStatus;
import com.ledger.ledgerservice.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportNotificationMessageResolver {
    private final MessageHelper messageHelper;

    public CreateNotificationRequest resolveNotification(ExcelExportJob job, Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }

        String reportType = job.getReportType();
        ExcelExportStatus status = job.getStatus();
        String jobId = job.getId();

        String statusStr = status == ExcelExportStatus.COMPLETED ? "completed" : "failed";
        String titleKey = String.format("export.%s.%s.title", reportType, statusStr);
        String contentKey = String.format("export.%s.%s.content", reportType, statusStr);

        String defaultTitle = status == ExcelExportStatus.COMPLETED
                ? "Excel export completed: " + reportType
                : "Excel export failed: " + reportType;

        String defaultContent = status == ExcelExportStatus.COMPLETED
                ? String.format("File %s is ready. Total rows: %d.", job.getFileName(), job.getTotalRows())
                : String.format("Unable to export. Error: %s", job.getErrorMessage());

        String title = messageHelper.getMsg(titleKey, locale, defaultTitle);
        String content;

        if (status == ExcelExportStatus.COMPLETED) {
            content = messageHelper.getMsg(contentKey, locale, defaultContent, job.getFileName(), job.getTotalRows());
        } else {
            content = messageHelper.getMsg(contentKey, locale, defaultContent, job.getErrorMessage() != null ? job.getErrorMessage() : "");
        }

        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setRecipientUserIds(Collections.singletonList(job.getRequestedBy()));
        request.setTitle(title);
        request.setContent(content);
        request.setType(status == ExcelExportStatus.COMPLETED ? "EXPORT_COMPLETED" : "EXPORT_FAILED");
        request.setStatus("ACTIVE");
        request.setPriority("NORMAL");
        request.setRelatedEntityType("EXCEL_EXPORT_JOB");
        request.setRelatedEntityId(jobId);
        request.setTargetUrl("/api/v1/exports/" + jobId);
        request.setFileName(job.getFileName());
        request.setIsDismissible(true);

        return request;
    }
}
