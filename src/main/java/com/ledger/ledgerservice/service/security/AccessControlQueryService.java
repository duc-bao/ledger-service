package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.context.RequestContext;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.model.dto.response.AuthorizedMenuListResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AccessControlQueryService {
    private final MenuPermissionService menuPermissionService;

    @Transactional(readOnly = true)
    public AuthorizedMenuListResponse getAuthorizedMenus() {
        RequestContext ctx = RequestContextHolder.get();
        if (ctx == null || !StringUtils.hasText(ctx.getUsername()) || !StringUtils.hasText(ctx.getUserId())) {
            throw new BusinessException(MessageCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        return AuthorizedMenuListResponse.builder()
                .userId(ctx.getUserId())
                .username(ctx.getUsername())
                .menus(menuPermissionService.getAuthorizedMenus(ctx.getUserId()))
                .build();
    }
}