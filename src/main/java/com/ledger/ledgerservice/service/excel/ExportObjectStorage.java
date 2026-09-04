package com.ledger.ledgerservice.service.excel;

import java.io.File;
import java.io.InputStream;

public interface ExportObjectStorage {
    void upload(String bucket, String objectKey, File file, String contentType);

    InputStream download(String bucket, String objectKey);

    String generatePresignedUrl(String bucket, String objectKey, int expiryMinutes);

    void delete(String bucket, String objectKey);
}
