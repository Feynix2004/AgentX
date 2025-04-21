package org.feynix.infrastructure.llm.protocol.enums;

import org.feynix.domain.conversation.constant.Role;
import org.feynix.infrastructure.exception.BusinessException;

public enum ProviderProtocol {

    OpenAI,
    CLAUDE;

    public static ProviderProtocol fromCode(String code) {
        for (ProviderProtocol protocol : values()) {
            if (protocol.name().equals(code)) {
                return protocol;
            }
        }
        throw new BusinessException("Unknown model type code: " + code);
    }
}
