package com.example.diagnoseillusion.enums.converter;

import com.example.diagnoseillusion.enums.SyncStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SyncStatusConverter extends AbstractByteEnumConverter<SyncStatus> {

    public SyncStatusConverter() {
        super(SyncStatus.class);
    }
}
