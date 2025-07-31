package org.feynix.application.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.feynix.application.agent.assembler.AgentAssembler;
import org.feynix.application.agent.assembler.AgentVersionAssembler;
import org.feynix.application.agent.dto.AgentDTO;
import org.feynix.application.agent.dto.AgentWithUserDTO;
import org.feynix.application.agent.dto.AgentStatisticsDTO;
import org.feynix.domain.agent.model.AgentEntity;
import org.feynix.application.agent.dto.AgentVersionDTO;
import org.feynix.domain.agent.model.AgentVersionEntity;
import org.feynix.domain.agent.model.AgentWorkspaceEntity;
import org.feynix.domain.agent.model.LLMModelConfig;
import org.feynix.domain.agent.service.AgentDomainService;
import org.feynix.domain.agent.service.AgentWorkspaceDomainService;
import org.feynix.infrastructure.exception.ParamValidationException;
import org.feynix.domain.agent.constant.PublishStatus;
import org.feynix.interfaces.dto.agent.request.*;
import org.feynix.domain.scheduledtask.service.ScheduledTaskExecutionService;
import org.feynix.application.billing.service.BillingService;
import org.feynix.application.billing.dto.BillingContext;
import org.feynix.domain.product.constant.BillingType;
import org.feynix.domain.product.constant.UsageDataKeys;
import org.feynix.infrastructure.exception.InsufficientBalanceException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Agent应用服务，用于适配领域层的Agent服务 职责： 1. 接收和验证来自接口层的请求 2. 将请求转换为领域对象或参数 3. 调用领域服务执行业务逻辑 4. 转换和返回结果给接口层 */
@Service
public class AgentAppService {

    private static final Logger logger = LoggerFactory.getLogger(AgentAppService.class);

    private final AgentDomainService agentServiceDomainService;
    private final AgentWorkspaceDomainService agentWorkspaceDomainService;
    private final ScheduledTaskExecutionService scheduledTaskExecutionService;
    private final BillingService billingService;

    public AgentAppService(AgentDomainService agentServiceDomainService,
            AgentWorkspaceDomainService agentWorkspaceDomainService,
            ScheduledTaskExecutionService scheduledTaskExecutionService, BillingService billingService) {
        this.agentServiceDomainService = agentServiceDomainService;
        this.agentWorkspaceDomainService = agentWorkspaceDomainService;
        this.scheduledTaskExecutionService = scheduledTaskExecutionService;
        this.billingService = billingService;
    }

    /** 创建新Agent */
    @Transactional
    public AgentDTO createAgent(CreateAgentRequest request, String userId) {
        logger.info("开始创建Agent - 用户: {}, Agent名称: {}", userId, request.getName());

        // 1. 创建计费上下文进行余额预检查
        BillingContext billingContext = BillingContext.builder().type(BillingType.AGENT_CREATION.getCode())
                .serviceId("agent_creation") // 固定业务标识
                .usageData(Map.of(UsageDataKeys.QUANTITY, 1)).requestId(generateRequestId(userId, "creation"))
                .userId(userId).build();

        // 2. 余额预检查 - 避免创建后发现余额不足
        if (!billingService.checkBalance(billingContext)) {
            logger.warn("Agent创建失败 - 用户余额不足: {}", userId);
            throw new InsufficientBalanceException("账户余额不足，无法创建Agent。请先充值后再试。");
        }

        // 3. 执行Agent创建逻辑
        AgentEntity entity = AgentAssembler.toEntity(request, userId);
        entity.setUserId(userId);
        AgentEntity agent = agentServiceDomainService.createAgent(entity);
        AgentWorkspaceEntity agentWorkspaceEntity = new AgentWorkspaceEntity(agent.getId(), userId,
                new LLMModelConfig());
        agentWorkspaceDomainService.save(agentWorkspaceEntity);

        // 4. 创建成功后执行计费扣费
        try {
            billingService.charge(billingContext);
            logger.info("Agent创建及计费成功 - 用户: {}, AgentID: {}, 请求ID: {}", userId, agent.getId(),
                    billingContext.getRequestId());
        } catch (Exception e) {
            // 计费失败但Agent已创建，记录错误日志但不影响用户体验
            // 实际场景中可能需要考虑回滚Agent创建或者重试机制
            logger.error("Agent创建成功但计费失败 - 用户: {}, AgentID: {}, 错误: {}", userId, agent.getId(), e.getMessage(), e);
            throw new InsufficientBalanceException("Agent创建成功，但计费处理失败: " + e.getMessage());
        }

        return AgentAssembler.toDTO(agent);
    }

    /** 获取Agent信息 */
    public AgentDTO getAgent(String agentId, String userId) {
        // todo feynix 判断用户是否存在
        AgentEntity agent = agentServiceDomainService.getAgent(agentId, userId);
        return AgentAssembler.toDTO(agent);
    }

    /** 获取用户的Agent列表，支持状态和名称过滤 */
    public List<AgentDTO> getUserAgents(String userId, SearchAgentsRequest searchAgentsRequest) {
        AgentEntity entity = AgentAssembler.toEntity(searchAgentsRequest);
        List<AgentEntity> agents = agentServiceDomainService.getUserAgents(userId, entity);
        return AgentAssembler.toDTOs(agents);
    }
    /** 获取已上架的Agent列表，支持名称搜索 */

    public List<AgentVersionDTO> getPublishedAgentsByName(SearchAgentsRequest searchAgentsRequest, String userId) {
        AgentEntity entity = AgentAssembler.toEntity(searchAgentsRequest);
        List<AgentVersionEntity> agentVersionEntities = agentServiceDomainService.getPublishedAgentsByName(entity);
        if (agentVersionEntities.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> agentIds = agentVersionEntities.stream().map(AgentVersionEntity::getAgentId).toList();
        List<AgentWorkspaceEntity> agentWorkspaceEntities = agentWorkspaceDomainService.listAgents(agentIds, userId);
        Set<String> agentIdsSet = agentWorkspaceEntities.stream().map(AgentWorkspaceEntity::getAgentId)
                .collect(Collectors.toSet());

        List<AgentVersionDTO> agentVersionDTOS = AgentVersionAssembler.toDTOs(agentVersionEntities);
        if (agentIdsSet.isEmpty()) {
            return agentVersionDTOS;
        }
        for (AgentVersionDTO agentVersionDTO : agentVersionDTOS) {
            agentVersionDTO.setAddWorkspace(agentIdsSet.contains(agentVersionDTO.getAgentId()));
        }
        return agentVersionDTOS;
    }

    /** 更新Agent信息（基本信息和配置合并更新） */
    public AgentDTO updateAgent(UpdateAgentRequest request, String userId) {

        // 使用组装器创建更新实体
        AgentEntity updateEntity = AgentAssembler.toEntity(request, userId);

        // 调用领域服务更新Agent
        AgentEntity agentEntity = agentServiceDomainService.updateAgent(updateEntity);
        return AgentAssembler.toDTO(agentEntity);
    }

    /** 切换Agent的启用/禁用状态 */
    public AgentDTO toggleAgentStatus(String agentId) {
        AgentEntity agentEntity = agentServiceDomainService.toggleAgentStatus(agentId);
        return AgentAssembler.toDTO(agentEntity);
    }

    /** 删除Agent */
    @Transactional
    public void deleteAgent(String agentId, String userId) {
        // 先删除Agent关联的定时任务（包括取消延迟队列中的任务）
        scheduledTaskExecutionService.deleteTasksByAgentId(agentId, userId);
        // 再删除Agent本身
        agentServiceDomainService.deleteAgent(agentId, userId);
    }

    /** 发布Agent版本 */
    public AgentVersionDTO publishAgentVersion(String agentId, PublishAgentVersionRequest request, String userId) {
        // 在应用层验证请求
        request.validate();

        // 获取当前Agent
        AgentEntity agent = agentServiceDomainService.getAgent(agentId, userId);

        // 获取最新版本，检查版本号大小
        AgentVersionEntity agentVersionEntity = agentServiceDomainService.getLatestAgentVersion(agentId);
        if (agentVersionEntity != null) {
            // 检查版本号是否大于上一个版本
            if (!request.isVersionGreaterThan(agentVersionEntity.getVersionNumber())) {
                throw new ParamValidationException("versionNumber", "新版本号(" + request.getVersionNumber()
                        + ")必须大于当前最新版本号(" + agentVersionEntity.getVersionNumber() + ")");
            }
        }

        // 使用组装器创建版本实体
        AgentVersionEntity versionEntity = AgentVersionAssembler.createVersionEntity(agent, request);

        versionEntity.setUserId(userId);
        // 调用领域服务发布版本
        agentVersionEntity = agentServiceDomainService.publishAgentVersion(agentId, versionEntity);
        return AgentVersionAssembler.toDTO(agentVersionEntity);
    }

    /** 获取Agent的所有版本 */
    public List<AgentVersionDTO> getAgentVersions(String agentId, String userId) {
        List<AgentVersionEntity> agentVersions = agentServiceDomainService.getAgentVersions(agentId, userId);
        return AgentVersionAssembler.toDTOs(agentVersions);
    }

    /** 获取Agent的特定版本 */
    public AgentVersionDTO getAgentVersion(String agentId, String versionNumber) {
        AgentVersionEntity agentVersion = agentServiceDomainService.getAgentVersion(agentId, versionNumber);
        return AgentVersionAssembler.toDTO(agentVersion);
    }

    /** 获取Agent的最新版本 */
    public AgentVersionDTO getLatestAgentVersion(String agentId) {
        AgentVersionEntity latestAgentVersion = agentServiceDomainService.getLatestAgentVersion(agentId);
        return AgentVersionAssembler.toDTO(latestAgentVersion);
    }

    /** 审核Agent版本 */
    public AgentVersionDTO reviewAgentVersion(String versionId, ReviewAgentVersionRequest request) {
        // 在应用层验证请求
        request.validate();

        AgentVersionEntity agentVersionEntity = null;
        // 根据状态执行相应操作
        if (PublishStatus.REJECTED.equals(request.getStatus())) {
            // 拒绝发布，需使用拒绝原因
            agentVersionEntity = agentServiceDomainService.rejectVersion(versionId, request.getRejectReason());
        } else {
            // 其他状态变更，直接更新状态
            agentVersionEntity = agentServiceDomainService.updateVersionPublishStatus(versionId, request.getStatus());
        }
        return AgentVersionAssembler.toDTO(agentVersionEntity);
    }

    /** 根据发布状态获取版本列表
     * 
     * @param status 发布状态
     * @return 版本列表（每个助理只返回最新版本） */
    public List<AgentVersionDTO> getVersionsByStatus(PublishStatus status) {
        List<AgentVersionEntity> versionsByStatus = agentServiceDomainService.getVersionsByStatus(status);
        return AgentVersionAssembler.toDTOs(versionsByStatus);
    }

    /** 分页查询Agent列表（管理员使用，包含用户信息）
     * 
     * @param queryAgentRequest 查询条件
     * @return Agent分页数据（包含用户信息） */
    public Page<AgentWithUserDTO> getAgents(QueryAgentRequest queryAgentRequest) {
        Page<AgentEntity> page = agentServiceDomainService.getAgents(queryAgentRequest);
        return agentServiceDomainService.getAgentsWithUserInfo(page);
    }

    /** 获取Agent统计信息
     * 
     * @return Agent统计数据 */
    public AgentStatisticsDTO getAgentStatistics() {
        return agentServiceDomainService.getAgentStatistics();
    }

    /** 生成用于计费的唯一请求ID
     * 
     * @param userId 用户ID
     * @param action 操作类型
     * @return 唯一请求ID */
    private String generateRequestId(String userId, String action) {
        return String.format("agent_%s_%s_%d", action, userId, System.currentTimeMillis());
    }
}