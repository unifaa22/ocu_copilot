package com.example.diagnoseillusion.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ChatAnswerResponse {

    private String conversationId;
    private String answer;
    private Long historyId;
    private List<Long> categoryIds;
    private List<String> categoryNames;
    private Long teamId;
    private String teamName;
}
