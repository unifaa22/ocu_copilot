# Dify 部署与配置指南

## 1. 部署 Dify

参考官方文档使用 Docker Compose 部署 Dify：

- 文档：https://docs.dify.ai/getting-started/install-self-hosted/docker-compose

部署完成后访问控制台（默认 `http://localhost`），创建管理员账号。

## 2. 创建知识库（Dataset）

1. 进入「知识库」→「创建知识库」
2. 记录该知识库的 **API Key**（Dataset API Key）
3. 每个 `file_category` 会在首次同步时对应一个 Dify Dataset（由后端自动创建）

## 3. 创建 Chat / 工作流应用

1. 创建「聊天助手」或「工作流」应用，启用 RAG / 知识库检索
2. 在应用设置中获取 **API Key**（App API Key）
3. 配置 Prompt、模型、检索策略（均在 Dify 控制台完成）

## 4. 后端配置

编辑 `backend/src/main/resources/application.yml`：

```yaml
dify:
  base-url: http://localhost/v1    # Dify API 根路径，按实际部署调整
  api-key: app-xxxxxxxx            # Chat / 工作流应用 API Key
  dataset-api-key: dataset-xxxxxxxx # 知识库 Dataset API Key
  stub-enabled: false              # 关闭 Stub，使用真实 Dify
  upload:
    max-file-size-mb: 15           # 与 Dify UPLOAD_FILE_SIZE_LIMIT 一致
```

> 生产环境建议通过环境变量注入 Key，勿提交至版本库。

## 5. Stub 模式（Dify 未就绪时）

保持 `dify.stub-enabled: true`（默认），同步与问答返回模拟结果，便于前后端联调与答辩演示。
