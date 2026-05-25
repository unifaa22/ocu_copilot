package com.example.diagnoseillusion.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PendingInvitationResponse {

    private Long teamId;
    private String teamName;
    private String creatorName;
    private LocalDateTime inviteTime;
}
