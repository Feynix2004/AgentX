package org.feynix.application.conversation.service.message.chat;

import org.springframework.stereotype.Component;
import org.feynix.application.conversation.service.message.AbstractMessageHandler;
import org.feynix.domain.conversation.service.MessageDomainService;
import org.feynix.domain.llm.service.HighAvailabilityDomainService;
import org.feynix.infrastructure.llm.LLMServiceFactory;

/** 标准消息处理器 */
@Component(value = "chatMessageHandler")
public class ChatMessageHandler extends AbstractMessageHandler {

    protected final HighAvailabilityDomainService highAvailabilityDomainService;

    public ChatMessageHandler(LLMServiceFactory llmServiceFactory, MessageDomainService messageDomainService, HighAvailabilityDomainService highAvailabilityDomainService) {
        super(llmServiceFactory, messageDomainService,highAvailabilityDomainService);
        this.highAvailabilityDomainService = highAvailabilityDomainService;
    }
}