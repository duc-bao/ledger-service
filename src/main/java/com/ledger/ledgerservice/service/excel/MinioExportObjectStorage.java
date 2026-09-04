package com.ledger.ledgerservice.service.excel;

import com.ledger.ledgerservice.exception.TransientExportException;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioExportObjectStorage implements ExportObjectStorage {
    private final MinioClient minioClient;

    @Override
    public void upload(String bucket, String objectKey, File file, String contentType) {
        try {
            ensureBucketExists(bucket);
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .filename(file.getAbsolutePath())
                            .contentType(contentType)
                            .build()
            );
            log.info("Successfully uploaded file {} to MinIO bucket {}, objectKey {}", file.getName(), bucket, objectKey);
        } catch (Exception e) {
            log.error("Failed to upload file {} to MinIO bucket {}, objectKey {}", file.getName(), bucket, objectKey, e);
            throw new TransientExportException("MinIO upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream download(String bucket, String objectKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to download object from MinIO bucket {}, objectKey {}", bucket, objectKey, e);
            throw new TransientExportException("MinIO download failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePresignedUrl(String bucket, String objectKey, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(expiryMinutes, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for MinIO bucket {}, objectKey {}", bucket, objectKey, e);
            throw new RuntimeException("MinIO presigned URL generation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String bucket, String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
            log.info("Successfully deleted object {} from MinIO bucket {}", objectKey, bucket);
        } catch (Exception e) {
            log.error("Failed to delete object {} from MinIO bucket {}", objectKey, bucket, e);
        }
    }

    private void ensureBucketExists(String bucket) throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!found) {
            log.info("Bucket {} does not exist, creating it", bucket);
            try {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            } catch (ErrorResponseException ex) {
                if (!"BucketAlreadyOwnedByYou".equals(ex.errorResponse().code())) {
                    throw ex;
                }
                log.info("Bucket {} was created concurrently by another worker", bucket);
            }
        }
    }
}
