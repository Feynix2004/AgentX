package org.feynix.application.conversation.service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.stereotype.Service;
import org.feynix.application.conversation.dto.StreamChatRequest;
import org.feynix.application.conversation.dto.StreamChatResponse;
import org.feynix.domain.agent.dto.AgentDTO;
import org.feynix.domain.agent.dto.AgentVersionDTO;
import org.feynix.domain.agent.service.AgentDomainService;
import org.feynix.domain.conversation.dto.MessageDTO;
import org.feynix.domain.conversation.model.MessageEntity;
import org.feynix.domain.conversation.model.SessionEntity;
import org.feynix.domain.conversation.service.ConversationDomainService;
import org.feynix.domain.conversation.service.SessionDomainService;
import org.feynix.infrastructure.exception.BusinessException;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * 对话应用服务，用于适配域层的对话服务
 */
@Service
public class ConversationAppService {

    private final ConversationDomainService conversationDomainService;

    private final SessionDomainService sessionDomainService;


    public ConversationAppService(
            ConversationDomainService conversationDomainService,
            SessionDomainService sessionDomainService,
            AgentDomainService agentDomainService
            ) {
        this.conversationDomainService = conversationDomainService;
        this.sessionDomainService = sessionDomainService;

    }

    /**
     * 处理流式聊天请求
     */
    public void chatStream(StreamChatRequest request, BiConsumer<StreamChatResponse, Boolean> responseHandler) {
        conversationDomainService.chatStream(request, responseHandler);
    }

    public MessageEntity saveAssistantMessage(String sessionId, String content,
            String provider, String model, Integer tokenCount) {
        return conversationDomainService.saveAssistantMessage(sessionId, content, provider, model, tokenCount);
    }

    /**
     * 发送消息 - 保存用户消息并创建或更新上下文
     *
     * @param sessionId 会话id
     * @param userId    用户id
     * @param message   消息内容
     * @param modelName 模型名称
     * @return 保存的用户消息实体
     */
    public MessageEntity sendMessage(String sessionId, String userId, String message, String modelName) {
        return conversationDomainService.sendMessage(sessionId, userId, message, modelName);
    }


    /**
     * 获取会话中的消息列表
     *
     * @param sessionId 会话id
     * @param userId    用户id
     * @return 消息列表
     */
    public List<MessageDTO> getConversationMessages(String sessionId, String userId) {

        // 查询对应会话是否存在
        SessionEntity sessionEntity = sessionDomainService.find(sessionId, userId);

        if (sessionEntity == null){
            throw new BusinessException("会话不存在");
        }

        return conversationDomainService.getConversationMessages(sessionId);
    }
}