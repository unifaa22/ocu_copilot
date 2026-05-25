package com.example.diagnoseillusion.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberRole implements ByteEnum {

    MEMBER((byte) 0),
    CREATOR((byte) 1);

    private final byte value;

    public boolean isCreator() {
        return this == CREATOR;
    }
}
