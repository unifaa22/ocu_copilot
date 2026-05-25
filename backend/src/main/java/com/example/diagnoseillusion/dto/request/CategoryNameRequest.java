package com.example.diagnoseillusion.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryNameRequest {

    @NotBlank(message = "分类名称不能为空")
    private String categoryName;
}
