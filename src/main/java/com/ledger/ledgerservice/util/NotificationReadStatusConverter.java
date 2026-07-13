package com.ledger.ledgerservice.util;

import com.ledger.ledgerservice.model.enums.NotificationReadStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class NotificationReadStatusConverter implements AttributeConverter<NotificationReadStatus, String> {

    @Override
    public String convertToDatabaseColumn(NotificationReadStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public NotificationReadStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : NotificationReadStatus.fromCode(dbData);
    }
}
