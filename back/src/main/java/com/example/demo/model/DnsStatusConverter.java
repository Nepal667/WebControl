package com.example.demo.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DnsStatusConverter implements AttributeConverter<DnsStatus, String> {

    @Override
    public String convertToDatabaseColumn(DnsStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public DnsStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : DnsStatus.valueOf(dbData);
    }
}