package com.ledger.ledgerservice.service.excel;

import org.springframework.context.ApplicationEvent;

public class ExcelExportRequestEvent extends ApplicationEvent {
    private final String jobId;

    public ExcelExportRequestEvent(Object source, String jobId) {
        super(source);
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }
}
