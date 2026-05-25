package com.example.diagnoseillusion.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShareStatus implements ByteEnum {

    DISABLED((byte) 0),
    ENABLED((byte) 1);

    private final byte value;

    public boolean isEnabled() {
        return this == ENABLED;
    }
}
