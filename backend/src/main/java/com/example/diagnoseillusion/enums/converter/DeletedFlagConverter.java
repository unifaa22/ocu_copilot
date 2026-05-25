package com.example.diagnoseillusion.enums.converter;

import com.example.diagnoseillusion.enums.DeletedFlag;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DeletedFlagConverter extends AbstractByteEnumConverter<DeletedFlag> {

    public DeletedFlagConverter() {
        super(DeletedFlag.class);
    }
}
