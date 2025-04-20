package org.feynix.application.conversation.service.message.chat;

import org.springframework.stereotype.Component;
import org.feynix.application.conversation.service.message.AbstractMessageHandler;
import org.feynix.domain.conversation.service.MessageDomainService;
import org.feynix.infrastructure.llm.LLMServiceFactory;

/**
 * 标准消息处理器
 */
@Component(value = "chatMessageHandler")
public class ChatMessageHandler extends AbstractMessageHandler {

    public ChatMessageHandler(
            LLMServiceFactory llmServiceFactory,
            MessageDomainService messageDomainService) {
        super(llmServiceFactory, messageDomainService);
    }
}