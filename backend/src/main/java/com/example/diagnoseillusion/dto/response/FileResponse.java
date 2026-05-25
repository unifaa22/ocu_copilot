package com.example.diagnoseillusion.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileResponse {

    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Long categoryId;
    private Byte syncStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
