package org.feynix.infrastructure.converter;

import org.apache.ibatis.type.MappedTypes;
import org.feynix.domain.tool.model.config.ToolDefinition;

import java.util.List;

/** 工具定义列表JSON转换器 */
@MappedTypes(List.class)
public class ToolDefinitionListConverter extends JsonToStringConverter<List<ToolDefinition>> {

    public ToolDefinitionListConverter() {
        super((Class<List<ToolDefinition>>) (Class<?>) List.class);
    }
}