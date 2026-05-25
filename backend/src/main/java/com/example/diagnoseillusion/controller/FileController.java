package com.example.diagnoseillusion.controller;

import com.example.diagnoseillusion.common.Result;
import com.example.diagnoseillusion.dto.common.PageResult;
import com.example.diagnoseillusion.dto.request.FileRenameRequest;
import com.example.diagnoseillusion.dto.response.FileResponse;
import com.example.diagnoseillusion.dto.response.PreviewUrlResponse;
import com.example.diagnoseillusion.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/api/categories/{categoryId}/files")
    public Result<PageResult<FileResponse>> listByCategory(
            @PathVariable Long categoryId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Byte syncStatus) {
        return Result.success(fileService.listByCategory(categoryId, page, size, syncStatus));
    }

    @GetMapping("/api/files")
    public Result<PageResult<FileResponse>> listAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Byte syncStatus) {
        return Result.success(fileService.listAll(page, size, syncStatus));
    }

    @PostMapping("/api/categories/{categoryId}/files")
    public Result<FileResponse> upload(@PathVariable Long categoryId, @RequestParam("file") MultipartFile file) {
        return Result.success(fileService.upload(categoryId, file));
    }

    @PutMapping("/api/files/{id}")
    public Result<FileResponse> rename(@PathVariable Long id, @Valid @RequestBody FileRenameRequest request) {
        return Result.success(fileService.rename(id, request));
    }

    @DeleteMapping("/api/files/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return Result.success();
    }

    @GetMapping("/api/files/{fileId}/preview-url")
    public Result<PreviewUrlResponse> previewUrl(@PathVariable Long fileId) {
        return Result.success(fileService.previewUrl(fileId));
    }
}
