package org.feynix.application.conversation.service.message.rag;

import org.feynix.application.conversation.service.handler.context.ChatContext;
import org.feynix.application.rag.dto.RagSearchRequest;

/** RAG专用的聊天上下文 继承ChatContext，添加RAG特定的配置 */
public class RagChatContext extends ChatContext {

    /** RAG搜索请求配置 */
    private RagSearchRequest ragSearchRequest;

    /** 用户RAG ID（已安装的知识库ID，可选） */
    private String userRagId;

    /** 文件ID（可选，用于单文件检索） */
    private String fileId;

    public RagSearchRequest getRagSearchRequest() {
        return ragSearchRequest;
    }

    public void setRagSearchRequest(RagSearchRequest ragSearchRequest) {
        this.ragSearchRequest = ragSearchRequest;
    }

    public String getUserRagId() {
        return userRagId;
    }

    public void setUserRagId(String userRagId) {
        this.userRagId = userRagId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    /** 构建器模式创建RagChatContext */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private RagChatContext context = new RagChatContext();

        public Builder sessionId(String sessionId) {
            context.setSessionId(sessionId);
            return this;
        }

        public Builder userId(String userId) {
            context.setUserId(userId);
            return this;
        }

        public Builder userMessage(String userMessage) {
            context.setUserMessage(userMessage);
            return this;
        }

        public Builder ragSearchRequest(RagSearchRequest ragSearchRequest) {
            context.setRagSearchRequest(ragSearchRequest);
            return this;
        }

        public Builder userRagId(String userRagId) {
            context.setUserRagId(userRagId);
            return this;
        }

        public Builder fileId(String fileId) {
            context.setFileId(fileId);
            return this;
        }

        public Builder agent(org.feynix.domain.agent.model.AgentEntity agent) {
            context.setAgent(agent);
            return this;
        }

        public Builder model(org.feynix.domain.llm.model.ModelEntity model) {
            context.setModel(model);
            return this;
        }

        public Builder provider(org.feynix.domain.llm.model.ProviderEntity provider) {
            context.setProvider(provider);
            return this;
        }

        public Builder contextEntity(org.feynix.domain.conversation.model.ContextEntity contextEntity) {
            context.setContextEntity(contextEntity);
            return this;
        }

        public Builder messageHistory(java.util.List<org.feynix.domain.conversation.model.MessageEntity> messageHistory) {
            context.setMessageHistory(messageHistory);
            return this;
        }

        public Builder instanceId(String instanceId) {
            context.setInstanceId(instanceId);
            return this;
        }

        public Builder streaming(boolean streaming) {
            context.setStreaming(streaming);
            return this;
        }

        public RagChatContext build() {
            return context;
        }
    }
}