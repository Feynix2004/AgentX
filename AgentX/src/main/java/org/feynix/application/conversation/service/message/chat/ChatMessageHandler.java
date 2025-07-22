package org.feynix.application.conversation.service.message.chat;

import org.springframework.stereotype.Component;
import org.feynix.application.conversation.service.message.AbstractMessageHandler;
import org.feynix.application.conversation.service.message.agent.tool.RagToolManager;
import org.feynix.domain.conversation.service.MessageDomainService;
import org.feynix.domain.conversation.service.SessionDomainService;
import org.feynix.domain.llm.service.HighAvailabilityDomainService;
import org.feynix.domain.llm.service.LLMDomainService;
import org.feynix.domain.user.service.UserSettingsDomainService;
import org.feynix.infrastructure.llm.LLMServiceFactory;

/** 标准消息处理器 */
@Component(value = "chatMessageHandler")
public class ChatMessageHandler extends AbstractMessageHandler {


    public ChatMessageHandler(LLMServiceFactory llmServiceFactory, MessageDomainService messageDomainService,
                              HighAvailabilityDomainService highAvailabilityDomainService, SessionDomainService sessionDomainService,
                              UserSettingsDomainService userSettingsDomainService, LLMDomainService llmDomainService, RagToolManager ragToolManager) {
        super(llmServiceFactory, messageDomainService, highAvailabilityDomainService, sessionDomainService,
                userSettingsDomainService, llmDomainService,ragToolManager);
    }
}