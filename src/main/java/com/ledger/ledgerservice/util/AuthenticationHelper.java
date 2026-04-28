package com.ledger.ledgerservice.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationHelper {
    private final ObjectProvider<AuthenticationManager> authManager;

    public String getUsername() {
        return Optional.ofNullable(getAuthContext()).map(Authentication::getName).orElse(null);
    }

    public Authentication authenticate(Authentication auth) {
        return getAuthManager().authenticate(auth);
    }

    public UsernamePasswordAuthenticationToken setAuthToken(Object principal, Object credential) {
        return new UsernamePasswordAuthenticationToken(principal, credential, Collections.emptyList());
    }

    public void setAuthContext(Authentication auth) {
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public boolean isAuthenticated() {
        Authentication auth = getAuthContext();
        return ObjectUtils.isNotEmpty(auth) && auth.isAuthenticated() && !isAnonymous();
    }

    public boolean isUnauthenticated() {
        return !isAuthenticated();
    }

    public boolean isAnonymous() {
        return Optional.ofNullable(getAuthContext()).filter(authentication -> authentication instanceof AnonymousAuthenticationToken).isPresent();
    }

    private Authentication getAuthContext() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private AuthenticationManager getAuthManager() {
        return authManager.getIfAvailable();
    }
}
