# 学生 AI 知识工作台（OCU.copilot）

基于 Vue 3 + Spring Boot + MySQL + MinIO + Dify 的轻量化学生知识管理与 AI 协作平台。

## 项目结构

```
diagnose-illusion/
├── backend/          # Spring Boot 4 REST API（40 个接口）
├── frontend/         # Vue 3 + Vite + Tailwind
├── database/         # MySQL 建表脚本
├── docs/             # 需求说明书、REST API 文档、演示脚本
└── docker-compose.yml
```

## 快速开始

### 1. 启动 MySQL 与 MinIO

```bash
docker compose up -d
```

- MySQL：`localhost:3306`，库名 `knowledge_workbench`，root 密码 `123456`
- MinIO：`http://localhost:9000`（控制台 `9001`），账号 `minioadmin` / `minioadmin`

首次启动会自动执行 `database/schema.sql` 初始化表结构与角色数据。

### 2. 启动后端

```bash
cd backend
./mvnw spring-boot:run
```

默认端口 `8080`。配置见 `backend/src/main/resources/application.yml`：

| 配置项 | 说明 |
|--------|------|
| `jwt.secret` | JWT 签名密钥（生产环境务必修改） |
| `minio.*` | 对象存储连接 |
| `dify.stub-enabled` | `true` 使用 Stub 模拟 AI；`false` 对接真实 Dify |
| `dify.upload.max-file-size-mb` | 学习文件大小上限（默认 15MB） |

### 3. 启动前端

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

前端开发服务器会将 `/api` 代理到 `http://localhost:8080`。

## API 文档

- 接口契约：[docs/rest-api.md](docs/rest-api.md)
- 需求说明：[docs/学生AI知识工作台软件需求说明书.md](docs/学生AI知识工作台软件需求说明书.md)
- 答辩演示：[docs/DEMO.md](docs/DEMO.md)

## 功能模块

| 模块 | 说明 |
|------|------|
| 认证 / 个人中心 | JWT 登录注册、头像（MinIO）、改密 |
| 知识库 | 分类 CRUD、文件上传预览、按分类同步 Dify |
| AI 问答 | 个人多分类问答、团队共享问答、会话历史 |
| 笔记 | Markdown 笔记、多标签筛选 |
| 团队 | 邀请成员、共享创建者知识库、只读浏览 |

## Dify 对接

详见 [docs/DIFY.md](docs/DIFY.md)。

## 技术栈

- **前端**：Vue 3、Pinia、Vue Router、Tailwind CSS、Axios
- **后端**：Spring Boot 4、Spring Security + JWT、JPA、MinIO SDK
- **数据库**：MySQL 8
- **AI**：Dify（文档解析、向量检索、RAG 问答）

## 开发说明

- 后端统一响应：`{ code, message, data }`，成功 `code=200`
- 所有 DELETE 为逻辑删除
- 主题偏好仅存浏览器 `localStorage`，无后端接口
