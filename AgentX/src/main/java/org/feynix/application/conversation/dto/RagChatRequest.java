package org.feynix.application.conversation.dto;

import org.feynix.application.rag.dto.RagSearchRequest;
import org.feynix.application.rag.dto.RagStreamChatRequest;

import java.util.List;

/** RAG专用对话请求 继承 ChatRequest，添加RAG特有的参数 */
public class RagChatRequest extends ChatRequest {

    /** 数据集ID列表（当fileId为空时使用） */
    private List<String> datasetIds;

    /** 用户RAG ID（已安装的知识库ID） */
    private String userRagId;

    /** 指定文件ID（优先级高于datasetIds） */
    private String fileId;

    /** RAG搜索配置 - 最大检索结果数 */
    private Integer maxResults = 5;

    /** RAG搜索配置 - 最小相似度阈值 */
    private Double minScore = 0.7;

    /** RAG搜索配置 - 是否启用重排序 */
    private Boolean enableRerank = true;

    /** 从 RagStreamChatRequest 转换的工厂方法
     * @param request RAG流式聊天请求
     * @param sessionId 会话ID
     * @return RAG对话请求 */
    public static RagChatRequest fromRagStreamChatRequest(RagStreamChatRequest request, String sessionId) {
        RagChatRequest ragRequest = new RagChatRequest();
        ragRequest.setMessage(request.getQuestion());
        ragRequest.setSessionId(sessionId);
        ragRequest.setDatasetIds(request.getDatasetIds());
        ragRequest.setFileId(request.getFileId());
        ragRequest.setMaxResults(request.getMaxResults());
        ragRequest.setMinScore(request.getMinScore());
        ragRequest.setEnableRerank(request.getEnableRerank());
        return ragRequest;
    }

    /** 从 RagStreamChatRequest 转换的工厂方法（支持userRagId）
     * @param request RAG流式聊天请求
     * @param userRagId 用户RAG ID
     * @param sessionId 会话ID
     * @return RAG对话请求 */
    public static RagChatRequest fromRagStreamChatRequestWithUserRag(RagStreamChatRequest request, String userRagId,
            String sessionId) {
        RagChatRequest ragRequest = fromRagStreamChatRequest(request, sessionId);
        ragRequest.setUserRagId(userRagId);
        return ragRequest;
    }

    /** 转换为 RagSearchRequest
     * @return RAG搜索请求 */
    public RagSearchRequest toRagSearchRequest() {
        RagSearchRequest ragSearchRequest = new RagSearchRequest();
        ragSearchRequest.setQuestion(this.getMessage());
        ragSearchRequest.setDatasetIds(this.datasetIds);
        ragSearchRequest.setMaxResults(this.maxResults);
        ragSearchRequest.setMinScore(this.minScore);
        ragSearchRequest.setEnableRerank(this.enableRerank);
        return ragSearchRequest;
    }

    public List<String> getDatasetIds() {
        return datasetIds;
    }

    public void setDatasetIds(List<String> datasetIds) {
        this.datasetIds = datasetIds;
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

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public Double getMinScore() {
        return minScore;
    }

    public void setMinScore(Double minScore) {
        this.minScore = minScore;
    }

    public Boolean getEnableRerank() {
        return enableRerank;
    }

    public void setEnableRerank(Boolean enableRerank) {
        this.enableRerank = enableRerank;
    }
}