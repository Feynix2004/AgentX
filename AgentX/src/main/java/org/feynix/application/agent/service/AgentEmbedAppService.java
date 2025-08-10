package org.feynix.application.agent.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.feynix.application.agent.assembler.AgentEmbedAssembler;
import org.feynix.application.agent.dto.AgentEmbedDTO;
import org.feynix.application.llm.assembler.ModelAssembler;
import org.feynix.application.llm.assembler.ProviderAssembler;
import org.feynix.application.llm.dto.ModelDTO;
import org.feynix.application.llm.dto.ProviderDTO;
import org.feynix.application.llm.service.LLMAppService;
import org.feynix.domain.agent.model.AgentEntity;
import org.feynix.domain.agent.model.AgentEmbedEntity;
import org.feynix.domain.agent.repository.AgentRepository;
import org.feynix.domain.agent.service.AgentEmbedDomainService;
import org.feynix.domain.llm.model.ModelEntity;
import org.feynix.domain.llm.model.ProviderEntity;
import org.feynix.domain.llm.service.LLMDomainService;
import org.feynix.infrastructure.exception.BusinessException;
import org.feynix.interfaces.dto.agent.request.CreateEmbedRequest;
import org.feynix.interfaces.dto.agent.request.UpdateEmbedRequest;

import java.util.ArrayList;
import java.util.List;

/** Agent嵌入配置应用服务 */
@Service
public class AgentEmbedAppService {

    private final AgentEmbedDomainService agentEmbedDomainService;
    private final AgentRepository agentRepository;
    private final LLMDomainService llmDomainService;
    private final AgentEmbedAssembler agentEmbedAssembler;

    public AgentEmbedAppService(AgentEmbedDomainService agentEmbedDomainService,
                              AgentRepository agentRepository,
                              LLMDomainService llmDomainService,
                              LLMAppService llmAppService,
                              AgentEmbedAssembler agentEmbedAssembler) {
        this.agentEmbedDomainService = agentEmbedDomainService;
        this.agentRepository = agentRepository;
        this.llmDomainService = llmDomainService;
        this.agentEmbedAssembler = agentEmbedAssembler;
    }

    /** 创建嵌入配置
     *
     * @param agentId Agent ID
     * @param request 创建请求
     * @param userId 用户ID
     * @return 创建的嵌入配置DTO */
    @Transactional
    public AgentEmbedDTO createEmbed(String agentId, CreateEmbedRequest request, String userId) {
        // 1. 验证Agent权限
        validateAgentOwnership(agentId, userId);


        // 3. 检查是否可以创建更多嵌入配置（可选限制）
        // if (!agentEmbedDomainService.canCreateMoreEmbeds(userId, 10)) {
        //     throw new BusinessException("已达到最大嵌入配置数量限制");
        // }

        // 4. 创建嵌入配置实体
        AgentEmbedEntity embed = AgentEmbedAssembler.toEntity(request, agentId, userId);
        
        // 5. 保存到数据库
        AgentEmbedEntity savedEmbed = agentEmbedDomainService.createEmbed(embed);

        // 6. 获取关联的模型和服务商信息
        ModelEntity model = llmDomainService.getModelById(embed.getModelId());
        ProviderEntity provider = llmDomainService.getProvider(model.getProviderId());

        // 7. 转换为DTO并返回
        return agentEmbedAssembler.toDTOWithEmbedCode(savedEmbed, ModelAssembler.toDTO(model), ProviderAssembler.toDTO(provider));
    }

    /** 获取Agent的所有嵌入配置
     *
     * @param agentId Agent ID
     * @param userId 用户ID
     * @return 嵌入配置列表 */
    public List<AgentEmbedDTO> getEmbedsByAgent(String agentId, String userId) {
        // 1. 验证Agent权限
        validateAgentOwnership(agentId, userId);

        // 2. 获取嵌入配置列表
        List<AgentEmbedEntity> embeds = agentEmbedDomainService.getEmbedsByAgent(agentId, userId);
        
        if (embeds.isEmpty()) {
            return List.of();
        }

        // 3. 批量获取模型和服务商信息
        List<ModelDTO> models = new ArrayList<>();
        List<ProviderDTO> providers = new ArrayList<>();

        // 4. 转换为DTO列表
        return AgentEmbedAssembler.toDTOsWithEmbedCode(embeds, models, providers, 
                agentEmbedAssembler.frontendBaseUrl);
    }

    /** 获取用户的所有嵌入配置
     *
     * @param userId 用户ID
     * @return 嵌入配置列表 */
    public List<AgentEmbedDTO> getEmbedsByUser(String userId) {
        List<AgentEmbedEntity> embeds = agentEmbedDomainService.getEmbedsByUser(userId);
        
        if (embeds.isEmpty()) {
            return List.of();
        }

        List<ModelDTO> models = new ArrayList<>();
        List<ProviderDTO> providers = new ArrayList<>();

        return AgentEmbedAssembler.toDTOsWithEmbedCode(embeds, models, providers,
                agentEmbedAssembler.frontendBaseUrl);
    }

    /** 更新嵌入配置
     *
     * @param embedId 嵌入配置ID
     * @param request 更新请求
     * @param userId 用户ID
     * @return 更新后的嵌入配置DTO */
    @Transactional
    public AgentEmbedDTO updateEmbed(String embedId, UpdateEmbedRequest request, String userId) {
        AgentEmbedEntity embed = agentEmbedDomainService.getEmbedById(embedId, userId);

        AgentEmbedAssembler.updateEntity(embed, request);

        AgentEmbedEntity updatedEmbed = agentEmbedDomainService.updateEmbed(embed, userId);

        ModelEntity model = llmDomainService.getModelById(embed.getModelId());
        ProviderEntity provider = llmDomainService.getProvider(model.getProviderId());

        return agentEmbedAssembler.toDTOWithEmbedCode(updatedEmbed, ModelAssembler.toDTO(model), ProviderAssembler.toDTO(provider));
    }

    /** 切换嵌入配置启用状态
     *
     * @param embedId 嵌入配置ID
     * @param userId 用户ID
     * @return 更新后的嵌入配置DTO */
    @Transactional
    public AgentEmbedDTO toggleEmbedStatus(String embedId, String userId) {
        AgentEmbedEntity embed = agentEmbedDomainService.toggleEmbedStatus(embedId, userId);
        ModelEntity model = llmDomainService.getModelById(embed.getModelId());
        ProviderEntity provider = llmDomainService.getProvider(model.getProviderId());

        return agentEmbedAssembler.toDTOWithEmbedCode(embed, ModelAssembler.toDTO(model), ProviderAssembler.toDTO(provider));
    }

    /** 删除嵌入配置
     *
     * @param embedId 嵌入配置ID
     * @param userId 用户ID */
    @Transactional
    public void deleteEmbed(String embedId, String userId) {
        agentEmbedDomainService.deleteEmbed(embedId, userId);
    }

    /** 获取嵌入配置详情
     *
     * @param embedId 嵌入配置ID
     * @param userId 用户ID
     * @return 嵌入配置DTO */
    public AgentEmbedDTO getEmbedDetail(String embedId, String userId) {
        AgentEmbedEntity embed = agentEmbedDomainService.getEmbedById(embedId, userId);

        ModelEntity model = llmDomainService.getModelById(embed.getModelId());
        ProviderEntity provider = llmDomainService.getProvider(model.getProviderId());

        return agentEmbedAssembler.toDTOWithEmbedCode(embed, ModelAssembler.toDTO(model), ProviderAssembler.toDTO(provider));
    }

    /** 根据公开ID获取嵌入配置（用于公开访问）
     *
     * @param publicId 公开访问ID
     * @return 嵌入配置实体 */
    public AgentEmbedEntity getEmbedForPublicAccess(String publicId) {
        return agentEmbedDomainService.getEnabledEmbedByPublicId(publicId);
    }

    /** 验证域名访问权限
     *
     * @param publicId 公开访问ID
     * @param domain 访问域名
     * @return 是否允许访问 */
    public boolean validateDomainAccess(String publicId, String domain) {
        return agentEmbedDomainService.validateDomainAccess(publicId, domain);
    }

    // 私有辅助方法

    /** 验证Agent所有权 */
    private void validateAgentOwnership(String agentId, String userId) {
        AgentEntity agent = agentRepository.selectById(agentId);
        if (agent == null || agent.getDeletedAt() != null) {
            throw new BusinessException("Agent不存在");
        }

        if (!agent.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作此Agent");
        }
        }
}