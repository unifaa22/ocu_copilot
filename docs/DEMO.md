# 答辩演示脚本

## 环境准备

1. 启动基础设施：`docker compose up -d`（MySQL + MinIO）
2. 启动后端：`cd backend && mvnw spring-boot:run`
3. 启动前端：`cd frontend && npm run dev`

## 演示流程（约 8–10 分钟）

### 1. 注册与登录（1 分钟）

- 注册新用户 `demo01` / 密码 `123456`
- 登录后进入首页，展示主题切换（明暗模式）

### 2. 知识库管理（2 分钟）

- 创建分类「期末复习」
- 上传 PDF 或 Markdown 文件
- 点击「同步知识库」（`dify.stub-enabled=true` 时返回模拟同步结果）
- 打开文件预览

### 3. AI 个人问答（2 分钟）

- 进入「个人问答」，选择已同步的分类
- 提问：「这份资料的核心知识点是什么？」
- 展示多轮对话与会话历史列表

### 4. 笔记（1 分钟）

- 新建笔记，编辑 Markdown，添加标签
- 按标签筛选

### 5. 团队协作（2 分钟）

- 用户 A 创建团队并邀请用户 B
- 用户 B 接受邀请
- 开启知识库共享
- 用户 B 在「团队问答」中提问（使用创建者知识库）

### 6. 收尾（1 分钟）

- 个人中心修改密码 / 上传头像
- 说明：AI 能力由 Dify 提供，后端负责鉴权、存储与 API 转发

## 切换真实 Dify

在 `backend/src/main/resources/application.yml` 中：

```yaml
dify:
  base-url: http://localhost/v1
  api-key: app-你的应用Key
  dataset-api-key: dataset-你的知识库Key
  stub-enabled: false
```

部署 Dify 后创建 Dataset 与 Chat 应用，将 Key 填入配置并重启后端。
