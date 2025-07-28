package org.feynix.application.conversation.service.message.chat;

import org.springframework.stereotype.Component;
import org.feynix.application.conversation.service.message.AbstractMessageHandler;
import org.feynix.domain.conversation.service.MessageDomainService;
import org.feynix.domain.conversation.service.SessionDomainService;
import org.feynix.domain.llm.service.HighAvailabilityDomainService;
import org.feynix.domain.llm.service.LLMDomainService;
import org.feynix.domain.user.service.UserSettingsDomainService;
import org.feynix.infrastructure.llm.LLMServiceFactory;
import org.feynix.application.billing.service.BillingService;
import org.feynix.domain.user.service.AccountDomainService;

/** 标准消息处理器 */
@Component(value = "chatMessageHandler")
public class ChatMessageHandler extends AbstractMessageHandler {

    protected final HighAvailabilityDomainService highAvailabilityDomainService;
    protected final SessionDomainService sessionDomainService;
    protected final UserSettingsDomainService userSettingsDomainService;
    protected final LLMDomainService llmDomainService;

    public ChatMessageHandler(LLMServiceFactory llmServiceFactory, MessageDomainService messageDomainService,
            HighAvailabilityDomainService highAvailabilityDomainService, SessionDomainService sessionDomainService,
            UserSettingsDomainService userSettingsDomainService, LLMDomainService llmDomainService,
            BillingService billingService, AccountDomainService accountDomainService) {
        super(llmServiceFactory, messageDomainService, highAvailabilityDomainService, sessionDomainService,
                userSettingsDomainService, llmDomainService, billingService, accountDomainService);
        this.highAvailabilityDomainService = highAvailabilityDomainService;
        this.sessionDomainService = sessionDomainService;
        this.userSettingsDomainService = userSettingsDomainService;
        this.llmDomainService = llmDomainService;
    }
}