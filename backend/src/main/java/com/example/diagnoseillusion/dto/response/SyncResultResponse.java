package com.example.diagnoseillusion.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class SyncResultResponse {

    private Long categoryId;
    private int total;
    private int successCount;
    private int failCount;
    private List<FailedFileItem> failedFiles;

    @Data
    public static class FailedFileItem {
        private Long fileId;
        private String fileName;
        private Byte syncStatus;
        private String errorMessage;
    }
}
