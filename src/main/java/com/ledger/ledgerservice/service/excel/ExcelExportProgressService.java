package com.ledger.ledgerservice.service.excel;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExcelExportProgressService {
    private final ExcelExportJobStateService stateService;

    public void updateProgress(String jobId, long processedRows) {
        stateService.updateProgress(jobId, processedRows);
    }

    public boolean isCancelled(String jobId) {
        return stateService.isCancelled(jobId);
    }
}
