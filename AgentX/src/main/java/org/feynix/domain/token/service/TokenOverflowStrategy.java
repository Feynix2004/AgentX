package org.feynix.domain.token.service;

import org.feynix.domain.token.model.TokenMessage;
import org.feynix.domain.token.model.TokenProcessResult;
import org.feynix.domain.token.model.config.TokenOverflowConfig;

import java.util.List;

/** Token溢出处理策略接口 */
public interface TokenOverflowStrategy {
    /** 处理消息列表
     *
     * @param messages 待处理的消息列表
     * @return 处理结果，包含处理后的消息列表、摘要等信息 */
    TokenProcessResult process(List<TokenMessage> messages, TokenOverflowConfig tokenOverflowConfig);

    /** 获取策略名称
     *
     * @return 策略名称 */
    String getName();

    /** 检查是否需要处理
     *
     * @param messages 待检查的消息列表
     * @return 是否需要处理 */
    boolean needsProcessing(List<TokenMessage> messages);
}