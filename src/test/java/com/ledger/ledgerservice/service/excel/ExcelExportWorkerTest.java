package com.ledger.ledgerservice.service.excel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.ledgerservice.model.dto.excel.ExcelTemplateDefinition;
import com.ledger.ledgerservice.model.dto.excel.ExportExecutionContext;
import com.ledger.ledgerservice.model.dto.request.CreateNotificationRequest;
import com.ledger.ledgerservice.model.entity.ExcelExportJob;
import com.ledger.ledgerservice.model.entity.NotificationEntity;
import com.ledger.ledgerservice.model.entity.NotificationRecipientEntity;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.enums.ExcelExportStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionOperations;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcelExportWorkerTest {

    @Mock
    private ExcelExportJobStateService jobStateService;

    @Mock
    private ExcelExportReportRegistry reportRegistry;

    @Mock
    private ExcelTemplateService templateService;

    @Mock
    private ExportObjectStorage objectStorage;

    @Mock
    private ExportNotificationMessageResolver notificationMessageResolver;

    @Mock
    private com.ledger.ledgerservice.repository.NotificationRepository notificationRepository;

    @Mock
    private com.ledger.ledgerservice.repository.NotificationRecipientRepository notificationRecipientRepository;

    @Mock
    private com.ledger.ledgerservice.repository.UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TransactionOperations transactionOperations;

    @InjectMocks
    private ExcelExportWorker worker;

    @Mock
    private AbstractExcelExportService<?> mockReportService;

    @Test
    void processJob_Success() throws Exception {
        String jobId = "job-123";
        ReflectionTestUtils.setField(worker, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(worker, "retentionDays", 7);

        when(jobStateService.claimJob(eq(jobId), any())).thenReturn(true);

        ExcelExportJob job = ExcelExportJob.builder()
                .id(jobId)
                .reportType("USER")
                .status(ExcelExportStatus.PENDING)
                .templateCode("user-default")
                .requestedBy("test_user")
                .attemptCount(0)
                .build();
        when(jobStateService.findJob(jobId)).thenReturn(Optional.of(job));
        when(jobStateService.isCancelled(jobId)).thenReturn(false);

        ExcelTemplateDefinition mockTemplate = new ExcelTemplateDefinition();
        when(templateService.getTemplate("user-default")).thenReturn(mockTemplate);
        when(reportRegistry.getReportService("USER")).thenReturn((AbstractExcelExportService) mockReportService);
        when(notificationMessageResolver.resolveNotification(any(), any())).thenReturn(notificationRequest());
        when(userRepository.findByUsername("test_user")).thenReturn(Optional.of(User.builder().id("user-id-123").build()));
        when(notificationRepository.save(any(NotificationEntity.class))).thenAnswer(invocation -> {
            NotificationEntity notification = invocation.getArgument(0);
            assertNull(notification.getId());
            notification.setId("notification-id-123");
            return notification;
        });
        doAnswer(invocation -> {
            invocation.getArgument(0, Consumer.class).accept(null);
            return null;
        }).when(transactionOperations).executeWithoutResult(any());

        doAnswer(invocation -> {
            ExportExecutionContext ctx = invocation.getArgument(0);
            File temp = File.createTempFile("test-export", ".xlsx");
            ctx.setTempFile(temp);
            ctx.setTotalRows(10L);
            return null;
        }).when(mockReportService).executeExport(any(ExportExecutionContext.class));

        worker.processJob(jobId);

        verify(objectStorage, times(1)).upload(eq("test-bucket"), any(), any(), any());
        verify(jobStateService, times(1)).markCompleted(eq(jobId), eq("MINIO"), eq("test-bucket"), any(), eq(0L), eq(10L), eq(7));
        verify(jobStateService, never()).markFailed(any(), any(), any());

        ArgumentCaptor<NotificationEntity> notificationCaptor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        assertNull(notificationCaptor.getValue().getSenderUserId());

        ArgumentCaptor<NotificationRecipientEntity> recipientCaptor = ArgumentCaptor.forClass(NotificationRecipientEntity.class);
        verify(notificationRecipientRepository).save(recipientCaptor.capture());
        assertEquals("notification-id-123", recipientCaptor.getValue().getNotificationId());
        assertEquals("user-id-123", recipientCaptor.getValue().getRecipientUserId());
    }

    private CreateNotificationRequest notificationRequest() {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setTitle("done");
        request.setContent("done");
        request.setType("INFO");
        request.setRecipientUserIds(List.of("test_user"));
        return request;
    }
}
