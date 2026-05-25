package com.example.diagnoseillusion.service;

import com.example.diagnoseillusion.common.CustomException;
import com.example.diagnoseillusion.common.PageUtils;
import com.example.diagnoseillusion.dto.common.PageResult;
import com.example.diagnoseillusion.dto.request.InviteRequest;
import com.example.diagnoseillusion.dto.request.ShareRequest;
import com.example.diagnoseillusion.dto.request.TeamNameRequest;
import com.example.diagnoseillusion.dto.response.*;
import com.example.diagnoseillusion.entity.FileCategory;
import com.example.diagnoseillusion.entity.SysUser;
import com.example.diagnoseillusion.entity.Team;
import com.example.diagnoseillusion.entity.TeamMember;
import com.example.diagnoseillusion.enums.DeletedFlag;
import com.example.diagnoseillusion.enums.MemberRole;
import com.example.diagnoseillusion.enums.ShareStatus;
import com.example.diagnoseillusion.enums.TeamMemberStatus;
import com.example.diagnoseillusion.repository.FileCategoryRepository;
import com.example.diagnoseillusion.repository.TeamMemberRepository;
import com.example.diagnoseillusion.repository.TeamRepository;
import com.example.diagnoseillusion.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserService userService;
    private final UserRoleHelper userRoleHelper;
    private final CategoryService categoryService;
    private final FileService fileService;
    private final FileCategoryRepository fileCategoryRepository;

    @Transactional
    public TeamResponse create(TeamNameRequest request) {
        SysUser creator = userService.requireCurrentUser();
        String name = request.getTeamName().trim();
        Team team = new Team();
        team.setTeamName(name);
        team.setCreator(creator);
        team.setIsShare(ShareStatus.DISABLED);
        team.setIsDeleted(DeletedFlag.NOT_DELETED);
        team = teamRepository.save(team);

        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUser(creator);
        member.setMemberRole(MemberRole.CREATOR);
        member.setStatus(TeamMemberStatus.JOINED);
        member.setIsDeleted(DeletedFlag.NOT_DELETED);
        teamMemberRepository.save(member);

        userRoleHelper.assignTeamCreatorRole(creator);
        return toTeamResponse(team, creator.getUsername());
    }

    public PageResult<TeamResponse> listManaged(Integer page, Integer size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageUtils.pageable(page, size);
        Page<Team> teams = teamRepository.findByCreator_IdAndIsDeleted(userId, DeletedFlag.NOT_DELETED, pageable);
        return PageUtils.toPageResult(teams.map(t -> toTeamResponse(t, t.getCreator().getUsername())));
    }

    public PageResult<TeamJoinedResponse> listJoined(Integer page, Integer size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageUtils.pageable(page, size);
        Page<TeamMember> members = teamMemberRepository.findJoinedTeams(
                userId, TeamMemberStatus.JOINED, DeletedFlag.NOT_DELETED, pageable);
        return PageUtils.toPageResult(members.map(this::toJoinedResponse));
    }

    public TeamDetailResponse getDetail(Long id) {
        Team team = requireActiveTeam(id);
        TeamMember member = requireMembership(team, SecurityUtils.getCurrentUserId());
        if (member.getStatus() != TeamMemberStatus.JOINED && !team.getCreator().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new CustomException(403, "无权限");
        }
        TeamDetailResponse detail = new TeamDetailResponse();
        detail.setId(team.getId());
        detail.setTeamName(team.getTeamName());
        detail.setCreatorId(team.getCreator().getId());
        detail.setCreatorName(team.getCreator().getUsername());
        detail.setIsShare(team.getIsShare().getValue());
        detail.setMyMemberRole(member.getMemberRole().getValue());
        detail.setMyStatus(member.getStatus().getValue());
        detail.setMemberCount(teamMemberRepository.countByTeam_IdAndStatusAndIsDeleted(
                team.getId(), TeamMemberStatus.JOINED, DeletedFlag.NOT_DELETED));
        detail.setCreateTime(team.getCreateTime());
        return detail;
    }

    @Transactional
    public void dissolve(Long id) {
        Team team = requireActiveTeam(id);
        if (!team.getCreator().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new CustomException(403, "仅创建者可解散团队");
        }
        team.setIsDeleted(DeletedFlag.DELETED);
        teamRepository.save(team);
        teamMemberRepository.findByTeam_IdAndIsDeleted(id, DeletedFlag.NOT_DELETED)
                .forEach(m -> {
                    m.setIsDeleted(DeletedFlag.DELETED);
                    teamMemberRepository.save(m);
                });
    }

    @Transactional
    public void updateShare(Long id, ShareRequest request) {
        Team team = requireActiveTeam(id);
        if (!team.getCreator().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new CustomException(403, "仅创建者可操作");
        }
        team.setIsShare(request.getIsShare() != null && request.getIsShare() == 1
                ? ShareStatus.ENABLED : ShareStatus.DISABLED);
        teamRepository.save(team);
    }

    public PageResult<TeamMemberResponse> listMembers(Long id, Integer page, Integer size) {
        Team team = requireActiveTeam(id);
        requireJoinedMember(team);
        Pageable pageable = PageUtils.pageable(page, size);
        var members = teamMemberRepository.findByTeam_IdAndStatusAndIsDeleted(id, TeamMemberStatus.JOINED, DeletedFlag.NOT_DELETED);
        int p = pageable.getPageNumber() + 1;
        int s = pageable.getPageSize();
        int start = pageable.getPageNumber() * s;
        int end = Math.min(start + s, members.size());
        var slice = start >= members.size() ? java.util.List.<TeamMember>of() : members.subList(start, end);
        var list = slice.stream().map(this::toMemberResponse).toList();
        return PageResult.of(list, members.size(), p, s);
    }

    @Transactional
    public void invite(Long teamId, InviteRequest request) {
        Team team = requireActiveTeam(teamId);
        if (!team.getCreator().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new CustomException(403, "无权限");
        }
        SysUser target = userService.findActiveByUsername(request.getUsername().trim());
        if (target.getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new CustomException(400, "不能邀请自己");
        }
        var existingOpt = teamMemberRepository.findByTeam_IdAndUser_Id(teamId, target.getId());
        if (existingOpt.isPresent()) {
            TeamMember existing = existingOpt.get();
            if (existing.getIsDeleted() == DeletedFlag.DELETED || existing.getStatus() == TeamMemberStatus.REJECTED) {
                existing.setStatus(TeamMemberStatus.PENDING);
                existing.setIsDeleted(DeletedFlag.NOT_DELETED);
                teamMemberRepository.save(existing);
                return;
            }
            if (existing.getStatus() == TeamMemberStatus.PENDING || existing.getStatus() == TeamMemberStatus.JOINED) {
                throw new CustomException(409, "用户已在团队中或待接受");
            }
        }
        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUser(target);
        member.setMemberRole(MemberRole.MEMBER);
        member.setStatus(TeamMemberStatus.PENDING);
        member.setIsDeleted(DeletedFlag.NOT_DELETED);
        teamMemberRepository.save(member);
    }

    public PageResult<PendingInvitationResponse> pendingInvitations(Integer page, Integer size) {
        Pageable pageable = PageUtils.pageable(page, size);
        Page<TeamMember> memberPage = teamMemberRepository.findPendingInvitations(
                SecurityUtils.getCurrentUserId(), TeamMemberStatus.PENDING, DeletedFlag.NOT_DELETED, pageable);
        return PageUtils.toPageResult(memberPage.map(m -> {
            PendingInvitationResponse item = new PendingInvitationResponse();
            item.setTeamId(m.getTeam().getId());
            item.setTeamName(m.getTeam().getTeamName());
            item.setCreatorName(m.getTeam().getCreator().getUsername());
            item.setInviteTime(m.getCreateTime());
            return item;
        }));
    }

    @Transactional
    public void acceptInvite(Long teamId) {
        TeamMember member = teamMemberRepository.findByTeam_IdAndUser_IdAndIsDeleted(
                        teamId, SecurityUtils.getCurrentUserId(), DeletedFlag.NOT_DELETED)
                .orElseThrow(() -> new CustomException(404, "邀请不存在"));
        if (member.getStatus() != TeamMemberStatus.PENDING) {
            throw new CustomException(403, "非待接受状态");
        }
        member.setStatus(TeamMemberStatus.JOINED);
        teamMemberRepository.save(member);
    }

    @Transactional
    public void rejectInvite(Long teamId) {
        TeamMember member = teamMemberRepository.findByTeam_IdAndUser_IdAndIsDeleted(
                        teamId, SecurityUtils.getCurrentUserId(), DeletedFlag.NOT_DELETED)
                .orElseThrow(() -> new CustomException(404, "邀请不存在"));
        if (member.getStatus() != TeamMemberStatus.PENDING) {
            throw new CustomException(403, "非待接受状态");
        }
        member.setStatus(TeamMemberStatus.REJECTED);
        teamMemberRepository.save(member);
    }

    @Transactional
    public void removeMember(Long teamId, Long userId) {
        Team team = requireActiveTeam(teamId);
        if (!team.getCreator().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new CustomException(403, "无权限");
        }
        TeamMember member = teamMemberRepository.findByTeam_IdAndUser_IdAndIsDeleted(teamId, userId, DeletedFlag.NOT_DELETED)
                .orElseThrow(() -> new CustomException(404, "成员不存在"));
        if (member.getMemberRole() == MemberRole.CREATOR) {
            throw new CustomException(400, "不能移除创建者");
        }
        member.setIsDeleted(DeletedFlag.DELETED);
        teamMemberRepository.save(member);
    }

    @Transactional
    public void leave(Long teamId) {
        Team team = requireActiveTeam(teamId);
        TeamMember member = teamMemberRepository.findByTeam_IdAndUser_IdAndIsDeleted(
                        teamId, SecurityUtils.getCurrentUserId(), DeletedFlag.NOT_DELETED)
                .orElseThrow(() -> new CustomException(404, "不在团队中"));
        if (member.getMemberRole() == MemberRole.CREATOR) {
            throw new CustomException(400, "创建者不能退出，请解散团队");
        }
        member.setIsDeleted(DeletedFlag.DELETED);
        teamMemberRepository.save(member);
    }

    public java.util.List<CategoryResponse> sharedCategories(Long teamId) {
        Team team = requireSharedTeamAccess(teamId);
        return categoryService.listByCreatorId(team.getCreator().getId());
    }

    public PageResult<FileResponse> sharedFiles(Long teamId, Long categoryId, Integer page, Integer size) {
        Team team = requireSharedTeamAccess(teamId);
        FileCategory category = fileCategoryRepository.findByIdAndUser_IdAndIsDeleted(
                        categoryId, team.getCreator().getId(), DeletedFlag.NOT_DELETED)
                .orElseThrow(() -> new CustomException(404, "分类不存在"));
        return fileService.listSharedFiles(category.getId(), page, size);
    }

    public Team requireSharedTeamAccess(Long teamId) {
        Team team = requireActiveTeam(teamId);
        if (team.getIsShare() != ShareStatus.ENABLED) {
            throw new CustomException(403, "团队未开启共享");
        }
        TeamMember member = teamMemberRepository.findByTeam_IdAndUser_IdAndIsDeleted(
                        teamId, SecurityUtils.getCurrentUserId(), DeletedFlag.NOT_DELETED)
                .orElseThrow(() -> new CustomException(403, "非团队成员"));
        if (member.getStatus() != TeamMemberStatus.JOINED) {
            throw new CustomException(403, "非团队成员");
        }
        return team;
    }

    private Team requireActiveTeam(Long id) {
        return teamRepository.findByIdAndIsDeleted(id, DeletedFlag.NOT_DELETED)
                .orElseThrow(() -> new CustomException(404, "团队不存在"));
    }

    private TeamMember requireMembership(Team team, Long userId) {
        return teamMemberRepository.findByTeam_IdAndUser_IdAndIsDeleted(team.getId(), userId, DeletedFlag.NOT_DELETED)
                .orElseThrow(() -> new CustomException(403, "无权限"));
    }

    private void requireJoinedMember(Team team) {
        TeamMember member = requireMembership(team, SecurityUtils.getCurrentUserId());
        if (member.getStatus() != TeamMemberStatus.JOINED && !team.getCreator().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new CustomException(403, "无权限");
        }
    }

    private TeamResponse toTeamResponse(Team team, String creatorName) {
        TeamResponse response = new TeamResponse();
        response.setId(team.getId());
        response.setTeamName(team.getTeamName());
        response.setCreatorId(team.getCreator().getId());
        response.setCreatorName(creatorName);
        response.setIsShare(team.getIsShare().getValue());
        response.setCreateTime(team.getCreateTime());
        return response;
    }

    private TeamJoinedResponse toJoinedResponse(TeamMember member) {
        Team team = member.getTeam();
        TeamJoinedResponse response = new TeamJoinedResponse();
        response.setId(team.getId());
        response.setTeamName(team.getTeamName());
        response.setCreatorId(team.getCreator().getId());
        response.setCreatorName(team.getCreator().getUsername());
        response.setIsShare(team.getIsShare().getValue());
        response.setIsCreator(team.getCreator().getId().equals(member.getUser().getId()));
        response.setJoinTime(member.getCreateTime());
        return response;
    }

    private TeamMemberResponse toMemberResponse(TeamMember member) {
        TeamMemberResponse response = new TeamMemberResponse();
        response.setId(member.getId());
        response.setUserId(member.getUser().getId());
        response.setUsername(member.getUser().getUsername());
        response.setMemberRole(member.getMemberRole().getValue());
        response.setStatus(member.getStatus().getValue());
        response.setJoinTime(member.getCreateTime());
        return response;
    }
}
