package org.feynix.application.conversation.dto;

/** RAG检索结果文档DTO - 专门用于检索结果展示 轻量级设计，只包含检索结果展示所需的关键信息 与前端期望的 RetrievedFileInfo 接口完全匹配 */
public class RagRetrievalDocumentDTO {

    /** 文件ID */
    private String fileId;

    /** 文件名 */
    private String fileName;

    /** 文档ID */
    private String documentId;

    /** 相似度分数 */
    private Double score;

    /** 页码（可选，用于详情展示） */
    private Integer page;

    public RagRetrievalDocumentDTO() {
    }

    public RagRetrievalDocumentDTO(String fileId, String fileName, String documentId, Double score) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.documentId = documentId;
        this.score = score;
    }

    public RagRetrievalDocumentDTO(String fileId, String fileName, String documentId, Double score, Integer page) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.documentId = documentId;
        this.score = score;
        this.page = page;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }
}