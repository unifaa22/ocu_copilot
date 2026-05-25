package com.example.diagnoseillusion.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SyncStatus implements ByteEnum {

    UNSYNCED((byte) 0),
    SUCCESS((byte) 1),
    FAILED((byte) 2);

    private final byte value;

    public boolean needsSync() {
        return this == UNSYNCED || this == FAILED;
    }
}
