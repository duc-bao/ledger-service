package com.ledger.ledgerservice.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class MessageHelper {

    private final MessageSource messageSource;

    public String getMsg(String key) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, new Object[]{}, locale);
    }

    public String getMsg(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, args, locale);
    }

    public String getMsg(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }

    public String getMsg(String key, Locale locale) {
        return messageSource.getMessage(key, new Object[]{}, locale);
    }

    public String getMsg(String key, String defMsg) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, new Object[]{}, defMsg, locale);
    }

    public String getMsg(String key, Locale locale, String defMsg, Object... args) {
        return messageSource.getMessage(key, args, defMsg, locale);
    }

}
