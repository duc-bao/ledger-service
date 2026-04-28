package com.ledger.ledgerservice.config.properties;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "app-setting")
@Configuration
@RequiredArgsConstructor
@Data
public class AppSettingProperty {
    @Value("${app-setting.interceptor}")
    private boolean interceptor;

    @Value("${app-setting.seed}")
    private boolean seed;

    @Value("${app-setting.superUser}")
    private String superUser;

    @Value("${app-setting.password}")
    private String password;

    @Value("${app-setting.artifact}")
    private String artifact;

    @Value("${app-setting.version}")
    private String version;

    @Value("${app-setting.resetPass}")
    private String resetPass;

    @Value("${app-setting.urlLogin}")
    private String urlLogin;

    @Value("${app-setting.seedDataSystemParam}")
    private String seedDataSystemParam;

    @Value("${app-setting.tempDirectory}")
    private String tempDirectory;
}
