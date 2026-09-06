package com.ledger.ledgerservice.service.excel;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

public interface ExportObjectStorage {
    void upload(String bucket, String objectKey, File file, String contentType);

    InputStream download(String bucket, String objectKey);

    void consumeObject(String bucket, String objectKey, Consumer<InputStream> consumer);

    void downloadTo(String bucket, String objectKey, OutputStream outputStream);

    String generatePresignedUrl(String bucket, String objectKey, int expiryMinutes);

    void delete(String bucket, String objectKey);
}
