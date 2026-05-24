-- =============================================================================
-- 学生 AI 知识工作台 — 数据库初始化脚本
-- 版本: v1.1 (Review 后修订)
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- =============================================================================

CREATE DATABASE IF NOT EXISTS knowledge_workbench
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE knowledge_workbench;

-- -----------------------------------------------------------------------------
-- 1. 用户表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username    VARCHAR(50)     NOT NULL COMMENT '用户名，唯一(仅未删除记录)',
    password    VARCHAR(100)    NOT NULL COMMENT 'BCrypt加密密码',
    avatar      VARCHAR(512)    NULL     COMMENT '头像MinIO object key',
    is_deleted  TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0否 1是',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -----------------------------------------------------------------------------
-- 2. 角色表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS role (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_name   VARCHAR(50)     NOT NULL COMMENT '角色编码: USER/TEAM_CREATOR',
    is_deleted  TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0否 1是(系统角色固定为0)',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- -----------------------------------------------------------------------------
-- 3. 用户角色关联表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_role (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    role_id     BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    is_deleted  TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0否 1是',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_role_user_id (user_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- -----------------------------------------------------------------------------
-- 4. 知识库分类表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS file_category (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    category_name   VARCHAR(100)    NOT NULL COMMENT '知识库分类名称',
    user_id         BIGINT UNSIGNED NOT NULL COMMENT '所属用户ID',
    dify_dataset_id VARCHAR(128)    NULL     COMMENT 'Dify Dataset ID',
    is_deleted      TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0否 1是',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_file_category_user_id (user_id),
    UNIQUE KEY uk_file_category_user_name (user_id, category_name),
    CONSTRAINT fk_file_category_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库分类表';

-- -----------------------------------------------------------------------------
-- 5. 文件表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS knowledge_file (
    id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    file_name        VARCHAR(255)    NOT NULL COMMENT '文件名',
    file_type        VARCHAR(10)     NOT NULL COMMENT '文件类型: md/pdf/doc/docx',
    file_path        VARCHAR(512)    NOT NULL COMMENT 'MinIO object key',
    file_size        BIGINT UNSIGNED NULL     COMMENT '文件大小(字节)',
    category_id      BIGINT UNSIGNED NOT NULL COMMENT '所属分类ID',
    user_id          BIGINT UNSIGNED NOT NULL COMMENT '所属用户ID(冗余)',
    sync_status      TINYINT         NOT NULL DEFAULT 0 COMMENT '0未同步 1成功 2失败',
    dify_document_id VARCHAR(128)    NULL     COMMENT 'Dify Document ID',
    is_deleted       TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0否 1是',
    create_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    update_time      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_knowledge_file_category_id (category_id),
    KEY idx_knowledge_file_user_id (user_id),
    KEY idx_knowledge_file_sync_status (category_id, sync_status),
    CONSTRAINT fk_knowledge_file_category FOREIGN KEY (category_id) REFERENCES file_category (id),
    CONSTRAINT fk_knowledge_file_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库文件表';

-- -----------------------------------------------------------------------------
-- 6. 笔记表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS note (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
    title       VARCHAR(200)    NOT NULL COMMENT '笔记标题',
    content     LONGTEXT        NOT NULL COMMENT 'Markdown内容',
    tags        JSON            NULL     COMMENT '多标签JSON数组',
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '所属用户ID',
    is_deleted  TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0否 1是',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_note_user_id (user_id),
    CONSTRAINT fk_note_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记表';

-- -----------------------------------------------------------------------------
-- 7. 团队表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS team (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '团队ID',
    team_name   VARCHAR(100)    NOT NULL COMMENT '团队名称',
    creator_id  BIGINT UNSIGNED NOT NULL COMMENT '创建者用户ID',
    is_share    TINYINT         NOT NULL DEFAULT 0 COMMENT '0关闭 1开启知识库共享',
    is_deleted  TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0否 1是',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_team_creator_id (creator_id),
    CONSTRAINT fk_team_creator FOREIGN KEY (creator_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队表';

-- -----------------------------------------------------------------------------
-- 8. 团队成员表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS team_member (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    team_id     BIGINT UNSIGNED NOT NULL COMMENT '团队ID',
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    member_role TINYINT         NOT NULL DEFAULT 0 COMMENT '0普通成员 1创建者',
    status      TINYINT         NOT NULL DEFAULT 0 COMMENT '0待接受 1已加入 2已拒绝',
    is_deleted  TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0否 1是',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建/邀请时间',
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_team_member (team_id, user_id),
    KEY idx_team_member_user_status (user_id, status),
    KEY idx_team_member_team_status (team_id, status),
    CONSTRAINT fk_team_member_team FOREIGN KEY (team_id) REFERENCES team (id),
    CONSTRAINT fk_team_member_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队成员表';

-- -----------------------------------------------------------------------------
-- 9. 问答历史表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_history (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    conversation_id VARCHAR(64)     NOT NULL COMMENT 'Dify会话ID',
    question        TEXT            NOT NULL COMMENT '用户问题',
    answer          LONGTEXT        NOT NULL COMMENT 'AI回答',
    category_ids    JSON            NOT NULL COMMENT '关联分类ID列表',
    category_names  JSON            NULL     COMMENT '分类名称快照',
    team_id         BIGINT UNSIGNED NULL     COMMENT '团队问答时的团队ID',
    user_id         BIGINT UNSIGNED NOT NULL COMMENT '提问用户ID',
    is_deleted      TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0否 1是',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提问时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_chat_history_user_id (user_id, create_time),
    KEY idx_chat_history_conversation (conversation_id, create_time),
    KEY idx_chat_history_team_id (team_id),
    CONSTRAINT fk_chat_history_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_chat_history_team FOREIGN KEY (team_id) REFERENCES team (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问答历史表';

-- -----------------------------------------------------------------------------
-- 初始化角色数据
-- -----------------------------------------------------------------------------
INSERT INTO role (id, role_name) VALUES
    (1, 'USER'),
    (2, 'TEAM_CREATOR')
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name);
