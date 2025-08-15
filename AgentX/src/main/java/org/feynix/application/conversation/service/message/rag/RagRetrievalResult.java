package org.feynix.application.conversation.service.message.rag;

import org.feynix.application.rag.dto.DocumentUnitDTO;

import java.util.List;

/** RAG检索结果
 * 封装文档检索的结果信息 */
public class RagRetrievalResult {

    /** 检索到的文档列表 */
    private List<DocumentUnitDTO> retrievedDocuments;

    /** 检索结果描述 */
    private String resultMessage;

    /** 检索是否成功 */
    private boolean success;

    /** 检索耗时（毫秒） */
    private long retrievalTime;

    public RagRetrievalResult() {
    }

    public RagRetrievalResult(List<DocumentUnitDTO> retrievedDocuments, String resultMessage) {
        this.retrievedDocuments = retrievedDocuments;
        this.resultMessage = resultMessage;
        this.success = true;
    }

    public RagRetrievalResult(List<DocumentUnitDTO> retrievedDocuments, String resultMessage, boolean success) {
        this.retrievedDocuments = retrievedDocuments;
        this.resultMessage = resultMessage;
        this.success = success;
    }

    public List<DocumentUnitDTO> getRetrievedDocuments() {
        return retrievedDocuments;
    }

    public void setRetrievedDocuments(List<DocumentUnitDTO> retrievedDocuments) {
        this.retrievedDocuments = retrievedDocuments;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getRetrievalTime() {
        return retrievalTime;
    }

    public void setRetrievalTime(long retrievalTime) {
        this.retrievalTime = retrievalTime;
    }

    /** 判断是否有检索到的文档 */
    public boolean hasDocuments() {
        return retrievedDocuments != null && !retrievedDocuments.isEmpty();
    }

    /** 获取检索到的文档数量 */
    public int getDocumentCount() {
        return retrievedDocuments != null ? retrievedDocuments.size() : 0;
    }
}