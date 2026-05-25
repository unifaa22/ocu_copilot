package com.example.diagnoseillusion.controller;

import com.example.diagnoseillusion.common.Result;
import com.example.diagnoseillusion.dto.common.PageResult;
import com.example.diagnoseillusion.dto.request.InviteRequest;
import com.example.diagnoseillusion.dto.request.ShareRequest;
import com.example.diagnoseillusion.dto.request.TeamNameRequest;
import com.example.diagnoseillusion.dto.response.*;
import com.example.diagnoseillusion.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public Result<TeamResponse> create(@Valid @RequestBody TeamNameRequest request) {
        return Result.success(teamService.create(request));
    }

    @GetMapping("/managed")
    public Result<PageResult<TeamResponse>> managed(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return Result.success(teamService.listManaged(page, size));
    }

    @GetMapping("/joined")
    public Result<PageResult<TeamJoinedResponse>> joined(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return Result.success(teamService.listJoined(page, size));
    }

    @GetMapping("/invitations/pending")
    public Result<PageResult<PendingInvitationResponse>> pendingInvitations(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return Result.success(teamService.pendingInvitations(page, size));
    }

    @GetMapping("/{id}")
    public Result<TeamDetailResponse> detail(@PathVariable Long id) {
        return Result.success(teamService.getDetail(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> dissolve(@PathVariable Long id) {
        teamService.dissolve(id);
        return Result.success();
    }

    @PutMapping("/{id}/share")
    public Result<Void> share(@PathVariable Long id, @RequestBody ShareRequest request) {
        teamService.updateShare(id, request);
        return Result.success();
    }

    @GetMapping("/{id}/members")
    public Result<PageResult<TeamMemberResponse>> members(
            @PathVariable Long id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return Result.success(teamService.listMembers(id, page, size));
    }

    @PostMapping("/{id}/invite")
    public Result<Void> invite(@PathVariable Long id, @Valid @RequestBody InviteRequest request) {
        teamService.invite(id, request);
        return Result.success("邀请已发送", null);
    }

    @PostMapping("/{id}/invite/accept")
    public Result<Void> accept(@PathVariable Long id) {
        teamService.acceptInvite(id);
        return Result.success();
    }

    @PostMapping("/{id}/invite/reject")
    public Result<Void> reject(@PathVariable Long id) {
        teamService.rejectInvite(id);
        return Result.success();
    }

    @DeleteMapping("/{id}/members/{userId}")
    public Result<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        teamService.removeMember(id, userId);
        return Result.success();
    }

    @PostMapping("/{id}/leave")
    public Result<Void> leave(@PathVariable Long id) {
        teamService.leave(id);
        return Result.success();
    }

    @GetMapping("/{teamId}/categories")
    public Result<List<CategoryResponse>> sharedCategories(@PathVariable Long teamId) {
        return Result.success(teamService.sharedCategories(teamId));
    }

    @GetMapping("/{teamId}/categories/{categoryId}/files")
    public Result<PageResult<FileResponse>> sharedFiles(
            @PathVariable Long teamId,
            @PathVariable Long categoryId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return Result.success(teamService.sharedFiles(teamId, categoryId, page, size));
    }
}
