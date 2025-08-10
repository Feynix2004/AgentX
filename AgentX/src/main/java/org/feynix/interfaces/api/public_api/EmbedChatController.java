package org.feynix.interfaces.api.public_api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.feynix.application.agent.dto.AgentEmbedDTO;
import org.feynix.application.agent.service.AgentEmbedAppService;
import org.feynix.application.conversation.dto.ChatResponse;
import org.feynix.application.conversation.service.ConversationAppService;
import org.feynix.domain.agent.model.AgentEmbedEntity;
import org.feynix.infrastructure.exception.BusinessException;
import org.feynix.interfaces.api.common.Result;
import org.feynix.interfaces.dto.agent.request.EmbedChatRequest;

import java.net.MalformedURLException;
import java.net.URL;

/** 嵌入聊天控制器 - 公开API，无需认证 */
@RestController
@RequestMapping("/embed")
public class EmbedChatController {

    private final ConversationAppService conversationAppService;
    private final AgentEmbedAppService agentEmbedAppService;

    public EmbedChatController(ConversationAppService conversationAppService,
                             AgentEmbedAppService agentEmbedAppService) {
        this.conversationAppService = conversationAppService;
        this.agentEmbedAppService = agentEmbedAppService;
    }

    /** 获取嵌入配置信息（公开访问）
     *
     * @param publicId 公开访问ID
     * @param request HTTP请求
     * @return 嵌入配置基本信息 */
    @GetMapping("/{publicId}/info")
    public Result<EmbedInfoResponse> getEmbedInfo(@PathVariable String publicId,
                                                HttpServletRequest request) {
        try {
            // 1. 验证域名访问权限
            String referer = request.getHeader("Referer");
            if (!validateDomainAccess(publicId, referer)) {
                return Result.forbidden("域名访问被拒绝");
            }

            // 2. 获取嵌入配置
            AgentEmbedEntity embed = agentEmbedAppService.getEmbedForPublicAccess(publicId);
            
            // 3. 构建响应信息
            EmbedInfoResponse response = new EmbedInfoResponse();
            response.setPublicId(embed.getPublicId());
            response.setEmbedName(embed.getEmbedName());
            response.setEmbedDescription(embed.getEmbedDescription());
            response.setDailyLimit(embed.getDailyLimit());
            
            return Result.success(response);
            
        } catch (BusinessException e) {
            return Result.error(404, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "获取嵌入信息失败");
        }
    }

    /** 嵌入聊天接口（流式）
     *
     * @param publicId 公开访问ID
     * @param request 聊天请求
     * @param httpRequest HTTP请求
     * @return SSE流 */
    @PostMapping("/{publicId}/chat")
    public SseEmitter embedChat(@PathVariable String publicId,
                              @RequestBody @Validated EmbedChatRequest request,
                              HttpServletRequest httpRequest) {
        try {
            // 1. 验证域名访问权限
            String referer = httpRequest.getHeader("Referer");
            if (!validateDomainAccess(publicId, referer)) {
                throw new BusinessException("域名访问被拒绝");
            }

            // 2. 获取嵌入配置
            AgentEmbedEntity embed = agentEmbedAppService.getEmbedForPublicAccess(publicId);

            // 3. 处理嵌入聊天
            return conversationAppService.embedChat(publicId, request, embed);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("聊天服务异常：" + e.getMessage());
        }
    }

    /** 嵌入聊天接口（同步）
     *
     * @param publicId 公开访问ID
     * @param request 聊天请求
     * @param httpRequest HTTP请求
     * @return 同步聊天响应 */
    @PostMapping("/{publicId}/chat/sync")
    public Result<ChatResponse> embedChatSync(@PathVariable String publicId,
                                            @RequestBody @Validated EmbedChatRequest request,
                                            HttpServletRequest httpRequest) {
        try {
            // 1. 验证域名访问权限
            String referer = httpRequest.getHeader("Referer");
            if (!validateDomainAccess(publicId, referer)) {
                return Result.forbidden("域名访问被拒绝");
            }

            // 2. 获取嵌入配置
            AgentEmbedEntity embed = agentEmbedAppService.getEmbedForPublicAccess(publicId);

            // 3. 处理同步聊天
            ChatResponse response = conversationAppService.embedChatSync(publicId, request, embed);
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
            return agentEmbedAppService.validateDomainAccess(publicId, domain);
            
        } catch (MalformedURLException e) {
            // Referer格式错误，拒绝访问
            return false;
        } catch (Exception e) {
            // 其他异常，保守起见拒绝访问
            return false;
        }
    }

    /** 嵌入信息响应类 */
    public static class EmbedInfoResponse {
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