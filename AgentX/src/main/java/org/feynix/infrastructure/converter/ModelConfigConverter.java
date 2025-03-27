package org.feynix.infrastructure.converter;

import org.feynix.domain.llm.model.config.ModelConfig;

/**
 * Model配置转换器
 */
public class ModelConfigConverter extends JsonToStringConverter<ModelConfig> {
    
    public ModelConfigConverter() {
        super(ModelConfig.class);
    }
} 