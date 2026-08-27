package com.ledger.ledgerservice.service.excel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.ledgerservice.config.properties.AppSettingProperty;
import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.excel.ExcelTemplateDefinition;
import com.ledger.ledgerservice.model.dto.excel.ExportDateRange;
import com.ledger.ledgerservice.model.dto.excel.ExportDateRangeRequest;
import com.ledger.ledgerservice.model.dto.excel.ExportJobResponse;
import com.ledger.ledgerservice.model.entity.ExcelExportJob;
import com.ledger.ledgerservice.model.enums.ExcelExportStatus;
import com.ledger.ledgerservice.model.security.JwtUserPrincipal;
import com.ledger.ledgerservice.repository.ExcelExportJobRepository;
import com.ledger.ledgerservice.service.security.PermissionResolutionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcelExportJobServiceImplTest {

    @Mock
    private ExcelExportJobRepository jobRepository;

    @Mock
    private ExcelExportReportRegistry reportRegistry;

    @Mock
    private ExcelTemplateService templateService;

    @Mock
    private ExportObjectStorage objectStorage;

    @Mock
    private PermissionResolutionService permissionResolutionService;

    @Mock
    private AppSettingProperty appSettingProperty;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ExcelExportJobServiceImpl jobService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private AbstractExcelExportService<?> mockReportService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requestExport_Success() throws Exception {
        JwtUserPrincipal principal = new JwtUserPrincipal("user-123", "test_user");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);

        ExportDateRangeRequest request = new ExportDateRangeRequest();
        ExportDateRange normalized = ExportDateRange.builder()
                .startDate(LocalDateTime.of(2026, 7, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 7, 16, 12, 0))
                .build();

        when(reportRegistry.getReportService("USER")).thenReturn((AbstractExcelExportService) mockReportService);
        when(mockReportService.normalizeDateRange(any())).thenReturn(normalized);

        ExcelTemplateDefinition mockTemplate = new ExcelTemplateDefinition();
        when(templateService.getTemplate("user-default")).thenReturn(mockTemplate);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        ExcelExportJob savedJob = ExcelExportJob.builder()
                .id("job-123")
                .reportType("USER")
                .status(ExcelExportStatus.PENDING)
                .fileName("USER_Export_test.xlsx")
                .requestedBy("test_user")
                .requestedAt(LocalDateTime.now())
                .build();
        when(jobRepository.saveAndFlush(any(ExcelExportJob.class))).thenReturn(savedJob);

        ExportJobResponse response = jobService.requestExport("USER", request);

        assertNotNull(response);
        assertEquals("job-123", response.getId());
        assertEquals("USER", response.getReportType());
        assertEquals(ExcelExportStatus.PENDING, response.getStatus());
        verify(eventPublisher, times(1)).publishEvent(any(ExcelExportRequestEvent.class));
    }

    @Test
    void getJobStatus_OwnershipVerified_Success() {
        JwtUserPrincipal principal = new JwtUserPrincipal("user-123", "test_user");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(appSettingProperty.getSuperUser()).thenReturn("admin");

        ExcelExportJob job = ExcelExportJob.builder()
                .id("job-123")
                .reportType("USER")
                .status(ExcelExportStatus.PROCESSING)
                .requestedBy("test_user")
                .build();
        when(jobRepository.findById("job-123")).thenReturn(Optional.of(job));
        when(permissionResolutionService.getGlobalPermissions("user-123")).thenReturn(Set.of());

        ExportJobResponse response = jobService.getJobStatus("job-123");

        assertNotNull(response);
        assertEquals("job-123", response.getId());
        assertEquals(ExcelExportStatus.PROCESSING, response.getStatus());
    }

    @Test
    void getJobStatus_Forbidden_ThrowsException() {
        JwtUserPrincipal principal = new JwtUserPrincipal("user-123", "other_user");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(appSettingProperty.getSuperUser()).thenReturn("admin");

        ExcelExportJob job = ExcelExportJob.builder()
                .id("job-123")
                .reportType("USER")
                .status(ExcelExportStatus.PROCESSING)
                .requestedBy("test_user")
                .build();
        when(jobRepository.findById("job-123")).thenReturn(Optional.of(job));
        when(permissionResolutionService.getGlobalPermissions("user-123")).thenReturn(Set.of());

        assertThrows(BusinessException.class, () -> jobService.getJobStatus("job-123"));
    }

    @Test
    void cancelJob_Success() {
        JwtUserPrincipal principal = new JwtUserPrincipal("user-123", "test_user");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(appSettingProperty.getSuperUser()).thenReturn("admin");

        ExcelExportJob job = ExcelExportJob.builder()
                .id("job-123")
                .status(ExcelExportStatus.PENDING)
                .requestedBy("test_user")
                .build();
        when(jobRepository.findById("job-123")).thenReturn(Optional.of(job));
        when(permissionResolutionService.getGlobalPermissions("user-123")).thenReturn(Set.of());

        jobService.cancelJob("job-123");

        assertEquals(ExcelExportStatus.CANCELLED, job.getStatus());
        verify(jobRepository, times(1)).saveAndFlush(job);
    }
}
