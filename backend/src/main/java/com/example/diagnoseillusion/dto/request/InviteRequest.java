package com.example.diagnoseillusion.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InviteRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;
}
