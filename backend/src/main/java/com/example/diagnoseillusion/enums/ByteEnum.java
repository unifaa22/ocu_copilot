package com.example.diagnoseillusion.enums;

public interface ByteEnum {

    byte getValue();

    static <E extends Enum<E> & ByteEnum> E fromValue(Class<E> enumClass, byte value) {
        for (E constant : enumClass.getEnumConstants()) {
            if (constant.getValue() == value) {
                return constant;
            }
        }
        throw new IllegalArgumentException("Unknown " + enumClass.getSimpleName() + " value: " + value);
    }
}
