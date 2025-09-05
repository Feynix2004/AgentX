package org.feynix.application.conversation.service.message.builtin.rag;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.feynix.application.conversation.service.message.builtin.AbstractBuiltInToolProvider;
import org.feynix.application.conversation.service.message.builtin.BuiltInTool;
import org.feynix.application.conversation.service.message.builtin.ToolDefinition;
import org.feynix.application.rag.service.search.RAGSearchAppService;
import org.feynix.application.rag.dto.DocumentUnitDTO;
import org.feynix.application.rag.dto.RagSearchRequest;
import org.feynix.domain.agent.model.AgentEntity;
import org.feynix.domain.rag.service.management.UserRagDomainService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** RAG内置工具提供者
 * 
 * 负责创建和管理Agent的RAG工具，支持多知识库集成 使用融合架构：工具定义、规范创建、执行逻辑都在这一个类中 */
@BuiltInTool(name = "rag_search", description = "知识库检索工具，支持在用户配置的知识库中进行智能搜索", priority = 10)
public class RagBuiltInToolProvider extends AbstractBuiltInToolProvider {

    private static final Logger log = LoggerFactory.getLogger(RagBuiltInToolProvider.class);

    private final RAGSearchAppService ragSearchAppService;
    private final UserRagDomainService userRagDomainService;

    public RagBuiltInToolProvider(RAGSearchAppService ragSearchAppService, UserRagDomainService userRagDomainService) {
        this.ragSearchAppService = ragSearchAppService;
        this.userRagDomainService = userRagDomainService;
    }

    /** 实现融合架构：定义RAG工具规范 替代原来的RagToolSpecification类 */
    @Override
    public List<ToolDefinition> defineTools(AgentEntity agent) {
        List<String> knowledgeBaseIds = agent.getKnowledgeBaseIds();

        // 如果没有配置知识库，返回空列表
        if (knowledgeBaseIds == null || knowledgeBaseIds.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // 验证知识库是否存在且用户有权限访问
            List<String> validKnowledgeBaseIds = validateKnowledgeBases(knowledgeBaseIds, agent.getUserId());

            if (validKnowledgeBaseIds.isEmpty()) {
                log.warn("Agent {} 配置的知识库都无效或无权限访问", agent.getId());
                return Collections.emptyList();
            }

            // 获取知识库名称用于工具描述
            List<String> knowledgeBaseNames = getKnowledgeBaseNames(validKnowledgeBaseIds, agent.getUserId());

            // 创建RAG工具定义
            String description = "在配置的知识库中搜索相关信息，用于回答用户问题";
            if (!knowledgeBaseNames.isEmpty()) {
                description += "。可用的知识库包括：" + String.join("、", knowledgeBaseNames);
            }

            ToolDefinition ragTool = ToolDefinition.builder().name("knowledge_search").description(description)
                    .addRequiredStringParameter("query", "搜索查询内容，描述用户想要了解的问题或关键词")
                    .addIntegerParameter("maxResults", "最大返回结果数量，默认为10，范围1-20")
                    .addNumberParameter("minScore", "最小相似度阈值，默认为0.5，范围0.0-1.0，值越高结果越精确")
                    .addBooleanParameter("enableRerank", "是否启用重排序优化，默认为true，可提高搜索结果质量").build();

            log.info("为Agent {} 定义RAG工具成功，关联知识库数量: {}", agent.getId(), validKnowledgeBaseIds.size());
            return List.of(ragTool);

        } catch (Exception e) {
            log.error("为Agent {} 定义RAG工具失败: {}", agent.getId(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /** 实现融合架构：执行RAG工具逻辑 替代原来的RagToolExecutor类 */
    @Override
    protected String doExecute(String toolName, JsonNode arguments, AgentEntity agent, Object memoryId) {
        if (!"knowledge_search".equals(toolName)) {
            return formatError("未知工具: " + toolName);
        }

        try {
            log.info("执行RAG工具搜索，agent: {}, user: {}, memoryId: {}", agent.getId(), agent.getUserId(), memoryId);

            // 获取Agent的知识库配置
            List<String> knowledgeBaseIds = agent.getKnowledgeBaseIds();
            if (knowledgeBaseIds == null || knowledgeBaseIds.isEmpty()) {
                return formatError("未配置任何知识库");
            }

            // 验证知识库权限
            List<String> validKnowledgeBaseIds = validateKnowledgeBases(knowledgeBaseIds, agent.getUserId());
            if (validKnowledgeBaseIds.isEmpty()) {
                return formatError("没有有效的知识库或无权限访问");
            }

            // 解析参数
            String query = getRequiredStringParameter(arguments, "query");
            int maxResults = getIntegerParameter(arguments, "maxResults", 10);
            double minScore = getDoubleParameter(arguments, "minScore", 0.5);
            boolean enableRerank = getBooleanParameter(arguments, "enableRerank", true);
            boolean enableQueryExpansion = getBooleanParameter(arguments, "enableQueryExpansion", false);

            // 参数验证
            validateRange(maxResults, 1, 20, "maxResults");
            validateRange(minScore, 0.0, 1.0, "minScore");

            log.debug(
                    "RAG搜索参数 - query: {}, maxResults: {}, minScore: {}, enableRerank: {}, enableQueryExpansion: {}, knowledgeBaseCount: {}",
                    query, maxResults, minScore, enableRerank, enableQueryExpansion, validKnowledgeBaseIds.size());

            // 构建RAG搜索请求，支持多个知识库
            RagSearchRequest searchRequest = new RagSearchRequest();
            searchRequest.setDatasetIds(validKnowledgeBaseIds);
            searchRequest.setQuestion(query);
            searchRequest.setMaxResults(maxResults);
            searchRequest.setMinScore(minScore);
            searchRequest.setEnableRerank(enableRerank);
            searchRequest.setEnableQueryExpansion(enableQueryExpansion);

            // 执行RAG搜索
            List<DocumentUnitDTO> searchResults = ragSearchAppService.ragSearch(searchRequest, agent.getUserId());

            if (searchResults == null || searchResults.isEmpty()) {
                log.info("RAG搜索未找到相关文档，knowledgeBaseIds: {}, query: {}", validKnowledgeBaseIds, query);
                return "未找到相关文档内容";
            }

            // 格式化搜索结果
            String formattedResults = formatSearchResults(searchResults, query);

            log.info("RAG搜索完成，knowledgeBaseIds: {}, query: {}, 找到文档数量: {}", validKnowledgeBaseIds, query,
                    searchResults.size());

            return formattedResults;

        } catch (IllegalArgumentException e) {
            // 参数错误已经由基类处理，这里重新抛出
            throw e;
        } catch (Exception e) {
            log.error("RAG工具执行失败，agent: {}, user: {}, error: {}", agent.getId(), agent.getUserId(), e.getMessage(), e);
            throw new RuntimeException("RAG搜索执行失败: " + e.getMessage(), e);
        }
    }

    /** 格式化搜索结果
     * @param searchResults 搜索结果列表
     * @param query 搜索查询
     * @return 格式化后的结果字符串 */
    private String formatSearchResults(List<DocumentUnitDTO> searchResults, String query) {
        StringBuilder result = new StringBuilder();
        result.append("根据查询「").append(query).append("」找到以下相关内容：\n\n");

        for (int i = 0; i < searchResults.size(); i++) {
            DocumentUnitDTO doc = searchResults.get(i);
            result.append("【文档片段 ").append(i + 1).append("】\n");

            // 添加文档内容
            if (StringUtils.hasText(doc.getContent())) {
                String content = doc.getContent().trim();
                // 限制单个文档片段的长度，避免响应过长
                if (content.length() > 500) {
                    content = content.substring(0, 500) + "...";
                }
                result.append(content).append("\n");
            }

            // 添加来源信息（如果有文件ID）
            if (StringUtils.hasText(doc.getFileId())) {
                result.append("来源：文件ID ").append(doc.getFileId());
                if (doc.getPage() != null) {
                    result.append("，第 ").append(doc.getPage()).append(" 页");
                }
                result.append("\n");
            }

            result.append("\n");
        }

        result.append("以上内容来自知识库，请基于这些信息回答用户问题。");

        return result.toString();
    }

    // ====== 辅助方法（保持原有逻辑不变）======

    /** 验证知识库是否存在且用户有权限访问
     * @param knowledgeBaseIds 知识库ID列表
     * @param userId 用户ID
     * @return 有效的知识库ID列表 */
    private List<String> validateKnowledgeBases(List<String> knowledgeBaseIds, String userId) {
        List<String> validIds = new ArrayList<>();

        for (String knowledgeBaseId : knowledgeBaseIds) {
            try {
                // 检查用户是否安装了这个知识库
                boolean isInstalled = userRagDomainService.isRagInstalledByOriginalId(userId, knowledgeBaseId);

                if (isInstalled) {
                    validIds.add(knowledgeBaseId);
                    log.debug("知识库 {} 验证通过，用户已安装", knowledgeBaseId);
                } else {
                    log.warn("知识库 {} 验证失败，用户 {} 未安装该知识库", knowledgeBaseId, userId);
                }
            } catch (Exception e) {
                log.warn("知识库 {} 验证失败: {}", knowledgeBaseId, e.getMessage());
            }
        }

        return validIds;
    }

    /** 获取知识库名称列表
     * @param knowledgeBaseIds 知识库ID列表
     * @param userId 用户ID
     * @return 知识库名称列表 */
    private List<String> getKnowledgeBaseNames(List<String> knowledgeBaseIds, String userId) {
        return knowledgeBaseIds.stream().map(id -> {
            try {
                // 获取用户安装的知识库信息
                var userRag = userRagDomainService.findInstalledRagByOriginalId(userId, id);
                if (userRag != null) {
                    // 用户已安装，直接使用安装记录中的名称
                    // 无论是SNAPSHOT还是REFERENCE类型，都使用安装记录中的信息
                    return userRag.getName();
                } else {
                    // 用户未安装该知识库，不应该能访问
                    log.warn("用户 {} 未安装知识库 {}，无法获取名称", userId, id);
                    return "未知知识库";
                }
            } catch (Exception e) {
                log.warn("获取知识库 {} 名称失败: {}", id, e.getMessage());
                return "未知知识库";
            }
        }).collect(Collectors.toList());
    }

    /** 检查Agent是否配置了RAG工具
     * @param agent Agent实体
     * @return 是否配置了RAG工具 */
    public boolean hasRagTools(AgentEntity agent) {
        List<String> knowledgeBaseIds = agent.getKnowledgeBaseIds();
        return knowledgeBaseIds != null && !knowledgeBaseIds.isEmpty();
    }

    /** 获取Agent配置的知识库数量
     * @param agent Agent实体
     * @return 知识库数量 */
    public int getKnowledgeBaseCount(AgentEntity agent) {
        List<String> knowledgeBaseIds = agent.getKnowledgeBaseIds();
        return knowledgeBaseIds != null ? knowledgeBaseIds.size() : 0;
    }
}
