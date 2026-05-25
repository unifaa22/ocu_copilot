package com.example.diagnoseillusion.enums.converter;

import com.example.diagnoseillusion.enums.ShareStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ShareStatusConverter extends AbstractByteEnumConverter<ShareStatus> {

    public ShareStatusConverter() {
        super(ShareStatus.class);
    }
}
