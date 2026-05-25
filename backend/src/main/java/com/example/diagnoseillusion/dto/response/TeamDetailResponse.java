package com.example.diagnoseillusion.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamDetailResponse {

    private Long id;
    private String teamName;
    private Long creatorId;
    private String creatorName;
    private Byte isShare;
    private Byte myMemberRole;
    private Byte myStatus;
    private long memberCount;
    private LocalDateTime createTime;
}
