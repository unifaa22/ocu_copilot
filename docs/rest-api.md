# 学生 AI 知识工作台 — REST API 接口文档

> **版本**：v1.1（Review 后修订）  
> **依据**：需求说明书 v1.2、数据库设计 v1.1  
> **基础路径**：`/api`  
> **状态**：已确认

---

## 1 通用约定

### 1.1 请求规范

| 项 | 约定 |
|----|------|
| 协议 | HTTP/HTTPS |
| 数据格式 | `application/json`（文件上传除外） |
| 字符编码 | UTF-8 |
| 鉴权 Header | `Authorization: Bearer <accessToken>` |
| 时间格式 | `yyyy-MM-dd HH:mm:ss`（服务端返回字符串） |

### 1.2 文件大小限制

| 类型 | 上限 | 说明 |
|------|------|------|
| 学习文件（pdf/doc/docx/md） | **与 Dify 保持一致** | Dify 官方默认 **15MB/文件**；自建 Dify 可通过环境变量 `UPLOAD_FILE_SIZE_LIMIT`（单位 MB）调整 |
| 头像 | **2MB** | 仅 jpg / png |

**后端配置建议**（`application.yml`）：

```yaml
dify:
  upload:
    max-file-size-mb: 15   # 须与 Dify 实例 UPLOAD_FILE_SIZE_LIMIT 一致

upload:
  avatar-max-size-mb: 2
```

- 本系统上传学习文件时，按 `dify.upload.max-file-size-mb` 校验；超限返回 400
- 同步 Dify 前若文件超过 Dify 限制，同步将失败（`syncStatus=2`），故**上传阶段即拦截**
- 若调大 Dify 限制，须同步修改本系统配置及 Nginx `client_max_body_size`（若经 Nginx 转发）

> **参考**：[Dify 文档 — Upload Local Files](https://docs.dify.ai/en/use-dify/knowledge/create-knowledge/import-text-data/readme)（默认单文件 15MB）

### 1.3 统一响应结构

与后端 `Result<T>` 保持一致：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

| code | 含义 | 典型场景 |
|------|------|----------|
| 200 | 成功 | 正常业务 |
| 400 | 请求参数错误 | 校验失败、格式错误 |
| 401 | 未认证 | Token 缺失、过期、无效 |
| 403 | 无权限 | 非创建者操作团队、非成员访问共享库等 |
| 404 | 资源不存在 | ID 无效或已逻辑删除 |
| 409 | 冲突 | 用户名重复、分类名重复、重复邀请等 |
| 500 | 服务器错误 | 未预期异常、Dify/MinIO 调用失败等 |

### 1.4 分页约定

列表接口统一 Query 参数：

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| page | int | 否 | 1 | 页码，从 1 开始 |
| size | int | 否 | 10 | 每页条数，最大 100 |

分页响应 `data` 结构：

```json
{
  "list": [],
  "total": 0,
  "page": 1,
  "size": 10
}
```

### 1.5 逻辑删除约定

- 所有 **DELETE** 接口均为**逻辑删除**（`is_deleted = 1`）
- 列表/详情接口默认只返回 `is_deleted = 0` 的数据
- 响应中**不返回** `is_deleted`、`password` 等内部字段

### 1.6 枚举说明

**文件类型 fileType**：`md` | `pdf` | `doc` | `docx`

**同步状态 syncStatus**：`0` 未同步 | `1` 成功 | `2` 失败

**团队成员 status**：`0` 待接受 | `1` 已加入 | `2` 已拒绝

**成员角色 memberRole**：`0` 普通成员 | `1` 创建者

**角色 roles（展示用）**：`USER` 普通用户 | `TEAM_CREATOR` 团队创建者

**共享开关 isShare**：`0` 关闭 | `1` 开启

**主题 theme（仅前端）**：`light` | `dark` | `system` — 存 `localStorage`，不入库，见 §3.0

---

## 2 认证模块

> 以下接口**无需** JWT。

### 2.1 用户注册

**POST** `/api/auth/register`

**请求体**

```json
{
  "username": "zhangsan",
  "password": "123456",
  "confirmPassword": "123456"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 唯一，非空 |
| password | string | 是 | 长度 ≥ 6 |
| confirmPassword | string | 是 | 须与 password 一致 |

**成功响应 data**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": 1,
    "username": "zhangsan",
    "avatar": null,
    "roles": ["USER"]
  }
}
```

**错误**

| code | message 示例 |
|------|----------------|
| 409 | 用户名已存在 |
| 400 | 两次密码不一致 |

---

### 2.2 用户登录

**POST** `/api/auth/login`

**请求体**

```json
{
  "username": "zhangsan",
  "password": "123456"
}
```

**成功响应 data**：同注册（含 `accessToken` 与 `user`）

**错误**

| code | message 示例 |
|------|----------------|
| 400 | 用户名或密码错误 |
| 401 | 用户名或密码错误 |

> **【说明】** 登录失败对外统一提示「用户名或密码错误」，不暴露用户是否存在。

---

### 2.3 登出

前端删除本地 Token 即可，**无后端接口**。

---

## 3 个人中心模块

> 以下接口均需 JWT。

### 3.0 主题切换（纯前端，无后端接口）

明暗主题（`light` / `dark` / `system`）为 UI 偏好，**不写入数据库**。

| 项 | 约定 |
|----|------|
| 存储位置 | 浏览器 `localStorage` |
| 推荐 key | `app-theme` |
| 默认值 | `system`（跟随系统） |
| 切换时机 | 用户在前端设置页切换后立即生效并持久化 |

前端可在应用启动时读取 `localStorage`，配合 CSS 变量或主题插件（如 VueUse `useDark`）实现。

---

### 3.1 获取当前用户信息

**GET** `/api/user/profile`

**成功响应 data**

```json
{
  "id": 1,
  "username": "zhangsan",
  "avatar": "avatars/1/xxx.jpg",
  "avatarUrl": "http://minio.example/presigned-url...",
  "roles": ["USER", "TEAM_CREATOR"]
}
```

| 字段 | 说明 |
|------|------|
| avatar | MinIO object key |
| avatarUrl | 可选，后端生成的临时访问 URL |
| roles | 英文 code 列表，前端映射中文 |

---

### 3.2 修改密码

**PUT** `/api/user/password`

**请求体**

```json
{
  "oldPassword": "123456",
  "newPassword": "654321",
  "confirmPassword": "654321"
}
```

**成功响应**：`data` 为 `null`，`message` 为「密码修改成功」

**错误**：400 旧密码错误 / 两次新密码不一致

---

### 3.3 上传头像

**POST** `/api/user/avatar`

**Content-Type**：`multipart/form-data`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 图片 jpg/png，≤ 2MB |

**成功响应 data**

```json
{
  "avatar": "avatars/1/uuid.jpg",
  "avatarUrl": "http://minio.example/presigned-url..."
}
```

---

## 4 知识库分类模块

### 4.1 我的分类列表

**GET** `/api/categories`

**Query**：无（分类数量通常较少，不分页）

**成功响应 data**

```json
[
  {
    "id": 1,
    "categoryName": "Java学习",
    "fileCount": 5,
    "syncedCount": 3,
    "createTime": "2026-05-24 10:00:00",
    "updateTime": "2026-05-24 10:00:00"
  }
]
```

| 字段 | 说明 |
|------|------|
| fileCount | 该分类下未删除文件总数（可选统计字段） |
| syncedCount | syncStatus=1 的文件数（可选统计字段） |

---

### 4.2 创建分类

**POST** `/api/categories`

**请求体**

```json
{
  "categoryName": "Java学习"
}
```

**成功响应 data**：单条分类对象（含 `id`）

**错误**：409 同一用户下分类名已存在

---

### 4.3 重命名分类

**PUT** `/api/categories/{id}`

**请求体**

```json
{
  "categoryName": "Java进阶"
}
```

**权限**：仅分类所属用户

---

### 4.4 删除分类

**DELETE** `/api/categories/{id}`

**权限**：仅分类所属用户

**业务说明**：

- 逻辑删除分类及其下属全部文件（`knowledge_file.is_deleted = 1`）
- 业务层同步清理 MinIO 对象、Dify Dataset/Documents（异步或同步均可，接口文档约定须最终一致）
- 删除时 `category_name` 加后缀 `__del_{id}` 释放唯一约束

---

## 5 文件模块

### 5.1 分类下文件列表

**GET** `/api/categories/{categoryId}/files`

**Query**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 分页 |
| size | int | 否 | 分页 |
| syncStatus | int | 否 | 按同步状态筛选 |

**成功响应 data**：分页结构，`list` 元素示例：

```json
{
  "id": 10,
  "fileName": "笔记.pdf",
  "fileType": "pdf",
  "fileSize": 102400,
  "categoryId": 1,
  "syncStatus": 1,
  "createTime": "2026-05-24 11:00:00",
  "updateTime": "2026-05-24 11:00:00"
}
```

---

### 5.2 我的全部文件（可选）

**GET** `/api/files`

**Query**：`page`、`size`、`syncStatus`（可选）

**说明**：跨分类汇总当前用户全部文件，依赖 `knowledge_file.user_id` 冗余字段。

---

### 5.3 上传文件

**POST** `/api/categories/{categoryId}/files`

**Content-Type**：`multipart/form-data`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | md / pdf / doc / docx；大小 ≤ Dify 限制（默认 15MB） |

**成功响应 data**：文件对象（`syncStatus` 默认为 `0`）

**错误**

| code | 说明 |
|------|------|
| 400 | 文件类型不支持 / 文件为空 / **超过 Dify 文件大小上限** |
| 403 | 非分类所属用户 |
| 404 | 分类不存在 |

> **【说明】** 上传后不自动同步 Dify，需调用 §7.1 同步接口。文件大小上限须与 Dify `UPLOAD_FILE_SIZE_LIMIT` 保持一致（见 §1.2）。

---

### 5.4 重命名文件

**PUT** `/api/files/{id}`

**请求体**

```json
{
  "fileName": "新文件名.pdf"
}
```

**权限**：仅文件所属用户

---

### 5.5 删除文件

**DELETE** `/api/files/{id}`

**权限**：仅文件所属用户

**业务说明**：逻辑删除；若已同步 Dify，须调用 Dify 删除 Document；删除 MinIO 对象。

---

### 5.6 获取文件预览/下载 URL（个人 + 团队共享共用）

**GET** `/api/files/{fileId}/preview-url`

**成功响应 data**

```json
{
  "previewUrl": "http://minio.example/presigned-url...",
  "expireSeconds": 3600
}
```

**权限**（后端统一鉴权，**个人与团队场景共用本接口**）：

| 场景 | 条件 |
|------|------|
| 个人文件 | 当前用户为文件 `user_id` 所属者 |
| 团队共享文件 | 当前用户为团队成员（`status=1`）且团队 `isShare=1`，且文件属于该团队 `creator_id` |

**错误**：403 无权限 / 404 文件不存在

## 6 笔记模块

### 6.1 笔记列表

**GET** `/api/notes`

**Query**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 分页 |
| size | int | 否 | 分页 |
| keyword | string | 否 | 标题模糊搜索 |
| tag | string | 否 | 标签模糊匹配 |

**成功响应 data**：分页结构，`list` 元素示例：

```json
{
  "id": 1,
  "title": "SpringBoot笔记",
  "tags": ["Java", "期末"],
  "createTime": "2026-05-24 12:00:00",
  "updateTime": "2026-05-24 12:00:00"
}
```

> 列表不返回 `content`，减少 payload。

---

### 6.2 笔记详情

**GET** `/api/notes/{id}`

**成功响应 data**：含完整 `content`（Markdown 文本）

---

### 6.3 创建笔记

**POST** `/api/notes`

**请求体**

```json
{
  "title": "SpringBoot笔记",
  "content": "# 标题\n正文...",
  "tags": ["Java", "期末"]
}
```

---

### 6.4 更新笔记

**PUT** `/api/notes/{id}`

**请求体**：同创建，`tags` 传完整数组覆盖。

---

### 6.5 删除笔记

**DELETE** `/api/notes/{id}`

逻辑删除，仅笔记所属用户可操作。

---

## 7 AI 同步与问答模块

### 7.1 同步知识库（按单分类）

**POST** `/api/categories/{categoryId}/sync`

**请求体**：无

**权限**：仅分类所属用户

**业务说明**：

- 同步该分类下全部 `syncStatus` 为 `0` 或 `2` 的文件
- 首次同步时创建 Dify Dataset 并回填 `dify_dataset_id`
- 逐文件上传 Dify Document，更新 `syncStatus` 与 `dify_document_id`
- **同步方式**：同步阻塞，请求完成后返回成功/失败统计（见 Review 确认 §12）

**成功响应 data**

```json
{
  "categoryId": 1,
  "total": 5,
  "successCount": 4,
  "failCount": 1,
  "failedFiles": [
    {
      "fileId": 12,
      "fileName": "损坏.pdf",
      "syncStatus": 2,
      "errorMessage": "Dify 解析失败"
    }
  ]
}
```

---

### 7.2 个人智能问答

**POST** `/api/chat/personal`

**请求体**

```json
{
  "question": "什么是 Spring IOC？",
  "categoryIds": [1, 3],
  "conversationId": "optional-dify-conversation-id"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| question | string | 是 | 用户问题 |
| categoryIds | long[] | 是 | 至少选一个，须为当前用户所属分类 |
| conversationId | string | 否 | 首轮不传；续聊传上一轮返回的 ID |

**成功响应 data**

```json
{
  "conversationId": "dify-conv-uuid",
  "answer": "Spring IOC 是控制反转...",
  "historyId": 100,
  "categoryIds": [1, 3],
  "categoryNames": ["Java学习", "算法"]
}
```

**业务说明**：

- 仅匹配所选分类下 `syncStatus=1` 的文档
- 每轮问答写入一条 `chat_history`，`teamId` 为空

---

### 7.3 团队智能问答

**POST** `/api/chat/team`

**请求体**

```json
{
  "question": "期末复习重点是什么？",
  "teamId": 2,
  "conversationId": "optional-dify-conversation-id"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| question | string | 是 | 用户问题 |
| teamId | long | 是 | 团队 ID |
| conversationId | string | 否 | 多轮续聊 |

**权限校验**：

- 当前用户为团队成员且 `status=1`
- 团队 `isShare=1` 且未解散

**业务说明**：

- **不传 categoryIds**；后端自动使用创建者名下全部已成功同步分类的 `dify_dataset_id`
- `chat_history.categoryIds` 写入创建者全部分类 ID 快照

**成功响应 data**：结构同个人问答，额外可含 `teamId`、`teamName`

---

## 8 问答历史模块

### 8.1 会话列表（聚合）

**GET** `/api/chat/conversations`

**Query**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 分页 |
| size | int | 否 | 分页 |
| type | string | 否 | `personal` / `team`，不传则全部 |

**成功响应 data**：分页结构，`list` 元素示例：

```json
{
  "conversationId": "dify-conv-uuid",
  "type": "personal",
  "teamId": null,
  "teamName": null,
  "lastQuestion": "什么是 IOC？",
  "lastAnswer": "IOC 是...",
  "messageCount": 3,
  "categoryNames": ["Java学习"],
  "lastTime": "2026-05-24 15:00:00"
}
```

> 按 `conversation_id` 分组，取每组最新一条作为会话摘要。

---

### 8.2 会话详情（多轮记录）

**GET** `/api/chat/conversations/{conversationId}`

**成功响应 data**

```json
{
  "conversationId": "dify-conv-uuid",
  "type": "personal",
  "teamId": null,
  "messages": [
    {
      "id": 100,
      "question": "什么是 IOC？",
      "answer": "IOC 是...",
      "categoryIds": [1],
      "categoryNames": ["Java学习"],
      "createTime": "2026-05-24 15:00:00"
    },
    {
      "id": 101,
      "question": "举个例子",
      "answer": "例如...",
      "categoryIds": [1],
      "categoryNames": ["Java学习"],
      "createTime": "2026-05-24 15:01:00"
    }
  ]
}
```

**权限**：仅会话所属用户（`chat_history.user_id`）

---

### 8.3 删除单条问答记录

**DELETE** `/api/chat/history/{id}`

逻辑删除单条 `chat_history`。

---

### 8.4 删除整个会话

**DELETE** `/api/chat/conversations/{conversationId}`

逻辑删除该 `conversationId` 下当前用户的全部记录。

---

### 8.5 清空全部问答历史

**DELETE** `/api/chat/history`

逻辑删除当前用户全部 `chat_history`。

---

## 9 团队模块

### 9.1 创建团队

**POST** `/api/teams`

**请求体**

```json
{
  "teamName": "期末复习小组"
}
```

**业务说明**：

- 插入 `team`，`creator_id` 为当前用户
- 插入 `team_member`（创建者，`memberRole=1`，`status=1`）
- 若用户尚无 `TEAM_CREATOR` 角色，追加 `user_role`

**成功响应 data**

```json
{
  "id": 2,
  "teamName": "期末复习小组",
  "creatorId": 1,
  "creatorName": "zhangsan",
  "isShare": 0,
  "createTime": "2026-05-24 16:00:00"
}
```

---

### 9.2 我创建的团队

**GET** `/api/teams/managed`

**Query**：`page`、`size`

**筛选条件**：`team.creator_id = 当前用户` 且 `team.is_deleted = 0`

**前端用途**：独立页面展示「我创建的团队」，用于管理、邀请、开关共享、解散等。

---

### 9.3 我加入的团队

**GET** `/api/teams/joined`

**Query**：`page`、`size`

**筛选条件**：`team_member.user_id = 当前用户` 且 `team_member.status = 1` 且 `team_member.is_deleted = 0` 且 `team.is_deleted = 0`

**说明**：

- **包含自己创建的团队**（创建者同样在 `team_member` 中且 `status=1`）
- 与 `managed` 可能数据重叠；前端分页面展示即可（`managed` 仅创建者管理视角，`joined` 为「我参与的全部团队」入口）
- 可选响应字段 `isCreator: true/false`，便于前端区分展示

**list 元素可扩展字段**

```json
{
  "id": 2,
  "teamName": "期末复习小组",
  "creatorId": 1,
  "creatorName": "zhangsan",
  "isShare": 1,
  "isCreator": true,
  "joinTime": "2026-05-24 16:00:00"
}
```

---

### 9.4 团队详情

**GET** `/api/teams/{id}`

**权限**：创建者或已加入成员

**成功响应 data**

```json
{
  "id": 2,
  "teamName": "期末复习小组",
  "creatorId": 1,
  "creatorName": "zhangsan",
  "isShare": 1,
  "myMemberRole": 0,
  "myStatus": 1,
  "memberCount": 5,
  "createTime": "2026-05-24 16:00:00"
}
```

| 字段 | 说明 |
|------|------|
| myMemberRole | 当前用户在团队中的角色 |
| myStatus | 当前用户在团队中的状态 |

---

### 9.5 解散团队

**DELETE** `/api/teams/{id}`

**权限**：仅 `creator_id = 当前用户`

**业务说明**：

- `team.is_deleted = 1`
- 该团队全部 `team_member.is_deleted = 1`
- `chat_history` **保留**，`team_id` 不变

---

### 9.6 开关知识库共享

**PUT** `/api/teams/{id}/share`

**请求体**

```json
{
  "isShare": 1
}
```

**权限**：仅团队创建者

---

### 9.7 团队成员列表

**GET** `/api/teams/{id}/members`

**Query**：`page`、`size`

**成功响应 data**：分页结构

```json
{
  "id": 5,
  "userId": 3,
  "username": "lisi",
  "memberRole": 0,
  "status": 1,
  "joinTime": "2026-05-24 16:30:00"
}
```

**权限**：创建者或已加入成员

---

### 9.8 邀请成员

**POST** `/api/teams/{id}/invite`

**请求体**

```json
{
  "username": "lisi"
}
```

**权限**：仅团队创建者

**业务说明**：

- 按用户名查找用户；不存在返回 404
- 若已有记录且 `status=2`（已拒绝）或 `is_deleted=1`（曾退出）：`UPDATE status=0, is_deleted=0`
- 若 `status=0`（待接受）或 `status=1`（已加入）：返回 409
- 不可邀请自己

---

### 9.9 我的待接受邀请

**GET** `/api/teams/invitations/pending`

**Query**：`page`、`size`

**说明**：`team_member.user_id=当前用户 AND status=0 AND is_deleted=0`

**list 元素示例**

```json
{
  "teamId": 2,
  "teamName": "期末复习小组",
  "creatorName": "zhangsan",
  "inviteTime": "2026-05-24 16:20:00"
}
```

---

### 9.10 接受邀请

**POST** `/api/teams/{id}/invite/accept`

**业务说明**：`team_member.status` 更新为 `1`

**错误**：403 非待接受状态 / 404 邀请不存在

---

### 9.11 拒绝邀请

**POST** `/api/teams/{id}/invite/reject`

**业务说明**：`team_member.status` 更新为 `2`

---

### 9.12 移除成员

**DELETE** `/api/teams/{id}/members/{userId}`

**权限**：仅团队创建者；不可移除自己（创建者）

**业务说明**：`team_member.is_deleted = 1`

---

### 9.13 退出团队

**POST** `/api/teams/{id}/leave`

**权限**：普通成员（`memberRole=0`）；创建者不可退出，须解散团队

**业务说明**：`team_member.is_deleted = 1`

---

## 10 团队共享资源（只读）

> 成员在 `isShare=1` 且 `status=1` 时可访问创建者的知识库，**只读 + 预览**。  
> 文件预览统一使用 **§5.6** `GET /api/files/{fileId}/preview-url`，本节仅提供列表类接口。

### 10.1 共享知识库分类列表

**GET** `/api/teams/{teamId}/categories`

**权限**：团队成员 + 共享已开启

**成功响应 data**：分类列表（结构同 §4.1，不含 Dify 内部 ID）

---

### 10.2 共享分类下文件列表

**GET** `/api/teams/{teamId}/categories/{categoryId}/files`

**Query**：`page`、`size`

**权限**：同上；且 `categoryId` 须属于团队创建者

**成功响应 data**：分页结构，元素结构同 §5.1

---

## 11 接口权限矩阵（摘要）

| 模块 | 接口 | 鉴权 | 额外权限 |
|------|------|------|----------|
| 认证 | register / login | 否 | — |
| 个人中心 | profile / password / avatar | 是 | 本人 |
| 分类 | CRUD | 是 | 分类 owner |
| 文件 | CRUD / preview | 是 | owner 或共享成员只读 |
| 同步 | POST sync | 是 | 分类 owner |
| 笔记 | CRUD | 是 | 笔记 owner |
| 个人问答 | POST personal | 是 | 所选分类须属本人 |
| 团队问答 | POST team | 是 | 成员 + isShare=1 |
| 问答历史 | 查 / 删 | 是 | 本人记录 |
| 团队管理 | 创建 / 解散 / 共享 / 邀请 / 移除 | 是 | 创建者 |
| 团队参与 | 接受 / 拒绝 / 退出 | 是 | 相关成员 |
| 共享浏览 | categories / files 列表 | 是 | 成员 + isShare=1 |
| 共享预览 | GET /api/files/{id}/preview-url | 是 | 成员 + isShare=1（与 §5.6 共用） |

---

## 12 Review 确认结论

| # | 问题 | 确认结果 |
|---|------|----------|
| 1 | `joined` 是否包含自己创建的团队 | ✅ **包含**；`managed` 专页管理我创建的，`joined` 为我参与的全部团队 |
| 2 | 预览 URL 是否共用 | ✅ **共用** `GET /api/files/{fileId}/preview-url` |
| 3 | 学习文件大小上限 | ✅ **与 Dify 一致**（默认 15MB，随 `UPLOAD_FILE_SIZE_LIMIT` 配置） |
| 4 | 头像大小上限 | ✅ **2MB** |
| 5 | 同步接口方式 | ✅ **同步阻塞**返回结果 |

---

## 13 接口清单汇总

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 注册 |
| POST | /api/auth/login | 登录 |
| GET | /api/user/profile | 当前用户信息 |
| PUT | /api/user/password | 修改密码 |
| POST | /api/user/avatar | 上传头像 |
| GET | /api/categories | 我的分类列表 |
| POST | /api/categories | 创建分类 |
| PUT | /api/categories/{id} | 重命名分类 |
| DELETE | /api/categories/{id} | 删除分类 |
| GET | /api/categories/{categoryId}/files | 分类文件列表 |
| GET | /api/files | 我的全部文件 |
| POST | /api/categories/{categoryId}/files | 上传文件 |
| PUT | /api/files/{id} | 重命名文件 |
| DELETE | /api/files/{id} | 删除文件 |
| GET | /api/files/{fileId}/preview-url | 预览/下载 URL（个人+团队共用） |
| POST | /api/categories/{categoryId}/sync | 同步知识库 |
| POST | /api/chat/personal | 个人问答 |
| POST | /api/chat/team | 团队问答 |
| GET | /api/chat/conversations | 会话列表 |
| GET | /api/chat/conversations/{conversationId} | 会话详情 |
| DELETE | /api/chat/history/{id} | 删除单条记录 |
| DELETE | /api/chat/conversations/{conversationId} | 删除会话 |
| DELETE | /api/chat/history | 清空历史 |
| GET | /api/notes | 笔记列表 |
| GET | /api/notes/{id} | 笔记详情 |
| POST | /api/notes | 创建笔记 |
| PUT | /api/notes/{id} | 更新笔记 |
| DELETE | /api/notes/{id} | 删除笔记 |
| POST | /api/teams | 创建团队 |
| GET | /api/teams/managed | 我创建的团队 |
| GET | /api/teams/joined | 我加入的团队 |
| GET | /api/teams/{id} | 团队详情 |
| DELETE | /api/teams/{id} | 解散团队 |
| PUT | /api/teams/{id}/share | 开关共享 |
| GET | /api/teams/{id}/members | 成员列表 |
| POST | /api/teams/{id}/invite | 邀请成员 |
| GET | /api/teams/invitations/pending | 待接受邀请 |
| POST | /api/teams/{id}/invite/accept | 接受邀请 |
| POST | /api/teams/{id}/invite/reject | 拒绝邀请 |
| DELETE | /api/teams/{id}/members/{userId} | 移除成员 |
| POST | /api/teams/{id}/leave | 退出团队 |
| GET | /api/teams/{teamId}/categories | 共享分类列表 |
| GET | /api/teams/{teamId}/categories/{categoryId}/files | 共享文件列表 |

**合计：40 个接口**

---

## 14 修订记录

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0 | 2026-05-24 | 初稿，对齐需求 v1.2、数据库 v1.1 |
| v1.1 | 2026-05-24 | Review 确认：joined 含自建团队、预览 URL 共用、文件上限对齐 Dify、头像 2MB、同步阻塞 |
| v1.2 | 2026-05-24 | 主题不入库，改前端 localStorage；移除 PUT /api/user/theme |
