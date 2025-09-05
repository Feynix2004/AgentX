package org.feynix.application.conversation.service.message.builtin;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonBooleanSchema;
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema;
import dev.langchain4j.model.chat.request.json.JsonNumberSchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;

import java.util.*;

/** 工具定义类
 * 
 * 用于简化内置工具的规范定义，提供链式构建器API 自动转换为langchain4j的ToolSpecification格式 */
public class ToolDefinition {

    private final String name;
    private final String description;
    private final Map<String, ParameterDefinition> parameters;
    private final Set<String> requiredParameters;
    private final int priority;

    private ToolDefinition(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.parameters = new HashMap<>(builder.parameters);
        this.requiredParameters = new HashSet<>(builder.requiredParameters);
        this.priority = builder.priority;
    }

    /** 转换为langchain4j的ToolSpecification */
    public ToolSpecification toSpecification() {
        JsonObjectSchema.Builder schemaBuilder = JsonObjectSchema.builder();

        // 添加参数定义
        Map<String, dev.langchain4j.model.chat.request.json.JsonSchemaElement> properties = new HashMap<>();
        for (Map.Entry<String, ParameterDefinition> entry : parameters.entrySet()) {
            String paramName = entry.getKey();
            ParameterDefinition paramDef = entry.getValue();

            dev.langchain4j.model.chat.request.json.JsonSchemaElement schema = createJsonSchema(paramDef);
            properties.put(paramName, schema);
        }

        if (!properties.isEmpty()) {
            schemaBuilder.addProperties(properties);
        }

        // 添加必需参数
        if (!requiredParameters.isEmpty()) {
            schemaBuilder.required(requiredParameters.toArray(new String[0]));
        }

        return ToolSpecification.builder().name(name).description(description).parameters(schemaBuilder.build())
                .build();
    }

    /** 根据参数定义创建JSON Schema */
    private dev.langchain4j.model.chat.request.json.JsonSchemaElement createJsonSchema(ParameterDefinition paramDef) {
        switch (paramDef.type) {
            case STRING :
                return JsonStringSchema.builder().description(paramDef.description).build();
            case INTEGER :
                return JsonIntegerSchema.builder().description(paramDef.description).build();
            case NUMBER :
                return JsonNumberSchema.builder().description(paramDef.description).build();
            case BOOLEAN :
                return JsonBooleanSchema.builder().description(paramDef.description).build();
            default :
                return JsonStringSchema.builder().description(paramDef.description).build();
        }
    }

    // Getters
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public Map<String, ParameterDefinition> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }
    public Set<String> getRequiredParameters() {
        return Collections.unmodifiableSet(requiredParameters);
    }
    public int getPriority() {
        return priority;
    }

    /** 创建构建器 */
    public static Builder builder() {
        return new Builder();
    }

    /** 构建器类 */
    public static class Builder {
        private String name;
        private String description;
        private Map<String, ParameterDefinition> parameters = new HashMap<>();
        private Set<String> requiredParameters = new HashSet<>();
        private int priority = 100;

        /** 设置工具名称 */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /** 设置工具描述 */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /** 设置优先级 */
        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        /** 添加字符串参数 */
        public Builder addStringParameter(String name, String description) {
            parameters.put(name, new ParameterDefinition(ParameterType.STRING, description));
            return this;
        }

        /** 添加字符串参数（必需） */
        public Builder addRequiredStringParameter(String name, String description) {
            parameters.put(name, new ParameterDefinition(ParameterType.STRING, description));
            requiredParameters.add(name);
            return this;
        }

        /** 添加整数参数 */
        public Builder addIntegerParameter(String name, String description) {
            parameters.put(name, new ParameterDefinition(ParameterType.INTEGER, description));
            return this;
        }

        /** 添加整数参数（必需） */
        public Builder addRequiredIntegerParameter(String name, String description) {
            parameters.put(name, new ParameterDefinition(ParameterType.INTEGER, description));
            requiredParameters.add(name);
            return this;
        }

        /** 添加数字参数 */
        public Builder addNumberParameter(String name, String description) {
            parameters.put(name, new ParameterDefinition(ParameterType.NUMBER, description));
            return this;
        }

        /** 添加数字参数（必需） */
        public Builder addRequiredNumberParameter(String name, String description) {
            parameters.put(name, new ParameterDefinition(ParameterType.NUMBER, description));
            requiredParameters.add(name);
            return this;
        }

        /** 添加布尔参数 */
        public Builder addBooleanParameter(String name, String description) {
            parameters.put(name, new ParameterDefinition(ParameterType.BOOLEAN, description));
            return this;
        }

        /** 添加布尔参数（必需） */
        public Builder addRequiredBooleanParameter(String name, String description) {
            parameters.put(name, new ParameterDefinition(ParameterType.BOOLEAN, description));
            requiredParameters.add(name);
            return this;
        }

        /** 快捷方法：添加参数（自动推断类型） */
        public Builder addParameter(String name, String description) {
            return addStringParameter(name, description);
        }

        /** 快捷方法：添加必需参数 */
        public Builder addRequiredParameter(String name, String description) {
            return addRequiredStringParameter(name, description);
        }

        /** 构建ToolDefinition */
        public ToolDefinition build() {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("工具名称不能为空");
            }
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("工具描述不能为空");
            }
            return new ToolDefinition(this);
        }
    }

    /** 参数定义内部类 */
    public static class ParameterDefinition {
        private final ParameterType type;
        private final String description;

        public ParameterDefinition(ParameterType type, String description) {
            this.type = type;
            this.description = description;
        }

        public ParameterType getType() {
            return type;
        }
        public String getDescription() {
            return description;
        }
    }

    /** 参数类型枚举 */
    public enum ParameterType {
        STRING, INTEGER, NUMBER, BOOLEAN
    }
}