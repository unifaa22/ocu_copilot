package com.example.diagnoseillusion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class PersonalChatRequest {

    @NotBlank(message = "问题不能为空")
    private String question;

    @NotEmpty(message = "请至少选择一个知识库分类")
    private List<Long> categoryIds;

    private String conversationId;
}
