package com.example.diagnoseillusion.controller;

import com.example.diagnoseillusion.common.Result;
import com.example.diagnoseillusion.dto.request.CategoryNameRequest;
import com.example.diagnoseillusion.dto.response.CategoryResponse;
import com.example.diagnoseillusion.dto.response.SyncResultResponse;
import com.example.diagnoseillusion.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<List<CategoryResponse>> list() {
        return Result.success(categoryService.listMyCategories());
    }

    @PostMapping
    public Result<CategoryResponse> create(@Valid @RequestBody CategoryNameRequest request) {
        return Result.success(categoryService.create(request));
    }

    @PutMapping("/{id}")
    public Result<CategoryResponse> rename(@PathVariable Long id, @Valid @RequestBody CategoryNameRequest request) {
        return Result.success(categoryService.rename(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/sync")
    public Result<SyncResultResponse> sync(@PathVariable Long id) {
        return Result.success(categoryService.sync(id));
    }
}
