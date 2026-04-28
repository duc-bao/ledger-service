package com.ledger.ledgerservice.model.context.holder;

import com.ledger.ledgerservice.model.constant.MDCConstant;
import com.ledger.ledgerservice.model.context.RequestContext;
import org.slf4j.MDC;

import java.util.Optional;

public class RequestContextHolder {

    private static final ThreadLocal<RequestContext> CTX = new ThreadLocal<>();

    private RequestContextHolder() {
    }

    public static RequestContext get() {
        return CTX.get();
    }

    public static void set(RequestContext ctx) {
        CTX.set(ctx);
        MDC.put(MDCConstant.REQUEST_ID, ctx.getRequestId());
        MDC.put(MDCConstant.SPAN_ID, ctx.getSpanId());
    }

    public static String getRequestId() {
        RequestContext ctx = RequestContextHolder.get();
        return Optional.ofNullable(ctx).map(RequestContext::getRequestId).orElse(null);
    }

    public static void setCtxError(String errorCode, String errorMsg) {
        RequestContext ctx = RequestContextHolder.get();
        Optional.ofNullable(ctx).ifPresent(c -> {
            c.setErrorCode(errorCode);
            ctx.setErrorMsg(errorMsg);
        });
    }

    public static void clear() {
        CTX.remove();
        MDC.clear();
    }

}
