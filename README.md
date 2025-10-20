# AgentX - 智能对话系统平台

[](https://opensource.org/licenses/MIT)

AgentX 是一个基于大模型 (LLM) 和多能力平台 (MCP) 的智能 Agent 构建平台。它致力于简化 Agent 的创建流程，让用户无需复杂的流程节点或拖拽操作，仅通过自然语言和工具集成即可打造个性化的智能 Agent。

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


## 👨‍💻 开发环境部署

```bash
# 1. 克隆项目
git clone https://github.com/Feynix2004/AgentX.git
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






















