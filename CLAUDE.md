# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Architecture

AgentX is an intelligent agent platform built with Domain-Driven Design (DDD) architecture, featuring:

**Backend (Java/Spring Boot)**:
- **Domain Layer**: Core business logic with entities, repositories, and domain services
- **Application Layer**: Business process orchestration and use cases 
- **Infrastructure Layer**: Technical implementations (persistence, configs, utilities)
- **Interface Layer**: API controllers and external interfaces

**Frontend (Next.js/TypeScript)**:
- **Pages**: Route-based page components using Next.js App Router
- **Components**: Reusable UI components with Tailwind CSS and Shadcn/ui
- **Lib**: API services, HTTP client, and utility functions
- **Types**: TypeScript type definitions
- **Contexts**: React Context for state management

**Key Technologies**:
- Backend: Spring Boot 3.2.3, MyBatis-Plus, PostgreSQL, JWT
- Frontend: Next.js 15, React 19, TypeScript, Tailwind CSS
- Infrastructure: Docker, Maven, Gradle

## Development Notes

### Frontend Development
- 前端代码进行修改后不用进行 npm install,因为前端项目一直在启动,会热更新
- 不需要执行 npm run build