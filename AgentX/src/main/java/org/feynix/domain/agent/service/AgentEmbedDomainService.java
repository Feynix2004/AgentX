package org.feynix.domain.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import org.feynix.domain.agent.model.AgentEmbedEntity;
import org.feynix.domain.agent.repository.AgentEmbedRepository;
import org.feynix.infrastructure.exception.BusinessException;

import java.util.List;

/** Agent嵌入配置领域服务 */
@Service
public class AgentEmbedDomainService{

    private final AgentEmbedRepository agentEmbedRepository;

    public AgentEmbedDomainService(AgentEmbedRepository agentEmbedRepository) {
        this.agentEmbedRepository = agentEmbedRepository;
    }

    /** 创建嵌入配置
     *
     * @param embed 嵌入配置实体
     * @return 创建的嵌入配置 */
    public AgentEmbedEntity createEmbed(AgentEmbedEntity embed) {

        LambdaQueryWrapper<AgentEmbedEntity> queryWrapper = Wrappers.<AgentEmbedEntity>lambdaQuery()
                .eq(AgentEmbedEntity::getPublicId, embed.getPublicId());
        boolean exists = agentEmbedRepository.exists(queryWrapper);
        // 检查公开ID是否唯一
        while (exists) {
            embed.setPublicId(generateNewPublicId());
        }
        
        agentEmbedRepository.insert(embed);
        return embed;
    }

    /** 根据ID获取嵌入配置
     *
     * @param embedId 嵌入配置ID
     * @param userId 用户ID（权限检查）
     * @return 嵌入配置实体 */
    public AgentEmbedEntity getEmbedById(String embedId, String userId) {
        AgentEmbedEntity embed = agentEmbedRepository.selectById(embedId);
        if (embed == null || embed.getDeletedAt() != null) {
            throw new BusinessException("嵌入配置不存在");
        }
        
        if (!embed.getUserId().equals(userId)) {
            throw new BusinessException("无权限访问此嵌入配置");
        }
        
        return embed;
    }


    /** 根据公开ID获取启用的嵌入配置
     *
     * @param publicId 公开访问ID
     * @return 启用的嵌入配置实体 */
    public AgentEmbedEntity getEnabledEmbedByPublicId(String publicId) {
        LambdaQueryWrapper<AgentEmbedEntity> queryWrapper = Wrappers.<AgentEmbedEntity>lambdaQuery()
                .eq(AgentEmbedEntity::getPublicId, publicId)
                .eq(AgentEmbedEntity::getEnabled, true);
        AgentEmbedEntity embed = agentEmbedRepository.selectOne(queryWrapper);
        if (embed == null) {
            throw new BusinessException("嵌入配置不存在或已禁用");
        }
        return embed;
    }

    /** 获取Agent的所有嵌入配置
     *
     * @param agentId Agent ID
     * @param userId 用户ID
     * @return 嵌入配置列表 */
    public List<AgentEmbedEntity> getEmbedsByAgent(String agentId, String userId) {
        LambdaQueryWrapper<AgentEmbedEntity> queryWrapper = Wrappers.<AgentEmbedEntity>lambdaQuery()
                .eq(AgentEmbedEntity::getAgentId, agentId)
                .eq(AgentEmbedEntity::getUserId, userId);
        return agentEmbedRepository.selectList(queryWrapper);
    }

    /** 获取用户的所有嵌入配置
     *
     * @param userId 用户ID
     * @return 嵌入配置列表 */
    public List<AgentEmbedEntity> getEmbedsByUser(String userId) {
        LambdaQueryWrapper<AgentEmbedEntity> queryWrapper = Wrappers.<AgentEmbedEntity>lambdaQuery()
                .eq(AgentEmbedEntity::getUserId, userId);
        return agentEmbedRepository.selectList(queryWrapper);
    }

    /** 更新嵌入配置
     *
     * @param embed 嵌入配置实体
     * @param userId 用户ID（权限检查）
     * @return 更新后的嵌入配置 */
    public AgentEmbedEntity updateEmbed(AgentEmbedEntity embed, String userId) {
        // 权限检查
        if (!embed.getUserId().equals(userId)) {
            throw new BusinessException("无权限修改此嵌入配置");
        }
        
        LambdaUpdateWrapper<AgentEmbedEntity> updateWrapper = Wrappers.<AgentEmbedEntity>lambdaUpdate()
                .eq(AgentEmbedEntity::getId, embed.getId())
                .eq(AgentEmbedEntity::getUserId, userId);
        
        agentEmbedRepository.checkedUpdate(embed, updateWrapper);
        return embed;
    }

    /** 切换嵌入配置启用状态
     *
     * @param embedId 嵌入配置ID
     * @param userId 用户ID
     * @return 更新后的嵌入配置 */
    public AgentEmbedEntity toggleEmbedStatus(String embedId, String userId) {
        AgentEmbedEntity embed = getEmbedById(embedId, userId);
        
        if (embed.getEnabled()) {
            embed.disable();
        } else {
            embed.enable();
        }
        
        return updateEmbed(embed, userId);
    }

    /** 删除嵌入配置（软删除）
     *
     * @param embedId 嵌入配置ID
     * @param userId 用户ID */
    public void deleteEmbed(String embedId, String userId) {
        AgentEmbedEntity embed = getEmbedById(embedId, userId);
        embed.delete();
        updateEmbed(embed, userId);
    }

    /** 验证域名访问权限
     *
     * @param publicId 公开访问ID
     * @param domain 访问域名
     * @return 是否允许访问 */
    public boolean validateDomainAccess(String publicId, String domain) {
        try {
            AgentEmbedEntity embed = getEnabledEmbedByPublicId(publicId);
            return embed.isDomainAllowed(domain);
        } catch (BusinessException e) {
            return false;
        }
    }

    /** 统计用户的嵌入配置数量
     *
     * @param userId 用户ID
     * @return 配置数量 */
    public long countEmbedsByUser(String userId) {
        LambdaQueryWrapper<AgentEmbedEntity> queryWrapper = Wrappers.<AgentEmbedEntity>lambdaQuery()
                .eq(AgentEmbedEntity::getUserId, userId);
        return agentEmbedRepository.selectCount(queryWrapper);
    }

    /** 统计Agent的嵌入配置数量
     *
     * @param agentId Agent ID
     * @param userId 用户ID
     * @return 配置数量 */
    public long countEmbedsByAgent(String agentId, String userId) {
        LambdaQueryWrapper<AgentEmbedEntity> queryWrapper = Wrappers.<AgentEmbedEntity>lambdaQuery()
                .eq(AgentEmbedEntity::getAgentId, agentId)
                .eq(AgentEmbedEntity::getUserId, userId);
        return agentEmbedRepository.selectCount(queryWrapper);
    }

    /** 检查用户是否可以创建更多嵌入配置
     *
     * @param userId 用户ID
     * @param maxEmbeds 最大嵌入配置数量（-1表示无限制）
     * @return 是否可以创建 */
    public boolean canCreateMoreEmbeds(String userId, int maxEmbeds) {
        if (maxEmbeds == -1) {
            return true; // 无限制
        }
        
        long currentCount = countEmbedsByUser(userId);
        return currentCount < maxEmbeds;
    }

    /** 生成新的公开ID */
    private String generateNewPublicId() {
        return "embed_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /** 批量获取嵌入配置 */
    public List<AgentEmbedEntity> getEmbedsByIds(List<String> embedIds, String userId) {
        if (embedIds == null || embedIds.isEmpty()) {
            return List.of();
        }
        
        LambdaQueryWrapper<AgentEmbedEntity> queryWrapper = Wrappers.<AgentEmbedEntity>lambdaQuery()
                .in(AgentEmbedEntity::getId, embedIds)
                .eq(AgentEmbedEntity::getUserId, userId)
                .isNull(AgentEmbedEntity::getDeletedAt)
                .orderByDesc(AgentEmbedEntity::getCreatedAt);
        
        return agentEmbedRepository.selectList(queryWrapper);
    }
}