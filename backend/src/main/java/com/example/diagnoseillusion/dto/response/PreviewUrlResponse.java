package com.example.diagnoseillusion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PreviewUrlResponse {

    private String previewUrl;
    private int expireSeconds;
}
