package com.example.diagnoseillusion.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ConversationDetailResponse {

    private String conversationId;
    private String type;
    private Long teamId;
    private List<ChatMessageResponse> messages;

    @Data
    public static class ChatMessageResponse {
        private Long id;
        private String question;
        private String answer;
        private List<Long> categoryIds;
        private List<String> categoryNames;
        private LocalDateTime createTime;
    }
}
