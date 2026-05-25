package com.example.diagnoseillusion.enums.converter;

import com.example.diagnoseillusion.enums.ByteEnum;
import jakarta.persistence.AttributeConverter;

public abstract class AbstractByteEnumConverter<E extends Enum<E> & ByteEnum>
        implements AttributeConverter<E, Byte> {

    private final Class<E> enumClass;

    protected AbstractByteEnumConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public Byte convertToDatabaseColumn(E attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public E convertToEntityAttribute(Byte dbData) {
        if (dbData == null) {
            return null;
        }
        return ByteEnum.fromValue(enumClass, dbData);
    }
}
