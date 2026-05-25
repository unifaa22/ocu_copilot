package com.example.diagnoseillusion.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamMemberResponse {

    private Long id;
    private Long userId;
    private String username;
    private Byte memberRole;
    private Byte status;
    private LocalDateTime joinTime;
}
