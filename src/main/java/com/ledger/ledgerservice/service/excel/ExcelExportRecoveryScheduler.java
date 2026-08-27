package com.ledger.ledgerservice.service.excel;

import com.ledger.ledgerservice.model.entity.ExcelExportJob;
import com.ledger.ledgerservice.model.enums.ExcelExportStatus;
import com.ledger.ledgerservice.repository.ExcelExportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExcelExportRecoveryScheduler {
    private final ExcelExportJobRepository jobRepository;
    private final ExcelExportJobStateService jobStateService;
    private final ExcelExportWorker worker;
    private final ExportObjectStorage objectStorage;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("App started. Recovering stuck or pending export jobs.");
        try {
            recoverStuckJobs();
            triggerPendingJobs();
        } catch (Exception e) {
            log.error("Error recovering jobs on application ready event", e);
        }
    }

    @Scheduled(cron = "${app-setting.export-recovery-cron:0 0/10 * * * ?}")
    public void runRecovery() {
        log.info("Running scheduled recovery for export jobs.");
        recoverStuckJobs();
        triggerPendingJobs();
        cleanupExpiredJobs();
    }

    private void recoverStuckJobs() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        List<ExcelExportJob> stuckJobs = jobRepository.findByStatusAndStartedAtBefore(ExcelExportStatus.PROCESSING, threshold);
        for (ExcelExportJob job : stuckJobs) {
            jobStateService.requeueStuckJob(job.getId(), threshold);
        }
    }

    private void triggerPendingJobs() {
        List<ExcelExportJob> pendingJobs = jobRepository.findByStatus(ExcelExportStatus.PENDING);
        for (ExcelExportJob job : pendingJobs) {
            try {
                worker.processJob(job.getId());
            } catch (Exception e) {
                log.error("Failed to recover/process job {}", job.getId(), e);
            }
        }
    }

    private void cleanupExpiredJobs() {
        LocalDateTime now = LocalDateTime.now();
        List<ExcelExportJob> expiredJobs = jobRepository.findByStatusAndExpiredAtBefore(ExcelExportStatus.COMPLETED, now);
        for (ExcelExportJob job : expiredJobs) {
            try {
                if (job.getStorageObjectKey() != null) {
                    objectStorage.delete(job.getStorageBucket(), job.getStorageObjectKey());
                }
                jobStateService.markExpired(job.getId());
            } catch (Exception e) {
                log.error("Failed to cleanup expired job {}", job.getId(), e);
            }
        }
    }
}
