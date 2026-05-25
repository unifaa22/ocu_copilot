package com.example.diagnoseillusion.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeamNameRequest {

    @NotBlank(message = "团队名称不能为空")
    private String teamName;
}
