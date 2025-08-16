package org.feynix.application.conversation.service.handler;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.feynix.application.conversation.dto.ChatRequest;
import org.feynix.application.conversation.dto.RagChatRequest;
import org.feynix.application.conversation.service.message.AbstractMessageHandler;
import org.feynix.domain.agent.model.AgentEntity;
import org.feynix.domain.agent.model.AgentWidgetEntity;

/** 消息处理器类型枚举 */
enum MessageHandlerType {
    STANDARD, AGENT
}

/** 消息处理器工厂 根据智能体类型选择适合的消息处理器 */
@Component
public class MessageHandlerFactory {

    private final ApplicationContext applicationContext;

    public MessageHandlerFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /** 根据请求类型获取合适的消息处理器
     * 
     * @param request 聊天请求
     * @return 消息处理器 */
    public AbstractMessageHandler getHandler(ChatRequest request) {
        if (request instanceof RagChatRequest) {
            return applicationContext.getBean("ragMessageHandler", AbstractMessageHandler.class);
        }

        // 默认使用标准Agent消息处理器
        return applicationContext.getBean("agentMessageHandler", AbstractMessageHandler.class);
    }

    /** 根据智能体获取合适的消息处理器
     * @deprecated 使用 getHandler(ChatRequest) 替代
     * @param agent 智能体实体
     * @return 消息处理器 */
    @Deprecated
    public AbstractMessageHandler getHandler(AgentEntity agent) {
        // 统一使用标准消息处理器
        return getHandlerByType(MessageHandlerType.STANDARD);
    }

    /** 根据智能体和Widget配置获取合适的消息处理器 支持根据Widget类型选择不同的处理器
     * 
     * @param agent 智能体实体
     * @param widget Widget配置实体（可为null）
     * @return 消息处理器 */
    public AbstractMessageHandler getHandler(AgentEntity agent, AgentWidgetEntity widget) {
        // 如果是RAG类型的Widget，直接使用RagMessageHandler
        if (widget != null && widget.isRagWidget()) {
            return applicationContext.getBean("ragMessageHandler", AbstractMessageHandler.class);
        }

        // 其他情况使用标准的Agent消息处理器
        return applicationContext.getBean("agentMessageHandler", AbstractMessageHandler.class);
    }

    /** 根据处理器类型获取对应的处理器实例
     * 
     * @param type 处理器类型
     * @return 消息处理器 */
    private AbstractMessageHandler getHandlerByType(MessageHandlerType type) {
        return applicationContext.getBean("agentMessageHandler", AbstractMessageHandler.class);
    }
}