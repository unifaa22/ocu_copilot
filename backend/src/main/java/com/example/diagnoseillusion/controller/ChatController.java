package com.example.diagnoseillusion.controller;

import com.example.diagnoseillusion.common.Result;
import com.example.diagnoseillusion.dto.common.PageResult;
import com.example.diagnoseillusion.dto.request.PersonalChatRequest;
import com.example.diagnoseillusion.dto.request.TeamChatRequest;
import com.example.diagnoseillusion.dto.response.ChatAnswerResponse;
import com.example.diagnoseillusion.dto.response.ConversationDetailResponse;
import com.example.diagnoseillusion.dto.response.ConversationSummaryResponse;
import com.example.diagnoseillusion.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/personal")
    public Result<ChatAnswerResponse> personal(@Valid @RequestBody PersonalChatRequest request) {
        return Result.success(chatService.personalChat(request));
    }

    @PostMapping("/team")
    public Result<ChatAnswerResponse> team(@Valid @RequestBody TeamChatRequest request) {
        return Result.success(chatService.teamChat(request));
    }

    @GetMapping("/conversations")
    public Result<PageResult<ConversationSummaryResponse>> conversations(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String type) {
        return Result.success(chatService.listConversations(page, size, type));
    }

    @GetMapping("/conversations/{conversationId}")
    public Result<ConversationDetailResponse> conversationDetail(@PathVariable String conversationId) {
        return Result.success(chatService.conversationDetail(conversationId));
    }

    @DeleteMapping("/history/{id}")
    public Result<Void> deleteHistory(@PathVariable Long id) {
        chatService.deleteHistory(id);
        return Result.success();
    }

    @DeleteMapping("/conversations/{conversationId}")
    public Result<Void> deleteConversation(@PathVariable String conversationId) {
        chatService.deleteConversation(conversationId);
        return Result.success();
    }

    @DeleteMapping("/history")
    public Result<Void> clearHistory() {
        chatService.clearAllHistory();
        return Result.success();
    }
}
