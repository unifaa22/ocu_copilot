package com.example.diagnoseillusion.controller;

import com.example.diagnoseillusion.common.Result;
import com.example.diagnoseillusion.dto.common.PageResult;
import com.example.diagnoseillusion.dto.request.NoteSaveRequest;
import com.example.diagnoseillusion.dto.response.NoteDetailResponse;
import com.example.diagnoseillusion.dto.response.NoteListItemResponse;
import com.example.diagnoseillusion.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping
    public Result<PageResult<NoteListItemResponse>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag) {
        return Result.success(noteService.list(page, size, keyword, tag));
    }

    @GetMapping("/{id}")
    public Result<NoteDetailResponse> get(@PathVariable Long id) {
        return Result.success(noteService.get(id));
    }

    @PostMapping
    public Result<NoteDetailResponse> create(@RequestBody NoteSaveRequest request) {
        return Result.success(noteService.create(request));
    }

    @PutMapping("/{id}")
    public Result<NoteDetailResponse> update(@PathVariable Long id, @RequestBody NoteSaveRequest request) {
        return Result.success(noteService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noteService.delete(id);
        return Result.success();
    }
}
