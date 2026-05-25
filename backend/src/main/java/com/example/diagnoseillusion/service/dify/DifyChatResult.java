package com.example.diagnoseillusion.service.dify;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DifyChatResult {

    private String conversationId;
    private String answer;
}
