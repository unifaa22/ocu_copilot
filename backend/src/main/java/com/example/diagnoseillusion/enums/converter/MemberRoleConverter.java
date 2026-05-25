package com.example.diagnoseillusion.enums.converter;

import com.example.diagnoseillusion.enums.MemberRole;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MemberRoleConverter extends AbstractByteEnumConverter<MemberRole> {

    public MemberRoleConverter() {
        super(MemberRole.class);
    }
}
