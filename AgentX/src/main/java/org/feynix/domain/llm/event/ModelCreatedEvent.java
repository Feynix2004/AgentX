package org.feynix.domain.llm.event;

import org.feynix.domain.llm.model.ModelEntity;

/**
 * 模型创建事件
 * 
 * @author feynix
 * @since 1.0.0
 */
public class ModelCreatedEvent extends ModelDomainEvent {

    /**
     * 模型实体
     */
    private final ModelEntity model;

    public ModelCreatedEvent(String modelId, String userId, ModelEntity model) {
        super(modelId, userId);
        this.model = model;
    }

    public ModelEntity getModel() {
        return model;
    }
} 