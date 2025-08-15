package org.feynix.application.conversation.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;
import org.feynix.domain.conversation.model.SessionEntity;
import org.feynix.domain.conversation.service.SessionDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/** RAG会话管理器
 * 负责管理RAG对话的临时会话，支持会话复用和自动清理 */
@Component
public class RagSessionManager {

    private static final Logger logger = LoggerFactory.getLogger(RagSessionManager.class);

    /** RAG会话缓存 - 使用Guava Cache，自动TTL过期 */
    private final Cache<String, String> ragSessionCache;

    /** RAG会话最大存活时间（分钟） */
    private static final int RAG_SESSION_MAX_LIFE_MINUTES = 30;

    private final SessionDomainService sessionDomainService;

    public RagSessionManager(SessionDomainService sessionDomainService) {
        this.sessionDomainService = sessionDomainService;
        
        // 初始化Guava Cache，30分钟自动过期
        this.ragSessionCache = CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(RAG_SESSION_MAX_LIFE_MINUTES))
                .maximumSize(1000) // 最大缓存1000个会话
                .recordStats() // 启用统计功能
                .build();
    }

    /** 为用户创建或获取RAG临时会话
     * @param userId 用户ID
     * @return 会话ID */
    public String createOrGetRagSession(String userId) {
        String existingSessionId = ragSessionCache.getIfPresent(userId);
        
        // 如果缓存中存在会话（说明未过期），直接返回
        if (existingSessionId != null) {
            logger.debug("复用已存在的RAG会话: {} for user: {}", existingSessionId, userId);
            return existingSessionId;
        }

        // 创建新会话
        return createNewRagSession(userId);
    }

    /** 为用户RAG对话创建新的临时会话
     * @param userId 用户ID
     * @param userRagId 用户RAG ID
     * @return 会话ID */
    public String createOrGetUserRagSession(String userId, String userRagId) {
        String sessionKey = userId + "_" + userRagId;
        String existingSessionId = ragSessionCache.getIfPresent(sessionKey);
        
        // 如果缓存中存在会话（说明未过期），直接返回
        if (existingSessionId != null) {
            logger.debug("复用已存在的用户RAG会话: {} for user: {} userRag: {}", existingSessionId, userId, userRagId);
            return existingSessionId;
        }

        // 创建新会话
        return createNewUserRagSession(userId, userRagId, sessionKey);
    }

    /** 创建新的RAG会话
     * @param userId 用户ID
     * @return 会话ID */
    private String createNewRagSession(String userId) {
        try {
            // 使用现有的 createSession 方法
            SessionEntity session = sessionDomainService.createSession("system-rag-agent", userId);
            String sessionId = session.getId();
            
            // 更新会话标题
            sessionDomainService.updateSession(sessionId, userId, "RAG对话");
            
            // 缓存会话信息到Guava Cache（自动TTL管理）
            ragSessionCache.put(userId, sessionId);
            
            logger.info("创建新的RAG会话: {} for user: {}", sessionId, userId);
            return sessionId;
            
        } catch (Exception e) {
            logger.error("创建RAG会话失败 for user: {}", userId, e);
            throw new RuntimeException("创建RAG会话失败", e);
        }
    }

    /** 创建新的用户RAG会话
     * @param userId 用户ID
     * @param userRagId 用户RAG ID
     * @param sessionKey 会话缓存键
     * @return 会话ID */
    private String createNewUserRagSession(String userId, String userRagId, String sessionKey) {
        try {
            // 使用现有的 createSession 方法
            SessionEntity session = sessionDomainService.createSession("system-rag-agent", userId);
            String sessionId = session.getId();
            
            // 更新会话标题
            sessionDomainService.updateSession(sessionId, userId, "知识库对话 - " + userRagId);
            
            // 缓存会话信息到Guava Cache（自动TTL管理）
            ragSessionCache.put(sessionKey, sessionId);
            
            logger.info("创建新的用户RAG会话: {} for user: {} userRag: {}", sessionId, userId, userRagId);
            return sessionId;
            
        } catch (Exception e) {
            logger.error("创建用户RAG会话失败 for user: {} userRag: {}", userId, userRagId, e);
            throw new RuntimeException("创建用户RAG会话失败", e);
        }
    }


    /** 手动清理指定用户的RAG会话
     * @param userId 用户ID */
    public void clearUserRagSessions(String userId) {
        String sessionId = ragSessionCache.getIfPresent(userId);
        if (sessionId != null) {
            ragSessionCache.invalidate(userId);
            logger.info("手动清理用户RAG会话: {} for user: {}", sessionId, userId);
        }
    }

    /** 获取当前缓存的会话数量（用于监控） */
    public long getCachedSessionCount() {
        return ragSessionCache.size();
    }

    /** 获取缓存统计信息（可选，用于监控和调试） */
    public String getCacheStats() {
        return String.format("缓存大小: %d, 统计信息: %s", 
                ragSessionCache.size(), 
                ragSessionCache.stats().toString());
    }

    /** 清理所有过期会话（手动触发清理） */
    public void cleanupExpiredSessions() {
        ragSessionCache.cleanUp();
        logger.info("手动清理过期的RAG会话缓存");
    }
}