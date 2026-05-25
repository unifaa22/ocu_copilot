package com.example.diagnoseillusion.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TeamMemberStatus implements ByteEnum {

    PENDING((byte) 0),
    JOINED((byte) 1),
    REJECTED((byte) 2);

    private final byte value;

    public boolean isJoined() {
        return this == JOINED;
    }

    public boolean isPending() {
        return this == PENDING;
    }
}
