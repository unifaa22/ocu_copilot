# Diagnose Illusion (诊断幻觉)

本项目采用目前非常标准、主流的前后端分离架构。项目结构分为后端的 Spring Boot 服务和前端的 Vue 3 应用。

## 整体架构概览

### 后端 (backend) 
后端遵循经典的 MVC 三层架构进行物理分层，职责划分清晰：

- **核心框架**: Spring Boot (v4.0.6)
- **开发语言**: Java 17
- **项目构建**: Maven
- **核心依赖**:
  - **Spring Boot WebMVC**: 提供 Web 服务、RESTful API 能力。
  - **Spring Boot Data JPA**: 作为 ORM 框架，以面向对象的方式操作数据库。
  - **Spring Boot Security**: 提供应用的安全控制、认证与授权框架。
  - **MySQL Connector J**: MySQL 数据库的运行时驱动。
  - **Lombok**: 提供注解支持，用于自动生成 Getter/Setter/构造器等样板代码，简化开发。
- **目录层次 (`src/main/java/.../diagnoseillusion/`)**:
  - `controller/`: **控制层**。负责接收前端的 HTTP 请求，校验参数，并调用 Service 层处理业务，最后返回统一格式的响应（`Result`）。
  - `service/` & `service/impl/`: **业务逻辑层**。包含业务接口及具体实现，负责核心业务逻辑的处理。
  - `repository/`: **数据访问层**。负责数据库交互，通常是 MyBatis 的 Mapper 接口或 Spring Data JPA 的 Repository。
  - `entity/`: **实体类**。与数据库表结构进行直接映射的简单 Java 对象 (POJO)。
  - `dto/`: **数据传输对象 (Data Transfer Object)**。用于 Controller 与客户端，或者 Service 与 Controller 传递数据，避免将实体类直接暴露。
  - `config/`: **配置中心**。如安全框架配置 (`SecurityConfig.java`) 和跨域配置 (`CorsConfig.java`) 等核心系统级配置。
  - `common/`: **通用组件**。统一接口规范，例如统一的 API 返回类 (`Result.java`) 和全局异常监听处理器 (`GlobalExceptionHandler.java`)，这是现代标准后端的标配。

### 前端 (frontend) 
前端采用了快速、现代化的 SPA (单页应用) 架构模式：

- **技术栈**: Vue 3, Vite, Node.js
- **目录层次 (`src/`)**:
  - `router/`: **路由层**。负责页面 URL 到组件的映射和页面导航卫士。
  - `stores/`: **全局状态管理**。通过管理共享状态（通常使用 Pinia），实现跨组件通信和数据存储。
  - `App.vue`: **根组件**。
  - `main.js`: **应用入口点**。负责挂载 Vue 实例及其所有插件。
  - `vite.config.js`: **工程化和构建配置**。处理本地服务器启动、接口跨域代理 (`/api` 转发) 及打包配置。

## 启动指南

### 启动后端
1. 确保安装了 Java (JDK) 和 Maven。
2. 进入 `backend` 目录。
3. 如果使用 IDE (如 IntelliJ IDEA/Eclipse)，找到 `DiagnoseIllusionApplication.java` 直接运行 main 方法。或者在终端执行 `mvn spring-boot:run`。
4. 后端服务默认端口通常为 `8080` (详见 `application.yml`)。

### 启动前端
1. 确保安装了 Node.js。
2. 进入 `frontend` 目录。
3. 执行 `npm install` 安装所有前端依赖。
4. 执行 `npm run dev` 启动 Vite 开发服务器（由于已配置代理，后端 API 会自动将请求跨域转发至 `http://localhost:8080`）。
