package com.example.diagnoseillusion.service;

import com.example.diagnoseillusion.common.CustomException;
import com.example.diagnoseillusion.common.PageUtils;
import com.example.diagnoseillusion.dto.common.PageResult;
import com.example.diagnoseillusion.dto.request.NoteSaveRequest;
import com.example.diagnoseillusion.dto.response.NoteDetailResponse;
import com.example.diagnoseillusion.dto.response.NoteListItemResponse;
import com.example.diagnoseillusion.entity.Note;
import com.example.diagnoseillusion.enums.DeletedFlag;
import com.example.diagnoseillusion.repository.NoteRepository;
import com.example.diagnoseillusion.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserService userService;

    public PageResult<NoteListItemResponse> list(Integer page, Integer size, String keyword, String tag) {
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageUtils.pageable(page, size);
        Page<Note> notes = noteRepository.searchByUser(userId, DeletedFlag.NOT_DELETED, keyword, pageable);
        List<NoteListItemResponse> filtered = new ArrayList<>();
        for (Note note : notes.getContent()) {
            if (tag != null && !tag.isBlank() && !matchesTag(note.getTags(), tag)) {
                continue;
            }
            filtered.add(toListItem(note));
        }
        if (tag != null && !tag.isBlank()) {
            return PageResult.of(filtered, filtered.size(), notes.getNumber() + 1, notes.getSize());
        }
        return PageUtils.toPageResult(notes.map(this::toListItem));
    }

    public NoteDetailResponse get(Long id) {
        Note note = requireOwned(id);
        return toDetail(note);
    }

    @Transactional
    public NoteDetailResponse create(NoteSaveRequest request) {
        Note note = new Note();
        note.setTitle(request.getTitle() != null ? request.getTitle() : "未命名笔记");
        note.setContent(request.getContent() != null ? request.getContent() : "");
        note.setTags(request.getTags() != null ? request.getTags() : List.of());
        note.setUser(userService.requireCurrentUser());
        note.setIsDeleted(DeletedFlag.NOT_DELETED);
        return toDetail(noteRepository.save(note));
    }

    @Transactional
    public NoteDetailResponse update(Long id, NoteSaveRequest request) {
        Note note = requireOwned(id);
        if (request.getTitle() != null) {
            note.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            note.setContent(request.getContent());
        }
        if (request.getTags() != null) {
            note.setTags(request.getTags());
        }
        return toDetail(noteRepository.save(note));
    }

    @Transactional
    public void delete(Long id) {
        Note note = requireOwned(id);
        note.setIsDeleted(DeletedFlag.DELETED);
        noteRepository.save(note);
    }

    private Note requireOwned(Long id) {
        return noteRepository.findByIdAndUser_IdAndIsDeleted(id, SecurityUtils.getCurrentUserId(), DeletedFlag.NOT_DELETED)
                .orElseThrow(() -> new CustomException(404, "笔记不存在"));
    }

    private boolean matchesTag(List<String> tags, String tag) {
        if (tags == null) {
            return false;
        }
        String lower = tag.toLowerCase();
        return tags.stream().anyMatch(t -> t != null && t.toLowerCase().contains(lower));
    }

    private NoteListItemResponse toListItem(Note note) {
        NoteListItemResponse item = new NoteListItemResponse();
        item.setId(note.getId());
        item.setTitle(note.getTitle());
        item.setTags(note.getTags());
        item.setCreateTime(note.getCreateTime());
        item.setUpdateTime(note.getUpdateTime());
        return item;
    }

    private NoteDetailResponse toDetail(Note note) {
        NoteDetailResponse detail = new NoteDetailResponse();
        detail.setId(note.getId());
        detail.setTitle(note.getTitle());
        detail.setContent(note.getContent());
        detail.setTags(note.getTags());
        detail.setCreateTime(note.getCreateTime());
        detail.setUpdateTime(note.getUpdateTime());
        return detail;
    }
}
