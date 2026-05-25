package com.example.diagnoseillusion.entity;

import com.example.diagnoseillusion.enums.DeletedFlag;
import com.example.diagnoseillusion.enums.SyncStatus;
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
@Table(name = "knowledge_file", schema = "knowledge_workbench")
public class KnowledgeFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_type", nullable = false, length = 10)
    private String fileType;

    @Column(name = "file_path", nullable = false, length = 512)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private FileCategory category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private SysUser user;

    @ColumnDefault("0")
    @Column(name = "sync_status", nullable = false)
    private SyncStatus syncStatus = SyncStatus.UNSYNCED;

    @Column(name = "dify_document_id", length = 128)
    private String difyDocumentId;

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