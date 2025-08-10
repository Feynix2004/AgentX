package org.feynix.domain.agent.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.feynix.infrastructure.converter.ListStringConverter;
import org.feynix.infrastructure.entity.BaseEntity;
import org.feynix.infrastructure.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Agent嵌入配置实体类，用于管理Agent的网站嵌入配置 */
@TableName(value = "agent_embeds", autoResultMap = true)
public class AgentEmbedEntity extends BaseEntity {

    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /** Agent ID */
    @TableField("agent_id")
    private String agentId;

    /** 创建者用户ID */
    @TableField("user_id")
    private String userId;

    /** 嵌入访问的唯一ID */
    @TableField("public_id")
    private String publicId;

    /** 嵌入名称 */
    @TableField("embed_name")
    private String embedName;

    /** 嵌入描述 */
    @TableField("embed_description")
    private String embedDescription;

    /** 指定使用的模型ID */
    @TableField("model_id")
    private String modelId;

    /** 可选：指定服务商ID */
    @TableField("provider_id")
    private String providerId;

    /** 允许的域名列表 */
    @TableField(value = "allowed_domains", typeHandler = ListStringConverter.class)
    private List<String> allowedDomains;

    /** 每日调用限制（-1为无限制） */
    @TableField("daily_limit")
    private Integer dailyLimit;

    /** 是否启用 */
    @TableField("enabled")
    private Boolean enabled;

    /** 无参构造函数 */
    public AgentEmbedEntity() {
        this.enabled = true;
        this.dailyLimit = -1;
    }

    /** 创建新的嵌入配置 */
    public static AgentEmbedEntity createNew(String agentId, String userId, String embedName, 
                                           String embedDescription, String modelId, String providerId,
                                           List<String> allowedDomains, Integer dailyLimit) {
        AgentEmbedEntity embed = new AgentEmbedEntity();
        embed.setAgentId(agentId);
        embed.setUserId(userId);
        embed.setPublicId(generateUniquePublicId());
        embed.setEmbedName(embedName);
        embed.setEmbedDescription(embedDescription);
        embed.setModelId(modelId);
        embed.setProviderId(providerId);
        embed.setAllowedDomains(allowedDomains);
        embed.setDailyLimit(dailyLimit != null ? dailyLimit : -1);
        embed.setEnabled(true);
        embed.setCreatedAt(LocalDateTime.now());
        embed.setUpdatedAt(LocalDateTime.now());
        return embed;
    }

    /** 生成唯一的公开访问ID */
    private static String generateUniquePublicId() {
        return "embed_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /** 检查是否启用 */
    public void checkEnabled() {
        if (!this.enabled) {
            throw new BusinessException("嵌入配置已禁用");
        }
    }

    /** 检查域名是否允许访问 */
    public boolean isDomainAllowed(String domain) {
        if (allowedDomains == null || allowedDomains.isEmpty()) {
            return true; // 空白名单表示允许所有域名
        }
        
        // 检查精确匹配和通配符匹配
        for (String allowedDomain : allowedDomains) {
            if (domain.equals(allowedDomain) || 
                (allowedDomain.startsWith("*.") && domain.endsWith(allowedDomain.substring(1)))) {
                return true;
            }
        }
        return false;
    }

    /** 启用嵌入配置 */
    public void enable() {
        this.enabled = true;
        this.updatedAt = LocalDateTime.now();
    }

    /** 禁用嵌入配置 */
    public void disable() {
        this.enabled = false;
        this.updatedAt = LocalDateTime.now();
    }

    /** 更新嵌入配置 */
    public void updateConfig(String embedName, String embedDescription, String modelId, 
                           String providerId, List<String> allowedDomains, Integer dailyLimit) {
        this.embedName = embedName;
        this.embedDescription = embedDescription;
        this.modelId = modelId;
        this.providerId = providerId;
        this.allowedDomains = allowedDomains;
        this.dailyLimit = dailyLimit != null ? dailyLimit : -1;
        this.updatedAt = LocalDateTime.now();
    }

    /** 软删除 */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getter和Setter方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getEmbedName() {
        return embedName;
    }

    public void setEmbedName(String embedName) {
        this.embedName = embedName;
    }

    public String getEmbedDescription() {
        return embedDescription;
    }

    public void setEmbedDescription(String embedDescription) {
        this.embedDescription = embedDescription;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public List<String> getAllowedDomains() {
        return allowedDomains;
    }

    public void setAllowedDomains(List<String> allowedDomains) {
        this.allowedDomains = allowedDomains;
    }

    public Integer getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(Integer dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}