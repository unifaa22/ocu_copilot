package com.example.diagnoseillusion.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeletedFlag implements ByteEnum {

    NOT_DELETED((byte) 0),
    DELETED((byte) 1);

    private final byte value;

    public boolean isDeleted() {
        return this == DELETED;
    }
}
