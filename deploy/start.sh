#!/bin/bash

# AgentX一键启动脚本
# 支持多种部署模式：local/production/external

set -e

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # 无颜色

# 项目信息
echo -e "${BLUE}"
echo "   ▄▄▄        ▄████  ▓█████  ███▄    █ ▄▄▄█████▓▒██   ██▒"
echo "  ▒████▄     ██▒ ▀█▒ ▓█   ▀  ██ ▀█   █ ▓  ██▒ ▓▒▒▒ █ █ ▒░"
echo "  ▒██  ▀█▄  ▒██░▄▄▄░ ▒███   ▓██  ▀█ ██▒▒ ▓██░ ▒░░░  █   ░"
echo "  ░██▄▄▄▄██ ░▓█  ██▓ ▒▓█  ▄ ▓██▒  ▐▌██▒░ ▓██▓ ░  ░ █ █ ▒ "
echo "   ▓█   ▓██▒░▒▓███▀▒ ░▒████▒▒██░   ▓██░  ▒██▒ ░ ▒██▒ ▒██▒"
echo -e "   ▒▒   ▓▒█░ ░▒   ▒  ░░ ▒░ ░░ ▒░   ▒ ▒   ▒ ░░   ▒▒ ░ ░▓ ░ ${NC}"
echo -e "${GREEN}            智能AI助手平台 - 统一部署工具${NC}"
echo -e "${BLUE}========================================================${NC}"
echo

# 检查Docker环境
check_docker() {
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}错误: Docker未安装，请先安装Docker${NC}"
        exit 1
    fi

    if ! docker compose version &> /dev/null; then
        echo -e "${RED}错误: Docker Compose未安装或版本过低${NC}"
        exit 1
    fi
}

# 显示部署模式选择
show_deployment_modes() {
    echo -e "${YELLOW}请选择部署模式:${NC}"
    echo "  1) ${GREEN}local${NC}      - 本地开发环境（内置数据库，支持热重载）"
    echo "  2) ${BLUE}production${NC} - 生产环境（内置数据库，优化配置）"
    echo "  3) ${YELLOW}external${NC}   - 外部数据库（连接已有PostgreSQL）"
    echo "  4) ${RED}dev${NC}        - 开发环境+管理工具（包含Adminer）"
    echo
}

# 选择部署模式
select_deployment_mode() {
    while true; do
        show_deployment_modes
        read -p "请输入选择 (1-4): " choice
        case $choice in
            1)
                MODE="local"
                ENV_FILE=".env.local.example"
                PROFILE="local"
                DOCKERFILE_SUFFIX=".dev"
                break
                ;;
            2)
                MODE="production"
                ENV_FILE=".env.production.example"
                PROFILE="production"
                DOCKERFILE_SUFFIX=""
                break
                ;;
            3)
                MODE="external"
                ENV_FILE=".env.external.example"
                PROFILE="external"
                DOCKERFILE_SUFFIX=""
                break
                ;;
            4)
                MODE="dev"
                ENV_FILE=".env.local.example"
                PROFILE="local,dev"
                DOCKERFILE_SUFFIX=".dev"
                break
                ;;
            *)
                echo -e "${RED}无效选择，请重新输入${NC}"
                ;;
        esac
    done
}

# 准备环境配置
prepare_env() {
    if [ ! -f ".env" ]; then
        echo -e "${YELLOW}创建环境配置文件...${NC}"
        cp "$ENV_FILE" ".env"
        echo -e "${GREEN}✅ 已创建 .env 文件，基于模板: $ENV_FILE${NC}"
        
        if [ "$MODE" = "external" ]; then
            echo -e "${YELLOW}⚠️  外部数据库模式需要手动配置数据库连接信息${NC}"
            echo "   请编辑 .env 文件中的 DB_HOST, DB_USER, DB_PASSWORD 等配置"
            echo "   并确保数据库已执行初始化脚本: docs/sql/01_init.sql"
            echo
            read -p "配置完成后按回车继续..."
        fi
    else
        echo -e "${GREEN}✅ 使用现有 .env 配置文件${NC}"
    fi
}

# 启动服务
start_services() {
    echo -e "${BLUE}启动AgentX服务...${NC}"
    echo "部署模式: $MODE"
    echo "Docker Compose Profile: $PROFILE"
    echo

    # 设置环境变量
    export COMPOSE_PROFILES="$PROFILE"
    export DOCKERFILE_SUFFIX="$DOCKERFILE_SUFFIX"

    # 启动服务
    docker compose --profile "$PROFILE" up -d --build

    echo
    echo -e "${GREEN}🎉 AgentX启动完成！${NC}"
    echo
    echo -e "${BLUE}服务访问地址:${NC}"
    echo "  前端: http://localhost:3000"
    echo "  后端API: http://localhost:8080"
    
    if [ "$MODE" = "dev" ]; then
        echo "  数据库管理: http://localhost:8081"
    fi
    
    echo
    echo -e "${BLUE}默认登录账号:${NC}"
    echo "  管理员: admin@agentx.ai / admin123"
    
    if [ "$MODE" = "local" ] || [ "$MODE" = "dev" ]; then
        echo "  测试用户: test@agentx.ai / test123"
    fi
    
    echo
    echo -e "${YELLOW}常用命令:${NC}"
    echo "  查看日志: docker compose logs -f"
    echo "  停止服务: docker compose down"
    echo "  重启服务: docker compose restart"
    echo "  查看状态: docker compose ps"
}

# 主程序
main() {
    check_docker
    
    # 解析命令行参数
    if [ "$1" ]; then
        MODE="$1"
        case "$MODE" in
            local)
                ENV_FILE=".env.local.example"
                PROFILE="local"
                DOCKERFILE_SUFFIX=".dev"
                ;;
            production)
                ENV_FILE=".env.production.example"
                PROFILE="production"
                DOCKERFILE_SUFFIX=""
                ;;
            external)
                ENV_FILE=".env.external.example"
                PROFILE="external"
                DOCKERFILE_SUFFIX=""
                ;;
            dev)
                ENV_FILE=".env.local.example"
                PROFILE="local,dev"
                DOCKERFILE_SUFFIX=".dev"
                ;;
            *)
                echo -e "${RED}无效的部署模式: $MODE${NC}"
                echo "支持的模式: local, production, external, dev"
                exit 1
                ;;
        esac
    else
        select_deployment_mode
    fi
    
    prepare_env
    start_services
}

# 运行主程序
main "$@"