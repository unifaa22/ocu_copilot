package com.example.diagnoseillusion.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FileRenameRequest {

    @NotBlank(message = "文件名不能为空")
    private String fileName;
}
