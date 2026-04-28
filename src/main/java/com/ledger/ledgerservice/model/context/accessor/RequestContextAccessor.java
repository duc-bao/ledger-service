package com.ledger.ledgerservice.model.context.accessor;


import com.ledger.ledgerservice.model.context.RequestContext;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import io.micrometer.context.ThreadLocalAccessor;
import lombok.NonNull;

public class RequestContextAccessor implements ThreadLocalAccessor<RequestContext> {

    public static final String CONTEXT_KEY  = "ledger-service:request-context";

    @NonNull
    @Override
    public Object key() {
            return CONTEXT_KEY ;
    }

    @Override
    public RequestContext getValue() {
        return RequestContextHolder.get();
    }

    @Override
    public void setValue(@NonNull RequestContext value) {
        RequestContextHolder.set(value);
    }

}
