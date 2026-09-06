package com.ledger.ledgerservice.config.security.config;

import com.ledger.ledgerservice.config.filter.HeaderFilter;
import com.ledger.ledgerservice.config.filter.JwtFilterChain;
import com.ledger.ledgerservice.config.filter.PermissionFilter;
import com.ledger.ledgerservice.config.properties.CorsProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PermissionFilter permissionFilter;
    private final Environment environment;

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsFilter corsFilter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(cors -> cors.configurationSource(request -> null));
        var filterChain = http.authorizeHttpRequests(auth ->
                auth.requestMatchers("/actuator/health", "/error", "/api/v1/login/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated());
        filterChain.addFilterBefore(headerFilter, CorsFilter.class);
        filterChain.addFilterBefore(corsFilter, CorsFilter.class);
        filterChain.addFilterBefore(jwtFilterChain, UsernamePasswordAuthenticationFilter.class);
        filterChain.addFilterAfter(permissionFilter, JwtFilterChain.class);
        return filterChain.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        boolean isDevOrTest = environment.matchesProfiles("dev", "test", "local");
        if (isDevOrTest) {
            config.setAllowedOriginPatterns(List.of("*"));
        } else {
            List<String> origins = corsProp.getAllowedOriginPatterns();
            if (origins != null && !origins.isEmpty()) {
                config.setAllowedOriginPatterns(origins);
            } else {
                config.setAllowedOriginPatterns(List.of("https://ob-consent.savyint.com", "https://iam-uat.savyint.com"));
            }
        }

        config.setAllowedMethods(corsProp.getAllowedMethods());
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(corsProp.getExposedHeaders());
        config.setAllowCredentials(corsProp.getAllowCredentials());
        config.setMaxAge(corsProp.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(corsProp.getRegisterCorsConfig(), config);
        return new CorsFilter(source);
    }
}