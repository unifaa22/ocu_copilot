package com.example.diagnoseillusion.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteDetailResponse {

    private Long id;
    private String title;
    private String content;
    private List<String> tags;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
