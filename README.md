# Quill-AI - 智能对话系统平台

[](https://opensource.org/licenses/MIT)

Quill-AI 是一个基于大模型 (LLM) 和多能力平台 (MCP) 的智能 Agent 构建平台。它致力于简化 Agent 的创建流程，让用户无需复杂的流程节点或拖拽操作，仅通过自然语言和工具集成即可打造个性化的智能 Agent。

<div align="center">
 
[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen?style=flat-square)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-15-black?style=flat-square)](https://nextjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-blue?style=flat-square)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=flat-square)](https://www.postgresql.org/)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-1.3.0-purple?style=flat-square)](https://docs.langchain4j.dev/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue?style=flat-square)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)

</div>

## 🔗 相关链接

### 📦 子仓库
- 🛡️ **高可用网关**: [API-Premium-Gateway](https://github.com/Feynix2004/API-Premium-Gateway) - 模型高可用组件
- 🌐 **MCP网关**: [mcp-gateway](https://github.com/Feynix2004/mcp-gateway) - MCP服务统一管理
- 🏪 **MCP社区**: [agent-mcp-community](https://github.com/Feynix2004/agent-mcp-community) - MCP Server 开源社区


## 🏗️ 技术架构

### 后端架构 (Java 17 + Spring Boot 3.5.3)

基于**领域驱动设计(DDD)**的四层架构模式：

```
┌─────────────────────────────────────────────────────────────┐
│                    Interface Layer (接口层)                     │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐             │
│  │ REST API    │ │   DTO       │ │Validation   │             │
│  │ Controllers │ │ Conversion  │ │   & Auth    │             │
│  └─────────────┘ └─────────────┘ └─────────────┘             │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                  Application Layer (应用层)                    │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐             │
│  │Agent Service│ │Conversation │ │   Workflow  │             │
│  │Management   │ │ Service     │ │  Service    │             │
│  └─────────────┘ └─────────────┘ └─────────────┘             │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                    Domain Layer (领域层)                      │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐             │
│  │   Entities  │ │ Value Objects│ │Domain Events│             │
│  │   & Aggregates│             │ │             │             │
│  └─────────────┘ └─────────────┘ └─────────────┘             │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│               Infrastructure Layer (基础设施层)                │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐             │
│  │ PostgreSQL  │ │LangChain4j  │ │   External  │             │
│  │   Database  │ │ Integration │ │  Services   │             │
│  └─────────────┘ └─────────────┘ └─────────────┘             │
└─────────────────────────────────────────────────────────────┘
```

### 核心技术栈

#### 后端技术
- **Java 17** + **Spring Boot 3.5.3** - 现代化Java企业级框架
- **MyBatis-Plus 3.5.7** - 高性能ORM框架
- **PostgreSQL 15** - 企业级关系型数据库
- **LangChain4j 1.3.0** - AI模型集成框架
  - 支持OpenAI、阿里云百炼、智谱AI等多种LLM提供商
  - 集成MCP (Model Context Protocol)支持
- **JWT认证** - 安全的用户认证和授权机制
- **事件驱动架构** - AgentEventBus异步消息处理

#### 前端技术
- **Next.js 15** + **React 19** - 现代化React全栈框架
- **TypeScript 5** - 类型安全的JavaScript超集
- **Tailwind CSS 4.1.9** - 原子化CSS框架
- **Ant Design 5.26.7** - 企业级UI组件库
- **Radix UI** - 无头组件库，提供完全的可定制性
- **Zustand** - 轻量级状态管理方案
- **React Hook Form + Zod** - 高性能表单处理和验证
- **AI SDK** - Vercel AI对话集成支持
- **react-markdown** - Markdown内容渲染支持

#### 开发运维
- **Docker + Docker Compose** - 容器化部署方案
- **Maven** - Java项目依赖管理
- **ESLint + Prettier** - 代码质量保证
- **PostgreSQL** - 数据库初始化和管理



## 👨‍💻 开发环境部署

```bash
# 1. 克隆项目
git clone https://github.com/Feynix2004/Quill-AI.git
cd AgentX/deploy

# 2. 启动开发环境（Linux）
./start.sh

# 2. 启动开发环境（Windows）
start.bat
```


## ⏳ 功能
 - [x] Agent 管理（创建/发布）
 - [x] LLM 上下文管理（滑动窗口，摘要算法）
 - [x] Agent 策略（MCP）
 - [x] 大模型服务商
 - [x] 用户
 - [x] 工具市场
 - [x] MCP Server Community
 - [x] MCP Gateway 
 - [x] 预先设置工具
 - [x] Agent 定时任务
 - [x] Agent OpenAPI
 - [x] 模型高可用组件
 - [x] RAG
 - [x] 计费
 - [x] Agent 监控
 - [x] 嵌入网站组件
 
## ⚙️ 环境变量配置

AgentX使用`.env`配置文件进行环境变量管理，支持丰富的自定义配置






















