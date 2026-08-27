//package com.ledger.ledgerservice.config.security.permission;
//
//import com.ledger.ledgerservice.config.filter.PermissionFilter;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
//import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//
//@Configuration
//@EnableMethodSecurity(securedEnabled = true)
//@RequiredArgsConstructor
//public class MethodSecurityConfig {
//    private final PermissionFilter permissionFilter;
//
//    @Bean
//    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
//        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
//        handler.setPermissionEvaluator(permissionFilter);
//        return handler;
//    }
//}
