package org.feynix.application.agent.dto;

import org.feynix.application.llm.dto.ModelDTO;
import org.feynix.application.llm.dto.ProviderDTO;
import org.feynix.domain.agent.constant.WidgetType;

import java.time.LocalDateTime;
import java.util.List;

/** Agent小组件配置DTO */
public class AgentWidgetDTO {

    /** 主键ID */
    private String id;

    /** Agent ID */
    private String agentId;

    /** 创建者用户ID */
    private String userId;

    /** 嵌入访问的唯一ID */
    private String publicId;

    /** 嵌入名称 */
    private String embedName;

    /** 嵌入描述 */
    private String embedDescription;

    /** 关联的模型信息 */
    private ModelDTO model;

    /** 关联的服务商信息 */
    private ProviderDTO provider;

    /** 允许的域名列表 */
    private List<String> allowedDomains;

    /** 每日调用限制（-1为无限制） */
    private Integer dailyLimit;

    /** 是否启用 */
    private Boolean enabled;

    /** Widget类型：AGENT/RAG */
    private WidgetType widgetType;

    /** 知识库ID列表（RAG类型专用） */
    private List<String> knowledgeBaseIds;

    /** 嵌入代码 */
    private String embedCode;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

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

    public ModelDTO getModel() {
        return model;
    }

    public void setModel(ModelDTO model) {
        this.model = model;
    }

    public ProviderDTO getProvider() {
        return provider;
    }

    public void setProvider(ProviderDTO provider) {
        this.provider = provider;
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

    public String getEmbedCode() {
        return embedCode;
    }

    public void setEmbedCode(String embedCode) {
        this.embedCode = embedCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public WidgetType getWidgetType() {
        return widgetType;
    }

    public void setWidgetType(WidgetType widgetType) {
        this.widgetType = widgetType;
    }

    public List<String> getKnowledgeBaseIds() {
        return knowledgeBaseIds;
    }

    public void setKnowledgeBaseIds(List<String> knowledgeBaseIds) {
        this.knowledgeBaseIds = knowledgeBaseIds;
    }
}