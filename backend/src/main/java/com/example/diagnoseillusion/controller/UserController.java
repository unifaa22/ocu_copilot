package com.example.diagnoseillusion.controller;

import com.example.diagnoseillusion.common.Result;
import com.example.diagnoseillusion.dto.request.ChangePasswordRequest;
import com.example.diagnoseillusion.dto.response.AvatarResponse;
import com.example.diagnoseillusion.dto.response.UserResponse;
import com.example.diagnoseillusion.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public Result<UserResponse> profile() {
        return Result.success(userService.getProfile());
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return Result.success("密码修改成功", null);
    }

    @PostMapping("/avatar")
    public Result<AvatarResponse> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return Result.success(userService.uploadAvatar(file));
    }
}
