package com.ledger.ledgerservice.config.feign.interceptor;

import com.ledger.ledgerservice.model.constant.HeaderConstant;
import com.ledger.ledgerservice.model.context.RequestContext;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class TraceIdFeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        String requestId = null;
        RequestContext ctx = RequestContextHolder.get();
        if (ctx != null) {
            requestId = ctx.getRequestId();
        }

        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        template.header(HeaderConstant.X_REQUEST_ID, requestId);
        log.info(
                "[FEIGN-TRACE] {} {} | traceId={} | headers={}",
                template.method(),
                template.url(),
                requestId,
                template.headers().keySet()
        );
    }
}
