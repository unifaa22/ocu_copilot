package com.example.diagnoseillusion.service;

import com.example.diagnoseillusion.common.CustomException;
import com.example.diagnoseillusion.common.PageUtils;
import com.example.diagnoseillusion.dto.common.PageResult;
import com.example.diagnoseillusion.dto.request.PersonalChatRequest;
import com.example.diagnoseillusion.dto.request.TeamChatRequest;
import com.example.diagnoseillusion.dto.response.ChatAnswerResponse;
import com.example.diagnoseillusion.dto.response.ConversationDetailResponse;
import com.example.diagnoseillusion.dto.response.ConversationSummaryResponse;
import com.example.diagnoseillusion.entity.ChatHistory;
import com.example.diagnoseillusion.entity.FileCategory;
import com.example.diagnoseillusion.entity.Team;
import com.example.diagnoseillusion.enums.DeletedFlag;
import com.example.diagnoseillusion.enums.SyncStatus;
import com.example.diagnoseillusion.repository.ChatHistoryRepository;
import com.example.diagnoseillusion.repository.FileCategoryRepository;
import com.example.diagnoseillusion.repository.KnowledgeFileRepository;
import com.example.diagnoseillusion.security.SecurityUtils;
import com.example.diagnoseillusion.service.dify.DifyChatResult;
import com.example.diagnoseillusion.service.dify.DifyClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatHistoryRepository chatHistoryRepository;
    private final FileCategoryRepository fileCategoryRepository;
    private final KnowledgeFileRepository knowledgeFileRepository;
    private final DifyClient difyClient;
    private final UserService userService;
    private final TeamService teamService;

    @Transactional
    public ChatAnswerResponse personalChat(PersonalChatRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<FileCategory> cats = fileCategoryRepository.findByUser_IdAndIsDeletedOrderByCreateTimeDesc(userId, DeletedFlag.NOT_DELETED)
                .stream()
                .filter(c -> request.getCategoryIds().contains(c.getId()))
                .toList();
        if (cats.isEmpty()) {
            throw new CustomException(400, "请至少选择一个知识库分类");
        }
        List<String> datasetIds = cats.stream()
                .map(FileCategory::getDifyDatasetId)
                .filter(id -> id != null && !id.isBlank())
                .toList();
        DifyChatResult result = difyClient.chat(request.getQuestion(), request.getConversationId(), datasetIds);
        ChatHistory history = saveHistory(userId, result.getConversationId(), request.getQuestion(), result.getAnswer(),
                cats.stream().map(FileCategory::getId).toList(),
                cats.stream().map(FileCategory::getCategoryName).toList(),
                null);
        return buildAnswer(result, history, cats.stream().map(FileCategory::getId).toList(),
                cats.stream().map(FileCategory::getCategoryName).toList(), null, null);
    }

    @Transactional
    public ChatAnswerResponse teamChat(TeamChatRequest request) {
        Team team = teamService.requireSharedTeamAccess(request.getTeamId());
        Long creatorId = team.getCreator().getId();
        List<FileCategory> cats = fileCategoryRepository.findByUser_IdAndIsDeleted(creatorId, DeletedFlag.NOT_DELETED)
                .stream()
                .filter(c -> knowledgeFileRepository.existsByCategory_IdAndSyncStatusAndIsDeleted(
                        c.getId(), SyncStatus.SUCCESS, DeletedFlag.NOT_DELETED))
                .toList();
        List<String> datasetIds = cats.stream()
                .map(FileCategory::getDifyDatasetId)
                .filter(id -> id != null && !id.isBlank())
                .toList();
        DifyChatResult result = difyClient.chat(request.getQuestion(), request.getConversationId(), datasetIds);
        List<Long> categoryIds = cats.stream().map(FileCategory::getId).toList();
        List<String> categoryNames = cats.stream().map(FileCategory::getCategoryName).toList();
        ChatHistory history = saveHistory(SecurityUtils.getCurrentUserId(), result.getConversationId(),
                request.getQuestion(), result.getAnswer(), categoryIds, categoryNames, team);
        return buildAnswer(result, history, categoryIds, categoryNames, team.getId(), team.getTeamName());
    }

    public PageResult<ConversationSummaryResponse> listConversations(Integer page, Integer size, String type) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean personalOnly = "personal".equalsIgnoreCase(type);
        boolean teamOnly = "team".equalsIgnoreCase(type);
        List<ChatHistory> all = chatHistoryRepository.findActiveForUser(
                userId, DeletedFlag.NOT_DELETED, personalOnly, teamOnly);

        Map<String, ConversationSummaryResponse> map = new LinkedHashMap<>();
        Map<String, Integer> counts = new HashMap<>();
        for (ChatHistory h : all) {
            counts.merge(h.getConversationId(), 1, Integer::sum);
            ConversationSummaryResponse existing = map.get(h.getConversationId());
            if (existing == null || h.getCreateTime().isAfter(existing.getLastTime())) {
                ConversationSummaryResponse summary = new ConversationSummaryResponse();
                summary.setConversationId(h.getConversationId());
                summary.setType(h.getTeam() != null ? "team" : "personal");
                summary.setTeamId(h.getTeam() != null ? h.getTeam().getId() : null);
                summary.setTeamName(h.getTeam() != null ? h.getTeam().getTeamName() : null);
                summary.setLastQuestion(h.getQuestion());
                summary.setLastAnswer(h.getAnswer());
                summary.setCategoryNames(h.getCategoryNames());
                summary.setLastTime(h.getCreateTime());
                map.put(h.getConversationId(), summary);
            }
        }
        for (ConversationSummaryResponse s : map.values()) {
            s.setMessageCount(counts.getOrDefault(s.getConversationId(), 0));
        }
        List<ConversationSummaryResponse> sorted = map.values().stream()
                .sorted(Comparator.comparing(ConversationSummaryResponse::getLastTime).reversed())
                .toList();
        return manualPaginate(sorted, page, size);
    }

    public ConversationDetailResponse conversationDetail(String conversationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<ChatHistory> messages = chatHistoryRepository
                .findByConversationIdAndUser_IdAndIsDeletedOrderByCreateTimeAsc(conversationId, userId, DeletedFlag.NOT_DELETED);
        if (messages.isEmpty()) {
            throw new CustomException(404, "会话不存在");
        }
        ChatHistory first = messages.get(0);
        ConversationDetailResponse detail = new ConversationDetailResponse();
        detail.setConversationId(conversationId);
        detail.setType(first.getTeam() != null ? "team" : "personal");
        detail.setTeamId(first.getTeam() != null ? first.getTeam().getId() : null);
        detail.setMessages(messages.stream().map(this::toMessage).collect(Collectors.toList()));
        return detail;
    }

    @Transactional
    public void deleteHistory(Long id) {
        ChatHistory record = chatHistoryRepository.findByIdAndUser_Id(id, SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new CustomException(404, "记录不存在"));
        record.setIsDeleted(DeletedFlag.DELETED);
        chatHistoryRepository.save(record);
    }

    @Transactional
    public void deleteConversation(String conversationId) {
        int updated = chatHistoryRepository.softDeleteByConversation(
                conversationId, SecurityUtils.getCurrentUserId(), DeletedFlag.DELETED);
        if (updated == 0) {
            throw new CustomException(404, "会话不存在");
        }
    }

    @Transactional
    public void clearAllHistory() {
        chatHistoryRepository.softDeleteAllByUser(
                SecurityUtils.getCurrentUserId(), DeletedFlag.DELETED, DeletedFlag.NOT_DELETED);
    }

    private ChatHistory saveHistory(Long userId, String conversationId, String question, String answer,
                                    List<Long> categoryIds, List<String> categoryNames, Team team) {
        ChatHistory history = new ChatHistory();
        history.setConversationId(conversationId);
        history.setQuestion(question);
        history.setAnswer(answer);
        history.setCategoryIds(categoryIds);
        history.setCategoryNames(categoryNames);
        history.setUser(userService.requireCurrentUser());
        history.setTeam(team);
        history.setIsDeleted(DeletedFlag.NOT_DELETED);
        return chatHistoryRepository.save(history);
    }

    private ChatAnswerResponse buildAnswer(DifyChatResult result, ChatHistory history,
                                           List<Long> categoryIds, List<String> categoryNames,
                                           Long teamId, String teamName) {
        ChatAnswerResponse response = new ChatAnswerResponse();
        response.setConversationId(result.getConversationId());
        response.setAnswer(result.getAnswer());
        response.setHistoryId(history.getId());
        response.setCategoryIds(categoryIds);
        response.setCategoryNames(categoryNames);
        response.setTeamId(teamId);
        response.setTeamName(teamName);
        return response;
    }

    private ConversationDetailResponse.ChatMessageResponse toMessage(ChatHistory h) {
        ConversationDetailResponse.ChatMessageResponse msg = new ConversationDetailResponse.ChatMessageResponse();
        msg.setId(h.getId());
        msg.setQuestion(h.getQuestion());
        msg.setAnswer(h.getAnswer());
        msg.setCategoryIds(h.getCategoryIds());
        msg.setCategoryNames(h.getCategoryNames());
        msg.setCreateTime(h.getCreateTime());
        return msg;
    }

    private <T> PageResult<T> manualPaginate(List<T> list, Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : Math.min(size, 100);
        int start = (p - 1) * s;
        int end = Math.min(start + s, list.size());
        List<T> slice = start >= list.size() ? List.of() : list.subList(start, end);
        return PageResult.of(slice, list.size(), p, s);
    }
}
