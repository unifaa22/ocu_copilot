package com.example.diagnoseillusion.entity;

import com.example.diagnoseillusion.enums.DeletedFlag;
import com.example.diagnoseillusion.enums.MemberRole;
import com.example.diagnoseillusion.enums.TeamMemberStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "team_member", schema = "knowledge_workbench",
       uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "user_id"}))
public class TeamMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private SysUser user;

    @ColumnDefault("0")
    @Column(name = "member_role", nullable = false)
    private MemberRole memberRole = MemberRole.MEMBER;

    @ColumnDefault("0")
    @Column(name = "status", nullable = false)
    private TeamMemberStatus status = TeamMemberStatus.PENDING;

    @ColumnDefault("0")
    @Column(name = "is_deleted", nullable = false)
    private DeletedFlag isDeleted = DeletedFlag.NOT_DELETED;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;


}