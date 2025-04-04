package org.feynix.infrastructure.converter;

import org.apache.ibatis.type.MappedTypes;
import org.feynix.domain.agent.model.LLMModelConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * List JSON转换器
 */
@MappedTypes(ArrayList.class)
public class ListConverter extends JsonToStringConverter<ArrayList> {

    public ListConverter() {
        super(ArrayList.class);
    }
}