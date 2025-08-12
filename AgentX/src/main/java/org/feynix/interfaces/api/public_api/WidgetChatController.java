package org.feynix.interfaces.api.public_api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.feynix.application.agent.service.AgentWidgetAppService;
import org.feynix.application.conversation.dto.ChatResponse;
import org.feynix.application.conversation.service.ConversationAppService;
import org.feynix.domain.agent.model.AgentWidgetEntity;
import org.feynix.infrastructure.exception.BusinessException;
import org.feynix.interfaces.api.common.Result;
import org.feynix.interfaces.dto.agent.request.WidgetChatRequest;

import java.net.MalformedURLException;
import java.net.URL;

/** 小组件聊天控制器 - 公开API，无需认证 */
@RestController
@RequestMapping("/widget")
public class WidgetChatController {

    private final ConversationAppService conversationAppService;
    private final AgentWidgetAppService agentWidgetAppService;

    public WidgetChatController(ConversationAppService conversationAppService,
                             AgentWidgetAppService agentWidgetAppService) {
        this.conversationAppService = conversationAppService;
        this.agentWidgetAppService = agentWidgetAppService;
    }

    /** 获取小组件配置信息（公开访问）
     *
     * @param publicId 公开访问ID
     * @param request HTTP请求
     * @return 小组件配置基本信息 */
    @GetMapping("/{publicId}/info")
    public Result<WidgetInfoResponse> getWidgetInfo(@PathVariable String publicId,
                                                HttpServletRequest request) {
        try {
            // 1. 验证域名访问权限
            String referer = request.getHeader("Referer");
            if (!validateDomainAccess(publicId, referer)) {
                return Result.forbidden("域名访问被拒绝");
            }

            // 2. 获取小组件配置
            AgentWidgetEntity widget = agentWidgetAppService.getWidgetForPublicAccess(publicId);
            
            // 3. 构建响应信息
            WidgetInfoResponse response = new WidgetInfoResponse();
            response.setPublicId(widget.getPublicId());
            response.setEmbedName(widget.getEmbedName());
            response.setEmbedDescription(widget.getEmbedDescription());
            response.setDailyLimit(widget.getDailyLimit());
            
            return Result.success(response);
            
        } catch (BusinessException e) {
            return Result.error(404, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "获取小组件信息失败");
        }
    }

    /** 小组件聊天接口（流式）
     *
     * @param publicId 公开访问ID
     * @param request 聊天请求
     * @param httpRequest HTTP请求
     * @return SSE流 */
    @PostMapping("/{publicId}/chat")
    public SseEmitter widgetChat(@PathVariable String publicId,
                              @RequestBody @Validated WidgetChatRequest request,
                              HttpServletRequest httpRequest) {
        try {
            // 1. 验证域名访问权限
            String referer = httpRequest.getHeader("Referer");
            if (!validateDomainAccess(publicId, referer)) {
                throw new BusinessException("域名访问被拒绝");
            }

            // 2. 获取小组件配置
            AgentWidgetEntity widget = agentWidgetAppService.getWidgetForPublicAccess(publicId);

            // 3. 处理小组件聊天
            return conversationAppService.widgetChat(publicId, request, widget);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("聊天服务异常：" + e.getMessage());
        }
    }

    /** 小组件聊天接口（同步）
     *
     * @param publicId 公开访问ID
     * @param request 聊天请求
     * @param httpRequest HTTP请求
     * @return 同步聊天响应 */
    @PostMapping("/{publicId}/chat/sync")
    public Result<ChatResponse> widgetChatSync(@PathVariable String publicId,
                                            @RequestBody @Validated WidgetChatRequest request,
                                            HttpServletRequest httpRequest) {
        try {
            // 1. 验证域名访问权限
            String referer = httpRequest.getHeader("Referer");
            if (!validateDomainAccess(publicId, referer)) {
                return Result.forbidden("域名访问被拒绝");
            }

            // 2. 获取小组件配置
            AgentWidgetEntity widget = agentWidgetAppService.getWidgetForPublicAccess(publicId);

            // 3. 处理同步聊天
            ChatResponse response = conversationAppService.widgetChatSync(publicId, request, widget);
            return Result.success(response);
            
        } catch (BusinessException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "聊天服务异常：" + e.getMessage());
        }
    }

    /** 验证域名访问权限
     *
     * @param publicId 公开访问ID
     * @param referer 来源域名
     * @return 是否允许访问 */
    private boolean validateDomainAccess(String publicId, String referer) {
        try {
            // 如果没有Referer，可能是直接访问或API调用，根据业务需求决定是否允许
            if (referer == null || referer.isEmpty()) {
                // 这里可以根据配置决定是否允许无Referer的访问
                return true; // 暂时允许，实际可根据需求调整
            }

            // 从Referer中提取域名
            URL url = new URL(referer);
            String domain = url.getHost();

            // 验证域名权限
            return agentWidgetAppService.validateDomainAccess(publicId, domain);
            
        } catch (MalformedURLException e) {
            // Referer格式错误，拒绝访问
            return false;
        } catch (Exception e) {
            // 其他异常，保守起见拒绝访问
            return false;
        }
    }

    /** 小组件信息响应类 */
    public static class WidgetInfoResponse {
        private String publicId;
        private String embedName;
        private String embedDescription;
        private Integer dailyLimit;

        // Getter和Setter方法
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

        public Integer getDailyLimit() {
            return dailyLimit;
        }

        public void setDailyLimit(Integer dailyLimit) {
            this.dailyLimit = dailyLimit;
        }
    }
}