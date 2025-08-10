package org.feynix.application.conversation.service.message.agent;

import dev.langchain4j.service.tool.ToolProvider;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.feynix.application.conversation.service.handler.context.ChatContext;
import org.feynix.application.conversation.service.message.AbstractMessageHandler;
import org.feynix.application.conversation.service.message.TracingMessageHandler;
import org.feynix.application.conversation.service.message.agent.tool.RagToolManager;
import org.feynix.application.trace.collector.TraceCollector;
import org.feynix.domain.conversation.service.MessageDomainService;
import org.feynix.domain.conversation.service.SessionDomainService;
import org.feynix.domain.llm.service.HighAvailabilityDomainService;
import org.feynix.domain.llm.service.LLMDomainService;
import org.feynix.domain.user.service.UserSettingsDomainService;
import org.feynix.infrastructure.llm.LLMServiceFactory;
import org.feynix.application.billing.service.BillingService;
import org.feynix.domain.user.service.AccountDomainService;

/** Agent消息处理器 用于支持工具调用的对话模式 实现任务拆分、执行和结果汇总的工作流 使用事件驱动架构进行状态转换 */
@Component(value = "agentMessageHandler")
public class AgentMessageHandler extends TracingMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(AgentMessageHandler.class);
    private AgentToolManager agentToolManager;

    public AgentMessageHandler(LLMServiceFactory llmServiceFactory, MessageDomainService messageDomainService,
            HighAvailabilityDomainService highAvailabilityDomainService, SessionDomainService sessionDomainService,
            UserSettingsDomainService userSettingsDomainService, LLMDomainService llmDomainService,
            RagToolManager ragToolManager, BillingService billingService, AccountDomainService accountDomainService,
            TraceCollector traceCollector, AgentToolManager agentToolManager) {
        super(llmServiceFactory, messageDomainService, highAvailabilityDomainService, sessionDomainService,
                userSettingsDomainService, llmDomainService, ragToolManager, billingService, accountDomainService,
                traceCollector);
        this.agentToolManager = agentToolManager;
    }

    @Override
    protected ToolProvider provideTools(ChatContext chatContext) {
        // 关键改造：传递用户ID给工具管理器
        return agentToolManager.createToolProvider(agentToolManager.getAvailableTools(chatContext),
                chatContext.getAgent().getToolPresetParams(), chatContext.getUserId() // 新增：传递用户ID
        );
    }
}