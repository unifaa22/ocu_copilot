package com.example.diagnoseillusion.enums.converter;

import com.example.diagnoseillusion.enums.TeamMemberStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TeamMemberStatusConverter extends AbstractByteEnumConverter<TeamMemberStatus> {

    public TeamMemberStatusConverter() {
        super(TeamMemberStatus.class);
    }
}
