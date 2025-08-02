# AgentX 生产环境部署指南

本文档专门针对生产环境用户，提供**无需源码**的快速部署方案。

## 🎯 适用人群

- **生产环境部署**：需要稳定运行的用户
- **快速体验**：想要快速试用AgentX的用户
- **运维人员**：负责服务部署和维护

> 💡 **开发者请使用**：源码目录下的 `deploy/start.sh` 进行开发

## 🚀 一键部署（推荐）

### 方式1：最简单部署

```bash
# 直接启动，使用默认配置
docker run -d \
  --name agentx \
  -p 3000:3000 \
  -p 8080:8080 \
  ghcr.io/feynix2004/agentx:latest

# 查看启动日志
docker logs agentx -f
```

**访问地址**：
- 前端：http://localhost:3000
- 后端：http://localhost:8080

**默认账号**：
- 管理员：admin@agentx.ai / admin123
- 测试用户：test@agentx.ai / test123

### 方式2：使用配置文件

```bash
# 1. 下载生产配置
curl -O https://raw.githubusercontent.com/feynix2004/AgentX/master/production/docker-compose.yml
curl -O https://raw.githubusercontent.com/feynix2004/AgentX/master/production/.env.example

# 2. 配置环境变量
mv .env.example .env
vim .env  # 修改配置

# 3. 启动服务
docker compose up -d

# 4. 查看状态
docker compose ps
```

## ⚙️ 配置文件说明

### 核心配置项

```bash
# 服务端口
FRONTEND_PORT=3000
BACKEND_PORT=8080

# 数据库配置（使用内置PostgreSQL）
DB_HOST=postgres
DB_PORT=5432
DB_NAME=agentx
DB_USER=agentx_user
DB_PASSWORD=your_secure_password  # ⚠️ 请修改

# 管理员配置
AGENTX_ADMIN_EMAIL=admin@your-domain.com
AGENTX_ADMIN_PASSWORD=your_admin_password  # ⚠️ 请修改
AGENTX_ADMIN_NICKNAME=系统管理员

# 生产环境设置
AGENTX_TEST_ENABLED=false  # 生产环境禁用测试用户
```

### 安全配置（重要）

```bash
# JWT密钥（必须修改）
JWT_SECRET=your_jwt_secret_key_here

# 数据库密码（必须修改）
DB_PASSWORD=your_secure_password

# 消息队列密码（建议修改）
RABBITMQ_USERNAME=agentx_mq
RABBITMQ_PASSWORD=your_rabbitmq_password
```

## 🔗 外部数据库部署

如果您有独立的PostgreSQL数据库：

### 1. 准备数据库

```sql
-- 连接到PostgreSQL
CREATE DATABASE agentx;
CREATE USER agentx_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE agentx TO agentx_user;
```

### 2. 初始化表结构

```bash
# 下载初始化脚本
curl -O https://raw.githubusercontent.com/feynix2004/AgentX/master/docs/sql/01_init.sql

# 执行初始化
psql -h your-db-host -U agentx_user -d agentx -f 01_init.sql
```

### 3. 启动应用（无数据库）

```bash
docker run -d \
  --name agentx \
  -p 3000:3000 \
  -p 8080:8080 \
  -e DB_HOST=your-db-host \
  -e DB_PORT=5432 \
  -e DB_NAME=agentx \
  -e DB_USER=agentx_user \
  -e DB_PASSWORD=your_password \
  -e AGENTX_ADMIN_EMAIL=admin@your-domain.com \
  -e AGENTX_ADMIN_PASSWORD=your_admin_password \
  ghcr.io/feynix2004/agentx:latest
```

## 🔒 生产环境安全配置

### SSL/HTTPS配置

推荐使用Nginx反向代理：

```nginx
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    ssl_certificate /path/to/certificate.crt;
    ssl_certificate_key /path/to/private.key;
    
    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 防火墙配置

```bash
# Ubuntu/Debian
ufw allow 22/tcp    # SSH
ufw allow 80/tcp    # HTTP
ufw allow 443/tcp   # HTTPS
ufw --force enable

# 内部端口不对外开放
# 3000, 8080 仅通过Nginx访问
```

## 🛠️ 常用管理命令

### 服务管理

```bash
# 查看状态
docker ps
docker compose ps  # 如果使用docker-compose

# 查看日志
docker logs agentx -f
docker compose logs -f  # 如果使用docker-compose

# 重启服务
docker restart agentx
docker compose restart  # 如果使用docker-compose

# 停止服务
docker stop agentx
docker compose down  # 如果使用docker-compose
```

### 数据备份

```bash
# 数据库备份
docker exec agentx-postgres pg_dump -U agentx_user agentx | gzip > backup_$(date +%Y%m%d).sql.gz

# 文件存储备份
docker run --rm \
  -v agentx_storage-data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/storage_backup_$(date +%Y%m%d).tar.gz -C /data .
```

### 更新升级

```bash
# 1. 停止服务
docker stop agentx

# 2. 备份数据（重要）
docker exec agentx-postgres pg_dump -U agentx_user agentx > backup_before_upgrade.sql

# 3. 拉取最新镜像
docker pull ghcr.io/feynix2004/agentx:latest

# 4. 重新启动
docker rm agentx
docker run -d \
  --name agentx \
  -p 3000:3000 \
  -p 8080:8080 \
  -v agentx_postgres-data:/var/lib/postgresql/data \
  -v agentx_storage-data:/app/storage \
  ghcr.io/feynix2004/agentx:latest
```

## 📊 监控和维护

### 健康检查

```bash
# 检查服务健康状态
curl http://localhost:8080/api/health

# 检查数据库连接
docker exec agentx-postgres pg_isready -U agentx_user
```

### 日志管理

```bash
# 查看错误日志
docker logs agentx | grep -i error

# 日志轮转（防止日志文件过大）
docker run --log-driver json-file --log-opt max-size=10m --log-opt max-file=5 ...
```

### 性能监控

```bash
# 查看资源使用
docker stats agentx

# 查看数据库大小
docker exec agentx-postgres psql -U agentx_user -d agentx -c "
SELECT pg_size_pretty(pg_database_size('agentx'));"
```

## 🆘 故障排查

### 常见问题

1. **服务无法启动**
   ```bash
   # 查看详细日志
   docker logs agentx
   
   # 检查端口占用
   lsof -i :3000
   lsof -i :8080
   ```

2. **数据库连接失败**
   ```bash
   # 检查数据库状态
   docker exec agentx-postgres pg_isready -U agentx_user
   
   # 查看数据库日志
   docker logs agentx-postgres
   ```

3. **前端无法访问后端**
   ```bash
   # 检查API健康状态
   curl http://localhost:8080/api/health
   
   # 检查网络连通性
   docker network ls
   ```

### 获取支持

- **故障排查手册**：[TROUBLESHOOTING.md](./TROUBLESHOOTING.md)
- **GitHub Issues**：[项目Issues页面](https://github.com/feynix2004/AgentX/issues)
- **详细部署指南**：[DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md)

## 📋 快速检查清单

部署完成后，请确认：

- [ ] 前端可以正常访问 (http://localhost:3000)
- [ ] 后端API响应正常 (http://localhost:8080/api/health)
- [ ] 管理员账户可以登录
- [ ] 数据库连接正常
- [ ] 已修改默认密码和密钥
- [ ] 防火墙规则已配置
- [ ] 数据备份方案已实施


**生产环境部署成功！** 🎉

如有问题，请参考故障排查文档或联系技术支持。