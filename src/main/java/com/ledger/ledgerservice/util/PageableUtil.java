package com.ledger.ledgerservice.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class PageableUtil {
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    private PageableUtil() {
    }

    public static Pageable clamp(Pageable pageable) {
        return clamp(pageable, DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);
    }

    public static Pageable clamp(Pageable pageable, int defaultSize, int maxSize) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, defaultSize);
        }
        int pageNumber = Math.max(pageable.getPageNumber(), 0);
        int pageSize = pageable.getPageSize() <= 0 ? defaultSize : Math.min(pageable.getPageSize(), maxSize);
        return PageRequest.of(pageNumber, pageSize, pageable.getSort());
    }
}
