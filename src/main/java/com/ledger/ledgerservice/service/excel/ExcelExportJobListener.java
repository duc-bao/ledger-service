package com.ledger.ledgerservice.service.excel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.Executor;

@Component
@Slf4j
public class ExcelExportJobListener {
    private final ExcelExportWorker exportWorker;
    private final Executor executor;

    public ExcelExportJobListener(ExcelExportWorker exportWorker,
                                  @Qualifier("excelExportExecutor") Executor executor) {
        this.exportWorker = exportWorker;
        this.executor = executor;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleExportRequest(ExcelExportRequestEvent event) {
        String jobId = event.getJobId();
        log.info("Received export request event post-commit for jobId: {}. Submitting to executor.", jobId);
        executor.execute(() -> {
            try {
                exportWorker.processJob(jobId);
            } catch (Exception e) {
                log.error("Failed to run export worker for jobId: {}", jobId, e);
            }
        });
    }
}
