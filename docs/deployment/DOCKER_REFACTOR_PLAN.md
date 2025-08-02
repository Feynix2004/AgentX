# AgentX Docker部署架构改造计划

## 📋 改造背景

### 现状问题分析

当前AgentX项目的Docker部署架构存在如下复杂性问题：

#### 1. 配置文件过多且分散
- **6个不同的docker-compose文件**：生产/开发/监控/网关等各自独立
- **多个Dockerfile版本**：每个服务都有dev和prod版本
- **复杂的启动脚本**：start-dev.sh达613行，包含大量业务逻辑

#### 2. 外部依赖管理复杂
- 需要自动克隆API-Premium-Gateway项目
- 复杂的路径替换和配置更新逻辑
- 多个数据库实例管理（AgentX + Gateway各自独立）

#### 3. 服务启动流程复杂
- 智能镜像检测和构建决策
- 复杂的健康检查机制
- 文件监听和热重载功能
- 交互式用户配置

#### 4. 维护成本高
- 新开发者上手困难
- 配置错误排查复杂
- 生产部署风险高
- 文档分散不统一

### 技术栈现状

项目使用以下技术栈：
- **后端**：Spring Boot + MyBatis-Plus + PostgreSQL
- **前端**：Next.js + TypeScript + Tailwind CSS
- **消息队列**：RabbitMQ
- **API网关**：独立的API-Premium-Gateway服务
- **容器化**：Docker + Docker Compose


## 🎯 改造目标

### 核心目标
1. **简化部署流程**：从10+步骤简化为1条命令
2. **配置统一管理**：通过环境变量统一管理所有配置
3. **移除脚本依赖**：完全移除Shell脚本，纯Docker Compose
4. **环境清晰分离**：本地开发vs生产部署完全分离
5. **保持技术栈**：不改变现有MyBatis-Plus架构

### 具体指标
- 配置文件：8个 → 3个主要文件
- 启动步骤：10+步骤 → 1条命令
- 代码行数：1000+行 → ~200行
- 外部依赖：需手动克隆 → 完全自包含
- 启动时间：5-8分钟 → 2-3分钟


## 🛠 详细改造方案

### 1. 配置化数据初始化

#### 现状分析
项目已有优秀的`DefaultDataInitializer`类，通过`ApplicationRunner`在应用启动时自动创建默认用户，但管理员信息是硬编码的。

#### 改造方案
修改`DefaultDataInitializer`支持配置化：

```java
@Component
@Order(100)
public class DefaultDataInitializer implements ApplicationRunner {
    
    @Value("${agentx.admin.email:admin@agentx.ai}")
    private String adminEmail;
    
    @Value("${agentx.admin.password:admin123}")
    private String adminPassword;
    
    @Value("${agentx.admin.nickname:AgentX管理员}")
    private String adminNickname;
    
    @Value("${agentx.test.enabled:true}")
    private Boolean testUserEnabled;
    
    @Value("${agentx.test.email:test@agentx.ai}")
    private String testEmail;
    
    @Value("${agentx.test.password:test123}")
    private String testPassword;
    
    // 现有逻辑改为使用配置变量
}
```

### 2. 统一Docker Compose架构

#### 设计原则
- **单一compose文件**：通过环境变量控制不同环境
- **服务解耦**：每个服务独立配置
- **网络统一**：所有服务使用同一网络
- **存储管理**：数据持久化配置

#### 核心服务架构
```yaml
services:
  postgres:       # 统一数据库（支持多schema）
  rabbitmq:       # 消息队列
  agentx-backend: # 后端服务
  api-gateway:    # API网关
  agentx-frontend: # 前端服务
  mcp-gateway:    # MCP网关
```

### 3. 环境分离策略

#### 本地开发模式
- **数据库**：容器内PostgreSQL，自动初始化
- **镜像**：本地构建（支持代码修改）
- **配置**：开发友好的默认值
- **日志**：详细调试信息

#### 生产部署模式
- **数据库**：外部PostgreSQL（物理机）
- **镜像**：预构建镜像（通过docker pull）
- **配置**：通过环境变量注入
- **日志**：生产级别日志

### 4. 数据库初始化策略

#### 保持现有方案
- ✅ 继续使用现有的`01_init.sql`文件
- ✅ 通过Docker volume挂载到PostgreSQL容器
- ✅ MyBatis-Plus处理业务逻辑
- ✅ `DefaultDataInitializer`创建默认用户

#### 优化点
- 配置化管理员信息
- 支持禁用测试用户（生产环境）
- 环境变量验证和默认值


## 📁 新架构目录结构

```
AgentX-2/
├── deploy/                     # 部署配置目录
│   ├── docker-compose.yml     # 统一compose文件
│   ├── .env.local.example     # 本地开发配置模板
│   ├── .env.production.example # 生产环境配置模板
│   └── README.md              # 部署使用说明
├── docker/                    # Dockerfile目录
│   ├── backend/
│   │   ├── Dockerfile         # 生产Dockerfile
│   │   └── Dockerfile.dev     # 开发Dockerfile
│   └── frontend/
│       ├── Dockerfile         # 生产Dockerfile
│       └── Dockerfile.dev     # 开发Dockerfile
├── docs/
│   └── deployment/
│       ├── DOCKER_REFACTOR_PLAN.md  # 本文档
│       ├── DEPLOYMENT_GUIDE.md      # 部署指南
│       └── TROUBLESHOOTING.md       # 问题排查
└── (现有项目代码)
```


## 🚀 实施计划

### 第一阶段：配置重构（第1-2周）

#### 任务列表
1. **创建新目录结构**
   - 创建`deploy/`目录
   - 创建`docker/`目录
   - 整理现有Dockerfile

2. **配置模板化**
   - 创建`.env`模板文件
   - 编写统一的`docker-compose.yml`
   - 配置环境变量映射

3. **代码适配**
   - 修改`DefaultDataInitializer`支持配置
   - 更新`application.yml`支持环境变量
   - 验证MyBatis-Plus配置兼容性

### 第二阶段：功能验证（第3周）

#### 任务列表
1. **本地开发环境测试**
   - 验证一键启动功能
   - 测试数据库自动初始化
   - 验证服务间通信

2. **生产环境模拟**
   - 连接外部数据库测试
   - 验证环境变量注入
   - 测试镜像拉取部署

3. **功能完整性验证**
   - 验证所有现有功能正常
   - 测试管理员登录
   - 验证API网关集成

### 第三阶段：文档完善（第4周）

#### 任务列表
1. **用户文档**
   - 编写部署指南
   - 创建快速开始教程
   - 整理常见问题

2. **开发者文档**
   - 架构说明文档
   - 配置参数文档
   - 故障排查手册

3. **迁移指南**
   - 从旧架构迁移步骤
   - 数据迁移指南
   - 回滚方案

### 第四阶段：发布上线（第5周）

#### 任务列表
1. **CI/CD集成**
   - 更新GitHub Actions
   - 自动镜像构建
   - 自动化测试

2. **正式发布**
   - 标记新版本
   - 更新README
   - 发布Release Notes


## ⚠️ 风险评估

### 高风险项
1. **数据库兼容性**
   - **风险**：SQL文件与不同PostgreSQL版本兼容性
   - **解决方案**：多版本测试，保持现有SQL结构

2. **服务依赖关系**
   - **风险**：服务启动顺序和依赖关系
   - **解决方案**：使用health check和depends_on

### 中风险项
1. **配置迁移**
   - **风险**：现有配置遗漏或错误映射
   - **解决方案**：配置对比表，逐项验证

2. **网络通信**
   - **风险**：容器间网络通信问题
   - **解决方案**：使用统一网络，测试服务发现

### 低风险项
1. **性能影响**
   - **风险**：新架构可能影响性能
   - **解决方案**：性能基准测试


## 🧪 测试计划

### 功能测试
1. **基础功能测试**
   - 用户注册登录
   - Agent创建和使用
   - 文件上传和RAG
   - API调用

2. **管理功能测试**
   - 管理员登录
   - 用户管理
   - 系统配置

### 环境测试
1. **本地开发环境**
   - 一键启动测试
   - 代码热重载测试
   - 数据库初始化测试

2. **生产环境**
   - 外部数据库连接测试
   - 环境变量注入测试
   - 服务稳定性测试

### 性能测试
1. **启动性能**
   - 容器启动时间
   - 服务就绪时间
   - 数据库初始化时间

2. **运行性能**
   - API响应时间
   - 数据库查询性能
   - 内存和CPU使用


## 🎉 预期收益

### 开发体验提升
- **新人上手时间**：从半天缩短到15分钟
- **环境搭建**：从手动配置到一键启动
- **故障排查**：从复杂脚本到标准化日志

### 运维效率提升
- **部署时间**：从30分钟缩短到5分钟
- **配置管理**：统一的环境变量管理
- **监控运维**：标准化的容器监控

### 项目维护性提升
- **代码维护**：移除1000+行脚本代码
- **文档完善**：标准化的部署文档
- **架构清晰**：明确的服务边界和依赖关系


## 📞 联系方式

如有任何问题或建议，请联系项目团队：
- 技术负责人：[技术负责人联系方式]
- 架构组：[架构组联系方式]
- 项目经理：[项目经理联系方式]


*本文档版本：v1.0*  
*最后更新：2025-01-08*  
*文档负责人：[负责人姓名]*