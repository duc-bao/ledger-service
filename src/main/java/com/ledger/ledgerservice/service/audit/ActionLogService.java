package com.ledger.ledgerservice.service.audit;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.context.RequestContext;
import com.ledger.ledgerservice.model.dto.request.AuditLogSearchRequest;
import com.ledger.ledgerservice.model.dto.response.AuditLogItemResponse;
import com.ledger.ledgerservice.model.dto.response.AuditLogResponse;
import com.ledger.ledgerservice.model.entity.ActionLog;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.repository.ActionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActionLogService {

    private final ActionLogRepository actionLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Transactional
    @Async("requestLogExecutor")
    public void save(RequestContext ctx) {
        if (ctx == null || ctx.getRequestId() == null || ctx.getRequestId().isBlank()) {
            return;
        }

        try {
            ActionLog logEntity = ActionLog.builder()
                    .requestId(ctx.getRequestId())
                    .spanId(ctx.getSpanId())
                    .username(ctx.getUsername())
                    .service(ctx.getService())
                    .action(ctx.getAction())
                    .requestMethod(ctx.getRequestMethod())
                    .requestUrl(ctx.getRequestUrl())
                    .requestUrlPath(ctx.getRequestUrlPath())
                    .requestQuery(ctx.getRequestQuery())
                    .requestIp(ctx.getRequestIp())
                    .userAgent(ctx.getUserAgent())
                    .statusCode(ctx.getStatusCode())
                    .errorCode(ctx.getErrorCode())
                    .errorMsg(ctx.getErrorMsg())
                    .requestStart(ctx.getRequestStart())
                    .requestEnd(ctx.getRequestEnd())
                    .durationMs(ctx.getDurationMs())
                    .description(ctx.getErrorMsg())
                    .build();

            actionLogRepository.save(logEntity);
        } catch (Exception ex) {
            log.error("Failed to persist action log for requestId={}", ctx.getRequestId(), ex);
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLogItemResponse> search(AuditLogSearchRequest request) {
        int requestPage = request.getPage() == null ? 1 : request.getPage();
        int page = requestPage <= 0 ? 0 : requestPage - 1;
        int size = request.getSize() == null ? 20 : request.getSize();
        Sort.Direction direction = "ASC".equalsIgnoreCase(request.getOrder()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = resolveSortBy(request.getSort());

        Specification<ActionLog> specification = buildSpecification(request);
        Page<ActionLog> result = actionLogRepository.findAll(specification, PageRequest.of(page, size, Sort.by(direction, sortBy)));

        return result.map(auditLogMapper::toItem);
    }

    @Transactional(readOnly = true)
    public AuditLogResponse getDetail(String logId) {
        ActionLog actionLog = actionLogRepository.findById(logId)
                .orElseThrow(() -> new BusinessException(MessageCode.NOT_FOUND, HttpStatus.NOT_FOUND));
        return auditLogMapper.toDetail(actionLog);
    }

    private String resolveSortBy(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return "requestStart";
        }
        return switch (sortBy) {
            case "durationMs", "statusCode", "username", "action", "requestStart" -> sortBy;
            default -> "requestStart";
        };
    }

    private Specification<ActionLog> buildSpecification(AuditLogSearchRequest request) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (request.getStatusCode() != null) {
                predicates.add(cb.equal(root.get("statusCode"), request.getStatusCode()));
            }
            if (request.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("requestStart"), request.getFromDate()));
            }
            if (request.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("requestStart"), request.getToDate()));
            }
            if (StringUtils.hasText(request.getRequestId())) {
                predicates.add(cb.like(cb.lower(root.get("requestId")), like(request.getRequestId())));
            }
            if (StringUtils.hasText(request.getUsername())) {
                predicates.add(cb.like(cb.lower(root.get("username")), like(request.getUsername())));
            }
            if (StringUtils.hasText(request.getAction())) {
                predicates.add(cb.like(cb.lower(root.get("action")), like(request.getAction())));
            }
            if (StringUtils.hasText(request.getRequestMethod())) {
                predicates.add(cb.equal(cb.upper(root.get("requestMethod")), request.getRequestMethod().trim().toUpperCase(Locale.ROOT)));
            }

            if (StringUtils.hasText(request.getKeyword())) {
                String keywordLike = like(request.getKeyword());
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("requestId")), keywordLike),
                        cb.like(cb.lower(root.get("username")), keywordLike),
                        cb.like(cb.lower(root.get("action")), keywordLike),
                        cb.like(cb.lower(root.get("requestMethod")), keywordLike),
                        cb.like(cb.lower(root.get("requestUrl")), keywordLike),
                        cb.like(cb.lower(root.get("requestUrlPath")), keywordLike),
                        cb.like(cb.lower(root.get("requestIp")), keywordLike),
                        cb.like(cb.lower(root.get("errorCode")), keywordLike),
                        cb.like(cb.lower(root.get("errorMsg")), keywordLike)
                ));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }

}
