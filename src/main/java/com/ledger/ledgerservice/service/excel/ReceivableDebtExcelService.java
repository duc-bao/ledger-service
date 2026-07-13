package com.ledger.ledgerservice.service.excel;

import com.ledger.ledgerservice.model.dto.response.ReceivableDebtExcelParseResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ReceivableDebtExcelService {
    ReceivableDebtExcelParseResponse parseReceivableDebtReport(MultipartFile file);
}
