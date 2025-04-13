package org.feynix.domain.rag.model.enums;

import org.feynix.infrastructure.enums.IBaseEnum;

/**
 * @author shilong.zang
 * @date 09:45 <br/>
 */
public enum RagDocSyncOcrEnum implements IBaseEnum {

    /**
     * pdf策略
     */
    PDF("pdf", "ragDocSyncOcr-PDF"),


    ;


    private final String value;
    private final String label;

    RagDocSyncOcrEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }


    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public String getLabel() {
        return this.label;
    }
}
