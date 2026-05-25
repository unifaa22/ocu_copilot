package com.example.diagnoseillusion.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ConversationSummaryResponse {

    private String conversationId;
    private String type;
    private Long teamId;
    private String teamName;
    private String lastQuestion;
    private String lastAnswer;
    private int messageCount;
    private List<String> categoryNames;
    private LocalDateTime lastTime;
}
