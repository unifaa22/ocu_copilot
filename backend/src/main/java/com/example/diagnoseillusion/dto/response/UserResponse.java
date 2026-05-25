package com.example.diagnoseillusion.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class UserResponse {

    private Long id;
    private String username;
    private String avatar;
    private String avatarUrl;
    private List<String> roles;
}
