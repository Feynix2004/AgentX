package org.feynix.application.admin.tool.service;


import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.feynix.domain.agent.constant.PublishStatus;
import org.feynix.domain.tool.constant.ToolStatus;
import org.feynix.domain.tool.model.ToolEntity;
import org.feynix.domain.tool.model.ToolVersionEntity;
import org.feynix.domain.tool.service.ToolDomainService;
import org.feynix.infrastructure.exception.BusinessException;

@Service
public class AdminToolAppService {

    private final ToolDomainService toolDomainService;

    public AdminToolAppService(ToolDomainService toolDomainService) {
        this.toolDomainService = toolDomainService;
    }

    public void updateToolStatus(String toolId, ToolStatus status, String reason) {

        ToolEntity tool = toolDomainService.getTool(toolId);

        // 如果状态一致
        if (tool.getStatus().equals(status)){
            throw new BusinessException("状态一致,不可修改");
        }

        tool.setStatus(status);
        tool.setRejectReason(reason);
        tool.setAdmin();
        toolDomainService.updateTool(tool);
    }
}
