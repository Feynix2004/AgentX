# AgentX 故障排查手册

本文档提供 AgentX 系统常见问题的诊断和解决方案，帮助快速定位和修复部署、配置、运行时问题。

## 📋 故障分类

- [部署问题](#部署问题)
- [配置问题](#配置问题)
- [网络问题](#网络问题)
- [数据库问题](#数据库问题)
- [性能问题](#性能问题)
- [安全问题](#安全问题)

## 🚨 紧急故障快速定位

### 快速诊断命令

```bash
# 1. 检查所有服务状态
docker compose ps

# 2. 查看近期错误日志
docker compose logs --tail=50 --since="10m"

# 3. 检查系统资源
docker stats --no-stream

# 4. 测试核心功能
curl -f http://localhost:8080/api/health || echo "后端服务异常"
curl -f http://localhost:3000 || echo "前端服务异常"
```

### 服务状态码解读

| 状态 | 含义 | 处理建议 |
|------|------|----------|
| Up | 正常运行 | 无需处理 |
| Restarting | 重启中 | 检查日志，可能有配置问题 |
| Exited (0) | 正常退出 | 检查是否意外停止 |
| Exited (1) | 异常退出 | 查看错误日志 |
| Dead | 服务死亡 | 重启服务并检查原因 |

## 🛠 部署问题

### 问题1: Docker 相关错误

#### 症状: `docker: command not found`
```bash
# 解决方案: 安装Docker
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# CentOS/RHEL
sudo yum install docker-ce docker-ce-cli containerd.io

# macOS
brew install docker
```

#### 症状: `docker compose: command not found`
```bash
# 解决方案: 升级Docker Compose
# 检查版本
docker --version
docker compose version

# 如果是旧版本，使用docker-compose
docker-compose --version

# 升级到新版本
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

#### 症状: `permission denied`
```bash
# 解决方案: 添加用户到docker组
sudo usermod -aG docker $USER
newgrp docker

# 或者使用sudo运行
sudo docker compose up -d
```

### 问题2: 镜像构建失败

#### 症状: `failed to build`
```bash
# 诊断步骤
# 1. 检查Dockerfile语法
docker compose config

# 2. 清理构建缓存
docker system prune -a

# 3. 重新构建
docker compose build --no-cache

# 4. 单独构建问题服务
docker compose build agentx-backend
```

#### 症状: 网络超时导致构建失败
```bash
# 解决方案: 配置镜像源
# 创建 /etc/docker/daemon.json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com"
  ]
}

# 重启Docker服务
sudo systemctl restart docker
```

### 问题3: 端口冲突

#### 症状: `port is already allocated`
```bash
# 诊断步骤
# 1. 查看端口占用
sudo lsof -i :3000
sudo lsof -i :8080
sudo lsof -i :5432

# 2. 停止占用进程
sudo kill -9 <PID>

# 3. 修改配置文件端口
vim .env
# FRONTEND_PORT=3001
# BACKEND_PORT=8081
```

## ⚙️ 配置问题

### 问题1: 环境变量未生效

#### 症状: 使用默认值而非环境变量值
```bash
# 诊断步骤
# 1. 检查.env文件是否存在
ls -la .env

# 2. 验证环境变量格式
cat .env | grep -v "^#" | grep -v "^$"

# 3. 检查容器内环境变量
docker compose exec agentx-backend env | grep AGENTX

# 4. 验证docker-compose.yml配置
docker compose config
```

#### 解决方案
```bash
# 1. 确保.env文件在正确位置
cp .env.production.example .env

# 2. 检查变量名拼写
# 3. 确保没有额外空格
sed -i 's/[[:space:]]*$//' .env

# 4. 重启服务使配置生效
docker compose restart
```

### 问题2: 数据库连接配置错误

#### 症状: `Connection refused` 或 `Unknown host`
```bash
# 诊断步骤
# 1. 检查数据库配置
docker compose exec agentx-backend env | grep DB_

# 2. 测试数据库连接
docker compose exec agentx-backend ping postgres

# 3. 验证数据库服务状态
docker compose ps postgres
```

#### 解决方案
```bash
# 1. 修正数据库主机名
# 内置模式: DB_HOST=postgres
# 外部模式: DB_HOST=your-db-host.com

# 2. 验证网络连接
docker network ls
docker network inspect deploy_agentx-network

# 3. 重启服务
docker compose restart agentx-backend
```

### 问题3: 管理员账户无法登录

#### 症状: 登录失败或账户不存在
```bash
# 诊断步骤
# 1. 检查管理员配置
docker compose exec agentx-backend env | grep AGENTX_ADMIN

# 2. 查看初始化日志
docker compose logs agentx-backend | grep -i "初始化\|admin"

# 3. 连接数据库检查
docker exec agentx-postgres psql -U agentx_user -d agentx -c "SELECT * FROM users WHERE email LIKE '%admin%';"
```

#### 解决方案
```bash
# 1. 确认配置正确
vim .env
# AGENTX_ADMIN_EMAIL=admin@agentx.ai
# AGENTX_ADMIN_PASSWORD=admin123

# 2. 如果已存在管理员，重置密码
docker exec agentx-postgres psql -U agentx_user -d agentx -c "
UPDATE users SET password = '$2a$10$encrypted_password_hash' 
WHERE email = 'admin@agentx.ai';
"

# 3. 重启后端服务
docker compose restart agentx-backend
```

## 🌐 网络问题

### 问题1: 前端无法访问后端API

#### 症状: API请求失败，网络错误
```bash
# 诊断步骤
# 1. 检查后端服务状态
curl http://localhost:8080/api/health

# 2. 检查前端配置
docker compose exec agentx-frontend env | grep NEXT_PUBLIC_API_BASE_URL

# 3. 测试容器间网络
docker compose exec agentx-frontend ping agentx-backend
```

#### 解决方案
```bash
# 1. 确认API基础URL配置
# 开发环境: NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
# 生产环境: NEXT_PUBLIC_API_BASE_URL=https://your-domain.com/api

# 2. 检查网络配置
docker compose config | grep networks -A 10

# 3. 重启前端服务
docker compose restart agentx-frontend
```

### 问题2: 外部无法访问服务

#### 症状: 本地可访问，外部无法访问
```bash
# 诊断步骤
# 1. 检查端口绑定
docker compose ps
netstat -tlnp | grep :3000

# 2. 检查防火墙
sudo ufw status
sudo firewall-cmd --list-all

# 3. 检查服务监听地址
ss -tlnp | grep :3000
```

#### 解决方案
```bash
# 1. 确保端口正确映射
# docker-compose.yml中确保端口映射为 "3000:3000"

# 2. 配置防火墙
sudo ufw allow 3000/tcp
sudo ufw allow 8080/tcp

# 3. 检查宿主机IP绑定
# 确保服务监听在0.0.0.0而非127.0.0.1
```

### 问题3: 消息队列连接问题

#### 症状: RabbitMQ连接失败
```bash
# 诊断步骤
# 1. 检查RabbitMQ状态
docker compose ps rabbitmq
docker compose logs rabbitmq

# 2. 测试管理界面
curl http://localhost:15672

# 3. 检查连接配置
docker compose exec agentx-backend env | grep RABBITMQ
```

#### 解决方案
```bash
# 1. 重启RabbitMQ服务
docker compose restart rabbitmq

# 2. 检查用户权限
docker exec agentx-rabbitmq rabbitmqctl list_users

# 3. 重新创建用户
docker exec agentx-rabbitmq rabbitmqctl add_user agentx_user password
docker exec agentx-rabbitmq rabbitmqctl set_permissions -p / agentx_user ".*" ".*" ".*"
```

## 💾 数据库问题

### 问题1: 数据库无法启动

#### 症状: PostgreSQL容器启动失败
```bash
# 诊断步骤
# 1. 查看详细错误
docker compose logs postgres

# 2. 检查数据目录权限
docker volume inspect agentx_postgres-data

# 3. 检查端口冲突
sudo lsof -i :5432
```

#### 解决方案
```bash
# 1. 清理数据卷重新初始化
docker compose down
docker volume rm agentx_postgres-data
docker compose up -d postgres

# 2. 修改端口避免冲突
vim .env
# POSTGRES_PORT=5433

# 3. 检查磁盘空间
df -h
```

### 问题2: 数据库连接池耗尽

#### 症状: `connection pool exhausted`
```bash
# 诊断步骤
# 1. 查看连接数
docker exec agentx-postgres psql -U agentx_user -d agentx -c "
SELECT count(*) as connections, state 
FROM pg_stat_activity 
GROUP BY state;
"

# 2. 查看长时间运行的查询
docker exec agentx-postgres psql -U agentx_user -d agentx -c "
SELECT pid, now() - pg_stat_activity.query_start AS duration, query 
FROM pg_stat_activity 
WHERE (now() - pg_stat_activity.query_start) > interval '5 minutes';
"
```

#### 解决方案
```bash
# 1. 调整连接池配置
vim AgentX/src/main/resources/application.yml
# spring:
#   datasource:
#     hikari:
#       maximum-pool-size: 50
#       minimum-idle: 10

# 2. 终止长时间运行的查询
docker exec agentx-postgres psql -U agentx_user -d agentx -c "
SELECT pg_terminate_backend(pid) FROM pg_stat_activity 
WHERE (now() - pg_stat_activity.query_start) > interval '10 minutes';
"

# 3. 重启后端服务
docker compose restart agentx-backend
```

### 问题3: 数据库磁盘空间不足

#### 症状: `No space left on device`
```bash
# 诊断步骤
# 1. 检查磁盘使用
df -h
docker system df

# 2. 查看数据库大小
docker exec agentx-postgres psql -U agentx_user -d agentx -c "
SELECT pg_size_pretty(pg_database_size('agentx'));
"

# 3. 检查表大小
docker exec agentx-postgres psql -U agentx_user -d agentx -c "\\dt+"
```

#### 解决方案
```bash
# 1. 清理Docker资源
docker system prune -a

# 2. 清理数据库
docker exec agentx-postgres psql -U agentx_user -d agentx -c "VACUUM FULL;"

# 3. 归档旧数据
# 根据业务需求删除或归档历史数据

# 4. 扩展磁盘空间
# 联系系统管理员扩展存储
```

## 🚀 性能问题

### 问题1: 响应时间过长

#### 症状: API响应缓慢
```bash
# 诊断步骤
# 1. 检查系统资源
docker stats --no-stream

# 2. 分析慢查询
docker exec agentx-postgres psql -U agentx_user -d agentx -c "
SELECT query, mean_time, calls, total_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;
"

# 3. 检查Java GC状况
docker compose logs agentx-backend | grep -i gc
```

#### 解决方案
```bash
# 1. 优化JVM参数
vim docker/backend/Dockerfile
# ENV JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC"

# 2. 添加数据库索引
# 根据慢查询分析结果添加适当索引

# 3. 启用缓存
# 配置Redis或应用层缓存

# 4. 扩容资源
# 增加CPU和内存配置
```

### 问题2: 内存溢出

#### 症状: `OutOfMemoryError`
```bash
# 诊断步骤
# 1. 查看内存使用
docker stats agentx-backend

# 2. 分析堆转储
# 如果启用了HeapDumpOnOutOfMemoryError
ls -la /app/heapdump*.hprof

# 3. 检查内存泄漏
docker compose logs agentx-backend | grep -i "memory\|heap"
```

#### 解决方案
```bash
# 1. 增加JVM堆内存
vim .env
# JAVA_OPTS="-Xms1g -Xmx2g"

# 2. 优化代码
# 检查是否有内存泄漏

# 3. 增加容器内存限制
vim docker-compose.yml
# services:
#   agentx-backend:
#     deploy:
#       resources:
#         limits:
#           memory: 4g
```

### 问题3: 数据库锁等待

#### 症状: 查询响应很慢
```bash
# 诊断步骤
# 1. 查看锁等待
docker exec agentx-postgres psql -U agentx_user -d agentx -c "
SELECT blocked_locks.pid AS blocked_pid,
       blocked_activity.usename AS blocked_user,
       blocking_locks.pid AS blocking_pid,
       blocking_activity.usename AS blocking_user,
       blocked_activity.query AS blocked_statement,
       blocking_activity.query AS current_statement_in_blocking_process
FROM pg_catalog.pg_locks blocked_locks
JOIN pg_catalog.pg_stat_activity blocked_activity ON blocked_activity.pid = blocked_locks.pid
JOIN pg_catalog.pg_locks blocking_locks ON blocking_locks.locktype = blocked_locks.locktype
JOIN pg_catalog.pg_stat_activity blocking_activity ON blocking_activity.pid = blocking_locks.pid
WHERE NOT blocked_locks.GRANTED;
"
```

#### 解决方案
```bash
# 1. 终止阻塞查询
docker exec agentx-postgres psql -U agentx_user -d agentx -c "
SELECT pg_terminate_backend(<blocking_pid>);
"

# 2. 优化事务
# 减少事务持有时间
# 避免长时间运行的事务

# 3. 添加适当索引
# 减少锁竞争
```

## 🔒 安全问题

### 问题1: 未授权访问

#### 症状: 安全扫描发现漏洞
```bash
# 诊断步骤
# 1. 检查默认密码
grep -r "admin123\|test123" .env*

# 2. 检查端口暴露
nmap localhost

# 3. 检查SSL配置
curl -I https://your-domain.com
```

#### 解决方案
```bash
# 1. 修改所有默认密码
vim .env
# 生成强密码
openssl rand -base64 32

# 2. 配置HTTPS
# 使用Let's Encrypt或其他SSL证书

# 3. 限制端口暴露
# 仅暴露必要端口
```

### 问题2: JWT令牌安全

#### 症状: 令牌容易被破解
```bash
# 诊断步骤
# 1. 检查JWT密钥强度
echo $JWT_SECRET | wc -c

# 2. 分析令牌
# 使用jwt.io解析令牌结构
```

#### 解决方案
```bash
# 1. 生成强JWT密钥
openssl rand -base64 64

# 2. 配置令牌过期时间
# 在代码中设置合理的过期时间

# 3. 实施令牌刷新机制
# 定期更新令牌
```

## 🔍 日志分析工具

### 日志收集脚本

```bash
#!/bin/bash
# collect-logs.sh

LOG_DIR="/tmp/agentx-logs-$(date +%Y%m%d-%H%M%S)"
mkdir -p $LOG_DIR

echo "收集AgentX系统日志..."

# 系统信息
echo "=== 系统信息 ===" > $LOG_DIR/system-info.txt
uname -a >> $LOG_DIR/system-info.txt
docker --version >> $LOG_DIR/system-info.txt
docker compose version >> $LOG_DIR/system-info.txt

# 服务状态
echo "=== 服务状态 ===" > $LOG_DIR/service-status.txt
docker compose ps >> $LOG_DIR/service-status.txt

# 服务日志
docker compose logs --no-color > $LOG_DIR/all-services.log
docker compose logs --no-color agentx-backend > $LOG_DIR/backend.log
docker compose logs --no-color agentx-frontend > $LOG_DIR/frontend.log
docker compose logs --no-color postgres > $LOG_DIR/postgres.log

# 配置文件
cp .env $LOG_DIR/env-config.txt
docker compose config > $LOG_DIR/compose-config.yml

# 打包
tar czf agentx-logs-$(date +%Y%m%d-%H%M%S).tar.gz -C /tmp $(basename $LOG_DIR)

echo "日志收集完成: agentx-logs-$(date +%Y%m%d-%H%M%S).tar.gz"
```

### 实时监控命令

```bash
# 实时查看所有服务日志
docker compose logs -f

# 监控资源使用
watch docker stats

# 监控文件系统
watch df -h

# 监控网络连接
watch 'ss -tlnp | grep -E "(3000|8080|5432)"'
```

## 📞 获取支持

### 报告问题

提交问题时请提供：

1. **环境信息**
   - 操作系统版本
   - Docker版本
   - Docker Compose版本

2. **配置信息**
   - 部署模式 (local/production/external)
   - 主要配置参数

3. **错误信息**
   - 完整错误日志
   - 相关服务状态

4. **复现步骤**
   - 详细操作步骤
   - 预期结果vs实际结果

### 紧急联系

- **技术支持**: 项目GitHub Issues
- **文档更新**: 提交PR到docs目录
- **安全问题**: 私密方式联系维护团队


*最后更新: 2025-01-08*  
*文档版本: v2.0*