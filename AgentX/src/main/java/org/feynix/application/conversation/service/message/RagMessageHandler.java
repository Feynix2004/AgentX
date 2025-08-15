package org.feynix.application.conversation.service.message;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.feynix.application.conversation.dto.AgentChatResponse;
import org.feynix.application.conversation.service.handler.context.ChatContext;
import org.feynix.application.conversation.service.message.agent.tool.RagToolManager;
import org.feynix.application.conversation.service.message.rag.RagChatContext;
import org.feynix.application.conversation.service.message.rag.RagRetrievalResult;
import org.feynix.application.conversation.dto.RagRetrievalDocumentDTO;
import org.feynix.application.rag.dto.DocumentUnitDTO;
import org.feynix.domain.rag.model.DocumentUnitEntity;
import org.feynix.domain.rag.model.FileDetailEntity;
import org.feynix.domain.rag.model.UserRagFileEntity;
import org.feynix.domain.rag.repository.FileDetailRepository;
import org.feynix.domain.rag.repository.UserRagFileRepository;
import org.feynix.application.rag.assembler.DocumentUnitAssembler;
import org.feynix.application.rag.service.RagQaDatasetAppService;
import org.feynix.domain.agent.model.AgentEntity;
import org.feynix.domain.conversation.constant.MessageType;
import org.feynix.domain.conversation.model.MessageEntity;
import org.feynix.domain.conversation.service.MessageDomainService;
import org.feynix.domain.conversation.service.SessionDomainService;
import org.feynix.domain.llm.service.HighAvailabilityDomainService;
import org.feynix.domain.llm.service.LLMDomainService;
import org.feynix.domain.user.service.UserSettingsDomainService;
import org.feynix.domain.user.service.AccountDomainService;
import org.feynix.infrastructure.llm.LLMServiceFactory;
import org.feynix.infrastructure.transport.MessageTransport;
import org.feynix.application.billing.service.BillingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/** RAG专用的消息处理器
 * 继承AbstractMessageHandler，添加RAG检索和问答的特定逻辑 */
@Component("ragMessageHandler")
public class RagMessageHandler extends AbstractMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(RagMessageHandler.class);

    private final RagQaDatasetAppService ragQaDatasetAppService;
    private final ObjectMapper objectMapper;
    private final FileDetailRepository fileDetailRepository;
    private final UserRagFileRepository userRagFileRepository;

    public RagMessageHandler(LLMServiceFactory llmServiceFactory, MessageDomainService messageDomainService,
            HighAvailabilityDomainService highAvailabilityDomainService, SessionDomainService sessionDomainService,
            UserSettingsDomainService userSettingsDomainService, LLMDomainService llmDomainService,
            RagToolManager ragToolManager, BillingService billingService, AccountDomainService accountDomainService,
            RagQaDatasetAppService ragQaDatasetAppService, ObjectMapper objectMapper,
            FileDetailRepository fileDetailRepository, UserRagFileRepository userRagFileRepository) {
        super(llmServiceFactory, messageDomainService, highAvailabilityDomainService, sessionDomainService,
                userSettingsDomainService, llmDomainService, ragToolManager, billingService, accountDomainService);
        this.ragQaDatasetAppService = ragQaDatasetAppService;
        this.objectMapper = objectMapper;
        this.fileDetailRepository = fileDetailRepository;
        this.userRagFileRepository = userRagFileRepository;
    }

    /** 重写流式聊天处理，添加RAG检索逻辑 */
    @Override
    protected <T> void processStreamingChat(ChatContext chatContext, T connection, MessageTransport<T> transport,
            MessageEntity userEntity, MessageEntity llmEntity, MessageWindowChatMemory memory,
            ToolProvider toolProvider) {

        // 检查是否是RAG上下文
        if (!(chatContext instanceof RagChatContext)) {
            throw new IllegalArgumentException("RagMessageHandler requires RagChatContext");
        }

        RagChatContext ragContext = (RagChatContext) chatContext;

        try {
            // 第一阶段：RAG检索
            RagRetrievalResult retrievalResult = performRagRetrieval(ragContext, transport, connection);

            // 第二阶段：基于检索结果生成回答
            generateRagAnswer(ragContext, retrievalResult, connection, transport, userEntity, llmEntity, memory,
                    toolProvider);

        } catch (Exception e) {
            logger.error("RAG流式处理失败", e);
            AgentChatResponse errorResponse = AgentChatResponse.buildEndMessage("处理过程中发生错误: " + e.getMessage(),
                    MessageType.TEXT);
            transport.sendMessage(connection, errorResponse);
        }
    }

    /** 执行RAG检索
     * @param ragContext RAG聊天上下文
     * @param transport 消息传输
     * @param connection 连接
     * @return 检索结果 */
    private <T> RagRetrievalResult performRagRetrieval(RagChatContext ragContext, MessageTransport<T> transport,
            T connection) {
        try {
            // 发送检索开始信号
            transport.sendMessage(connection,
                    AgentChatResponse.build("开始检索相关文档...", MessageType.RAG_RETRIEVAL_START));
            Thread.sleep(500);

            // 执行RAG检索 - 直接获取Entity以保留真实数据
            List<DocumentUnitEntity> retrievedEntities;
            if (ragContext.getUserRagId() != null) {
                // 基于已安装知识库检索
                retrievedEntities = ragQaDatasetAppService.performRagSearchByUserRag(ragContext.getRagSearchRequest(),
                        ragContext.getUserRagId(), ragContext.getUserId());
            } else {
                // 基于数据集ID检索
                retrievedEntities = ragQaDatasetAppService.performRagSearch(ragContext.getRagSearchRequest(),
                        ragContext.getUserId());
            }

            // 转换为轻量级DTO用于前端展示（包含真实数据）
            List<RagRetrievalDocumentDTO> lightweightDocuments = convertEntitiesToLightweightDTOs(retrievedEntities, ragContext.getUserRagId() != null);
            
            // 转换为DocumentUnitDTO用于答案生成
            List<DocumentUnitDTO> fullRetrievedDocuments = convertEntitiesToDTOs(retrievedEntities);

            // 构建检索结果响应
            String retrievalMessage = String.format("检索完成，找到 %d 个相关文档", lightweightDocuments.size());
            AgentChatResponse retrievalEndResponse = AgentChatResponse.build(retrievalMessage,
                    MessageType.RAG_RETRIEVAL_END);

            // 设置轻量级文档作为payload（优化传输）
            try {
                retrievalEndResponse.setPayload(objectMapper.writeValueAsString(lightweightDocuments));
            } catch (Exception e) {
                logger.error("序列化检索文档失败", e);
            }

            transport.sendMessage(connection, retrievalEndResponse);
            Thread.sleep(500);

            // 返回包含完整数据的结果用于答案生成
            return new RagRetrievalResult(fullRetrievedDocuments, retrievalMessage);

        } catch (Exception e) {
            logger.error("RAG检索失败", e);
            transport.sendMessage(connection, AgentChatResponse.build("文档检索失败: " + e.getMessage(), MessageType.TEXT));
            return new RagRetrievalResult(Collections.emptyList(), "检索失败");
        }
    }

    /** 基于检索结果生成回答
     * @param ragContext RAG聊天上下文
     * @param retrievalResult 检索结果
     * @param connection 连接
     * @param transport 消息传输
     * @param userEntity 用户消息实体
     * @param llmEntity LLM消息实体
     * @param memory 聊天内存
     * @param toolProvider 工具提供者 */
    private <T> void generateRagAnswer(RagChatContext ragContext, RagRetrievalResult retrievalResult, T connection,
            MessageTransport<T> transport, MessageEntity userEntity, MessageEntity llmEntity,
            MessageWindowChatMemory memory, ToolProvider toolProvider) {

        // 发送回答生成开始信号
        transport.sendMessage(connection, AgentChatResponse.build("开始生成回答...", MessageType.RAG_ANSWER_START));

        // 构建RAG提示词
        String ragPrompt = buildRagPrompt(ragContext.getUserMessage(), retrievalResult.getRetrievedDocuments());

        // 保存用户消息
        messageDomainService.saveMessageAndUpdateContext(Collections.singletonList(userEntity),
                ragContext.getContextEntity());

        // 获取流式LLM客户端
        StreamingChatModel streamingClient = llmServiceFactory.getStreamingClient(ragContext.getProvider(),
                ragContext.getModel());

        // 创建RAG专用的流式Agent
        Agent agent = buildRagStreamingAgent(streamingClient, memory, toolProvider, ragContext.getAgent());

        // 启动流式处理
        processRagChat(agent, connection, transport, ragContext, userEntity, llmEntity, ragPrompt);
    }

    /** RAG专用的聊天处理逻辑 */
    private <T> void processRagChat(Agent agent, T connection, MessageTransport<T> transport, RagChatContext ragContext,
            MessageEntity userEntity, MessageEntity llmEntity, String ragPrompt) {

        AtomicReference<StringBuilder> messageBuilder = new AtomicReference<>(new StringBuilder());
        TokenStream tokenStream = agent.chat(ragPrompt);

        // 记录调用开始时间
        long startTime = System.currentTimeMillis();

        // 思维链状态跟踪
        final boolean[] thinkingStarted = {false};
        final boolean[] thinkingEnded = {false};
        final boolean[] hasThinkingProcess = {false};

        // 错误处理
        tokenStream.onError(throwable -> {
            transport.sendMessage(connection,
                    AgentChatResponse.buildEndMessage(throwable.getMessage(), MessageType.TEXT));

            // 上报调用失败结果
            long latency = System.currentTimeMillis() - startTime;
            highAvailabilityDomainService.reportCallResult(ragContext.getInstanceId(), ragContext.getModel().getId(),
                    false, latency, throwable.getMessage());
        });

        // 部分回答处理
        tokenStream.onPartialResponse(fragment -> {
            // 如果有思考过程但还没结束思考，先结束思考阶段
            if (hasThinkingProcess[0] && !thinkingEnded[0]) {
                transport.sendMessage(connection, AgentChatResponse.build("思考完成", MessageType.RAG_THINKING_END));
                thinkingEnded[0] = true;
            }

            // 如果没有思考过程且还没开始过思考，先发送思考开始和结束
            if (!hasThinkingProcess[0] && !thinkingStarted[0]) {
                transport.sendMessage(connection, AgentChatResponse.build("开始思考...", MessageType.RAG_THINKING_START));
                transport.sendMessage(connection, AgentChatResponse.build("思考完成", MessageType.RAG_THINKING_END));
                thinkingStarted[0] = true;
                thinkingEnded[0] = true;
            }

            messageBuilder.get().append(fragment);
            transport.sendMessage(connection, AgentChatResponse.build(fragment, MessageType.RAG_ANSWER_PROGRESS));
        });

        // 思维链处理
        tokenStream.onPartialReasoning(reasoning -> {
            hasThinkingProcess[0] = true;
            if (!thinkingStarted[0]) {
                transport.sendMessage(connection, AgentChatResponse.build("开始思考...", MessageType.RAG_THINKING_START));
                thinkingStarted[0] = true;
            }
            transport.sendMessage(connection, AgentChatResponse.build(reasoning, MessageType.RAG_THINKING_PROGRESS));
        });

        // 完整响应处理
        tokenStream.onCompleteResponse(chatResponse -> {
            this.setMessageTokenCount(ragContext.getMessageHistory(), userEntity, llmEntity, chatResponse);

            messageDomainService.updateMessage(userEntity);
            messageDomainService.saveMessageAndUpdateContext(Collections.singletonList(llmEntity),
                    ragContext.getContextEntity());

            // 发送RAG回答结束信号
            transport.sendMessage(connection, AgentChatResponse.buildEndMessage("回答生成完成", MessageType.RAG_ANSWER_END));

            // 上报调用成功结果
            long latency = System.currentTimeMillis() - startTime;
            highAvailabilityDomainService.reportCallResult(ragContext.getInstanceId(), ragContext.getModel().getId(),
                    true, latency, null);

            // 执行模型调用计费
            performBillingWithErrorHandling(ragContext, chatResponse.tokenUsage().inputTokenCount(),
                    chatResponse.tokenUsage().outputTokenCount(), transport, connection);

            smartRenameSession(ragContext);
        });

        // 启动流处理
        tokenStream.start();
    }

    /** 将DocumentUnitEntity转换为轻量级展示DTO（包含真实数据）
     * @param entities 文档实体列表
     * @param isUserRag 是否为用户RAG模式（影响文件名查询方式）
     * @return 轻量级检索结果DTO列表 */
    private List<RagRetrievalDocumentDTO> convertEntitiesToLightweightDTOs(List<DocumentUnitEntity> entities, boolean isUserRag) {
        List<RagRetrievalDocumentDTO> lightweightDTOs = new ArrayList<>();
        
        for (DocumentUnitEntity entity : entities) {
            try {
                // 🎯 获取真实相似度分数
                Double realScore = entity.getSimilarityScore() != null ? entity.getSimilarityScore() : 0.0;
                
                // 🎯 获取真实文件名
                String realFileName = getRealFileName(entity.getFileId(), isUserRag);
                
                // ✅ 创建包含真实数据的轻量级DTO
                RagRetrievalDocumentDTO lightweightDTO = new RagRetrievalDocumentDTO(
                    entity.getFileId(),
                    realFileName,
                    entity.getId(),  // documentId
                    realScore,       // 真实的相似度分数
                    entity.getPage()
                );
                
                lightweightDTOs.add(lightweightDTO);
                
            } catch (Exception e) {
                logger.warn("转换轻量级DTO失败，文档ID: {}", entity.getId(), e);
                // 出错时使用默认值，但仍尽量保留真实分数
                Double score = entity.getSimilarityScore() != null ? entity.getSimilarityScore() : 0.0;
                RagRetrievalDocumentDTO lightweightDTO = new RagRetrievalDocumentDTO(
                    entity.getFileId(),
                    "未知文件",
                    entity.getId(),
                    score,
                    entity.getPage()
                );
                lightweightDTOs.add(lightweightDTO);
            }
        }
        
        return lightweightDTOs;
    }
    
    /** 获取真实文件名
     * @param fileId 文件ID
     * @param isUserRag 是否为用户RAG模式
     * @return 真实文件名 */
    private String getRealFileName(String fileId, boolean isUserRag) {
        try {
            if (isUserRag) {
                // SNAPSHOT模式：查询UserRagFileEntity
                UserRagFileEntity userFile = userRagFileRepository.selectById(fileId);
                return userFile != null ? userFile.getFileName() : "未知文件";
            } else {
                // REFERENCE模式：查询FileDetailEntity  
                FileDetailEntity fileDetail = fileDetailRepository.selectById(fileId);
                return fileDetail != null ? fileDetail.getOriginalFilename() : "未知文件";
            }
        } catch (Exception e) {
            logger.warn("查询文件名失败，fileId: {}, isUserRag: {}, 错误: {}", fileId, isUserRag, e.getMessage());
            return "未知文件";
        }
    }
    
    /** 将DocumentUnitEntity转换为DocumentUnitDTO用于答案生成
     * @param entities 文档实体列表
     * @return DocumentUnitDTO列表 */
    private List<DocumentUnitDTO> convertEntitiesToDTOs(List<DocumentUnitEntity> entities) {
        return DocumentUnitAssembler.toDTOs(entities);
    }

    /** 构建RAG提示词
     * @param question 用户问题
     * @param documents 检索到的文档
     * @return RAG提示词 */
    private String buildRagPrompt(String question, List<DocumentUnitDTO> documents) {
        if (documents.isEmpty()) {
            return String.format("用户问题：%s\n\n暂无相关文档信息，请基于你的知识回答。", question);
        }

        StringBuilder context = new StringBuilder();
        context.append("以下是相关的文档片段：\n\n");

        for (int i = 0; i < documents.size(); i++) {
            DocumentUnitDTO doc = documents.get(i);
            context.append(String.format("文档片段 %d：\n", i + 1));
            context.append(doc.getContent());
            context.append("\n\n");
        }

        return String.format(
                "请基于以下提供的文档内容回答用户的问题。如果文档中没有相关信息，请诚实地告知用户。\n\n" + "文档内容：\n%s\n\n" + "用户问题：%s\n\n" + "请提供准确、有帮助的回答：",
                context.toString(), question);
    }

    /** 构建RAG专用的流式Agent */
    private Agent buildRagStreamingAgent(StreamingChatModel model, MessageWindowChatMemory memory,
            ToolProvider toolProvider, AgentEntity agent) {

        // 为RAG对话添加专用的系统提示词
        MessageWindowChatMemory ragMemory = MessageWindowChatMemory.builder().maxMessages(1000)
                .chatMemoryStore(new InMemoryChatMemoryStore()).build();

        // 复制原有内存内容
        memory.messages().forEach(ragMemory::add);

        // 添加RAG专用系统提示词
        ragMemory.add(new SystemMessage("""
                你是一位专业的文档问答助手，你的任务是基于提供的文档回答用户问题。
                你需要遵循以下Markdown格式要求：
                1. 使用标准Markdown语法
                2. 列表项使用 ' - ' 而不是 '*'，确保破折号后有一个空格
                3. 引用页码使用方括号，例如：[页码: 1]
                4. 在每个主要段落之间添加一个空行
                5. 加粗使用 **文本** 格式
                6. 保持一致的缩进，列表项不要过度缩进
                7. 确保列表项之间没有多余的空行
                8. 该加## 这种标题的时候要加上

                回答结构应该是：
                1. 首先是简短的介绍语
                2. 然后是主要内容（使用列表形式）
                3. 最后是"信息来源"部分，总结使用的页面及其贡献
                """));

        return buildStreamingAgent(model, ragMemory, toolProvider, agent);
    }

    /** 设置消息Token计数（调用父类方法） */
    private void setMessageTokenCount(List<MessageEntity> historyMessages, MessageEntity userEntity,
            MessageEntity llmEntity, dev.langchain4j.model.chat.response.ChatResponse chatResponse) {
        // 调用父类AbstractMessageHandler中的方法
        llmEntity.setTokenCount(chatResponse.tokenUsage().outputTokenCount());
        llmEntity.setBodyTokenCount(chatResponse.tokenUsage().outputTokenCount());
        llmEntity.setContent(chatResponse.aiMessage().text());

        int bodyTokenSum = 0;
        if (historyMessages != null && !historyMessages.isEmpty()) {
            bodyTokenSum = historyMessages.stream().filter(java.util.Objects::nonNull)
                    .mapToInt(MessageEntity::getBodyTokenCount).sum();
        }
        userEntity.setTokenCount(chatResponse.tokenUsage().inputTokenCount());
        userEntity.setBodyTokenCount(chatResponse.tokenUsage().inputTokenCount() - bodyTokenSum);
    }
}