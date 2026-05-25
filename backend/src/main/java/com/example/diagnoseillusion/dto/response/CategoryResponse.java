package com.example.diagnoseillusion.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryResponse {

    private Long id;
    private String categoryName;
    private Long fileCount;
    private Long syncedCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
