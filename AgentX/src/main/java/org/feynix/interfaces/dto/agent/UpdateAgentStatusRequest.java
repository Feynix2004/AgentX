package org.feynix.interfaces.dto.agent;

import org.feynix.domain.agent.model.AgentStatus;
import org.feynix.domain.common.util.ValidationUtils;

/**
 * 更新Agent状态的请求对象
 */
public class UpdateAgentStatusRequest {
    
    private AgentStatus status;
    
    // 构造方法
    public UpdateAgentStatusRequest() {
    }
    
    public UpdateAgentStatusRequest(AgentStatus status) {
        this.status = status;
    }
    
    /**
     * 校验请求参数
     */
    public void validate() {
        ValidationUtils.notNull(status, "status");
    }
    
    // Getter和Setter
    public AgentStatus getStatus() {
        return status;
    }
    
    public void setStatus(AgentStatus status) {
        this.status = status;
    }
} 