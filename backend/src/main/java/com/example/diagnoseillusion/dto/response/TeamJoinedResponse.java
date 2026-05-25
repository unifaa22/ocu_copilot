package com.example.diagnoseillusion.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamJoinedResponse {

    private Long id;
    private String teamName;
    private Long creatorId;
    private String creatorName;
    private Byte isShare;
    private Boolean isCreator;
    private LocalDateTime joinTime;
}
