package com.ledger.ledgerservice.service.excel;

import com.ledger.ledgerservice.model.dto.response.ExcelUploadResponse;
import com.ledger.ledgerservice.model.dto.response.ExcelExportJobResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ExcelService {
    ExcelUploadResponse uploadExcel(MultipartFile file);
    ExcelExportJobResponse requestBusinessReportExport();
    ExportFilePayload downloadExportedFile(String jobId);
}
