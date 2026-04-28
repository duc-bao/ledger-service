package com.ledger.ledgerservice.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;

@Converter(autoApply = false)
public class StringListConverter implements AttributeConverter<List<String>, String> {
    private static final String SPLIT_CHAR = ";";

    @Override
    public String convertToDatabaseColumn(List<String> stringList) {
        return (stringList != null && !stringList.isEmpty()) ? String.join(SPLIT_CHAR, stringList) : null;
    }

    @Override
    public List<String> convertToEntityAttribute(String string) {
        return StringUtils.hasText(string) ? Arrays.asList(string.split(SPLIT_CHAR)) : emptyList();
    }
}
