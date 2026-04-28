package com.ledger.ledgerservice.service.menu;

import com.ledger.ledgerservice.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MenuLocalizationService {
    private static final String MENU_KEY_PREFIX = "menu.";

    private final MessageHelper messageHelper;

    public String getDisplayName(String menuCode) {
        Locale locale = LocaleContextHolder.getLocale();
        String key = MENU_KEY_PREFIX + menuCode;
        return messageHelper.getMsg(key, locale, menuCode);
    }
}
