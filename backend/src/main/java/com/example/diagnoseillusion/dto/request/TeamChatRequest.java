package com.example.diagnoseillusion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeamChatRequest {

    @NotBlank(message = "问题不能为空")
    private String question;

    @NotNull(message = "团队ID不能为空")
    private Long teamId;

    private String conversationId;
}
