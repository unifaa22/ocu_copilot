package com.example.diagnoseillusion.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class NoteSaveRequest {

    private String title;
    private String content;
    private List<String> tags;
}
