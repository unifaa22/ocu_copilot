package com.example.diagnoseillusion.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteListItemResponse {

    private Long id;
    private String title;
    private List<String> tags;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
