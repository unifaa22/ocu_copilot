package com.example.diagnoseillusion.entity;

import com.example.diagnoseillusion.enums.DeletedFlag;
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
@Table(name = "file_category", schema = "knowledge_workbench",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category_name"}))
public class FileCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "dify_dataset_id", length = 128)
    private String difyDatasetId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private SysUser user;

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