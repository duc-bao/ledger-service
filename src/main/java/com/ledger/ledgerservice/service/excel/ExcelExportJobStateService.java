package com.ledger.ledgerservice.service.excel;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.exception.TransientExportException;
import com.ledger.ledgerservice.model.entity.ExcelExportJob;
import com.ledger.ledgerservice.model.enums.ExcelExportStatus;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.repository.ExcelExportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelExportJobStateService {
    private static final int PROGRESS_RETRY_LIMIT = 3;

    private final ExcelExportJobRepository jobRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean claimJob(String jobId, String workerId) {
        try {
            ExcelExportJob job = getJobOrThrow(jobId);
            if (job.getStatus() != ExcelExportStatus.PENDING) {
                return false;
            }

            job.setStatus(ExcelExportStatus.PROCESSING);
            job.setWorkerId(workerId);
            job.setStartedAt(LocalDateTime.now());
            job.setCompletedAt(null);
            job.setErrorCode(null);
            job.setErrorMessage(null);
            jobRepository.saveAndFlush(job);
            return true;
        } catch (ObjectOptimisticLockingFailureException ex) {
            log.info("Failed to claim export job {} because another transaction already updated it.", jobId);
            return false;
        }
    }

    @Transactional(readOnly = true)
    public Optional<ExcelExportJob> findJob(String jobId) {
        return jobRepository.findById(jobId);
    }

    @Transactional(readOnly = true)
    public ExcelExportJob getJobOrThrow(String jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException(MessageCode.EXCEL_EXPORT_JOB_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateProgress(String jobId, long processedRows) {
        for (int attempt = 1; attempt <= PROGRESS_RETRY_LIMIT; attempt++) {
            try {
                ExcelExportJob job = getJobOrThrow(jobId);
                if (job.getStatus() == ExcelExportStatus.CANCELLED) {
                    return;
                }
                if (job.getStatus() != ExcelExportStatus.PROCESSING) {
                    throw new TransientExportException("Cannot update progress for job " + jobId + " in status " + job.getStatus());
                }

                job.setProcessedRows(processedRows);
                jobRepository.saveAndFlush(job);
                return;
            } catch (ObjectOptimisticLockingFailureException ex) {
                log.warn("Optimistic lock while updating progress for job {} attempt {}", jobId, attempt, ex);
                if (attempt == PROGRESS_RETRY_LIMIT) {
                    throw new TransientExportException("Failed to update export progress after retries for job " + jobId);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public boolean isCancelled(String jobId) {
        return jobRepository.findStatusById(jobId) == ExcelExportStatus.CANCELLED;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompleted(String jobId,
                              String storageProvider,
                              String bucket,
                              String objectKey,
                              long fileSizeBytes,
                              long totalRows,
                              int retentionDays) {
        ExcelExportJob job = getJobOrThrow(jobId);
        if (job.getStatus() == ExcelExportStatus.CANCELLED) {
            return;
        }
        if (job.getStatus() != ExcelExportStatus.PROCESSING) {
            throw new TransientExportException("Cannot complete export job " + jobId + " from status " + job.getStatus());
        }

        job.setStatus(ExcelExportStatus.COMPLETED);
        job.setStorageProvider(storageProvider);
        job.setStorageBucket(bucket);
        job.setStorageObjectKey(objectKey);
        job.setFileSizeBytes(fileSizeBytes);
        job.setTotalRows(totalRows);
        job.setProcessedRows(totalRows);
        job.setCompletedAt(LocalDateTime.now());
        job.setExpiredAt(LocalDateTime.now().plusDays(retentionDays));
        job.setWorkerId(null);
        jobRepository.saveAndFlush(job);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String jobId, String errorCode, String errorMessage) {
        ExcelExportJob job = getJobOrThrow(jobId);
        if (job.getStatus() == ExcelExportStatus.CANCELLED || job.getStatus() == ExcelExportStatus.COMPLETED) {
            return;
        }

        job.setStatus(ExcelExportStatus.FAILED);
        job.setErrorCode(errorCode);
        job.setErrorMessage(errorMessage);
        job.setCompletedAt(LocalDateTime.now());
        job.setWorkerId(null);
        job.setAttemptCount(job.getAttemptCount() + 1);
        jobRepository.saveAndFlush(job);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requeueStuckJob(String jobId, LocalDateTime threshold) {
        ExcelExportJob job = getJobOrThrow(jobId);
        if (job.getStatus() != ExcelExportStatus.PROCESSING || job.getStartedAt() == null || job.getStartedAt().isAfter(threshold)) {
            return;
        }

        job.setStatus(ExcelExportStatus.PENDING);
        job.setWorkerId(null);
        job.setStartedAt(null);
        job.setAttemptCount(job.getAttemptCount() + 1);
        jobRepository.saveAndFlush(job);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markExpired(String jobId) {
        ExcelExportJob job = getJobOrThrow(jobId);
        if (job.getStatus() != ExcelExportStatus.COMPLETED) {
            return;
        }

        job.setStatus(ExcelExportStatus.EXPIRED);
        job.setWorkerId(null);
        jobRepository.saveAndFlush(job);
    }
}
