package com.example.diagnoseillusion.entity;

import com.example.diagnoseillusion.enums.DeletedFlag;
import com.example.diagnoseillusion.enums.ShareStatus;
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
@Table(name = "team", schema = "knowledge_workbench")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "team_name", nullable = false, length = 100)
    private String teamName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private SysUser creator;

    @ColumnDefault("0")
    @Column(name = "is_share", nullable = false)
    private ShareStatus isShare = ShareStatus.DISABLED;

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