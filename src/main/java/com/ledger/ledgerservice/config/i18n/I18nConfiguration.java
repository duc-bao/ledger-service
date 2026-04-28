package com.ledger.ledgerservice.config.i18n;

import com.ledger.ledgerservice.config.properties.I18nProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

@Configuration
@RequiredArgsConstructor
public class I18nConfiguration {
    private final I18nProperties i18nProp;

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.forLanguageTag(i18nProp.getDefaultLocale()));
        resolver.setSupportedLocales(i18nProp.getSupportedLocales().stream().map(Locale::forLanguageTag).toList());
        return resolver;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        I18nProperties.MessageSourceProp msgSourceProp = i18nProp.getMessageSources();

        messageSource.setDefaultEncoding(i18nProp.getDefaultEncode());
        messageSource.setBasenames(msgSourceProp.getBasenames().toArray(new String[0]));
        messageSource.setCacheSeconds((int) msgSourceProp.getCacheExpiresIn().getSeconds());

        if (msgSourceProp.isReloadable()) {
            messageSource.setCacheMillis((int) msgSourceProp.getCacheExpiresIn().toMillis());
        }

        return messageSource;
    }

    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        return validator;
    }
}
