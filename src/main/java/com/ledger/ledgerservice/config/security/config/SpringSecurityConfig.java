package com.ledger.ledgerservice.config.security.config;

import com.ledger.ledgerservice.config.filter.HeaderFilter;
import com.ledger.ledgerservice.config.filter.JwtFilterChain;
import com.ledger.ledgerservice.config.properties.CorsProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SpringSecurityConfig {
    private final CorsProperty corsProp;
    private final HeaderFilter headerFilter;
    private final JwtFilterChain jwtFilterChain;

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity filterChain, CorsFilter corsFilter) throws Exception {
        filterChain.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        http -> http
                                .requestMatchers("/actuator/health", "/error").permitAll()
                                .requestMatchers("/api/v1/login/**").permitAll()
                                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                                .anyRequest().authenticated()
                );
        filterChain.addFilterBefore(headerFilter, CorsFilter.class);
        filterChain.addFilterBefore(corsFilter, CorsFilter.class);
        filterChain.addFilterBefore(jwtFilterChain, UsernamePasswordAuthenticationFilter.class);
        return filterChain.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowedOriginPatterns(corsProp.getAllowedOriginPatterns());
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(corsProp.getAllowedMethods());
//        config.setAllowedHeaders(corsProp.getAllowedHeaders());
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(corsProp.getExposedHeaders());
        config.setAllowCredentials(corsProp.getAllowCredentials());
        config.setMaxAge(corsProp.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(corsProp.getRegisterCorsConfig(), config);

        return new CorsFilter(source);
    }
}
