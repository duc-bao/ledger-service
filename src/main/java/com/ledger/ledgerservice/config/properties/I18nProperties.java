package com.ledger.ledgerservice.config.properties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "i18n")
public class I18nProperties {
    private String defaultLocale;
    private String defaultEncode;
    private List<String> supportedLocales;
    private MessageSourceProp messageSources;

    @Getter
    @Setter
    public static class MessageSourceProp {

        private List<String> basenames;
        private Duration cacheExpiresIn;
        private boolean reloadable;

    }
}
