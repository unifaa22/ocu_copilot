package com.example.diagnoseillusion.entity;

import com.example.diagnoseillusion.enums.DeletedFlag;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "chat_history", schema = "knowledge_workbench")
public class ChatHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "conversation_id", nullable = false, length = 64)
    private String conversationId;

    @Lob
    @Column(name = "question", nullable = false)
    private String question;

    @Lob
    @Column(name = "answer", nullable = false)
    private String answer;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "category_ids", nullable = false)
    private List<Long> categoryIds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "category_names")
    private List<String> categoryNames;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private SysUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

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