package com.ledger.ledgerservice.model.converter;

import com.ledger.ledgerservice.util.AesGcmCryptoUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return AesGcmCryptoUtil.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return AesGcmCryptoUtil.decrypt(dbData);
    }
}
