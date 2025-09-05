package org.feynix.application.conversation.service.message.builtin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.feynix.domain.agent.model.AgentEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 内置工具提供者抽象基类
 * 
 * 提供模板方法模式和通用功能，简化内置工具的开发 子类只需实现核心的业务逻辑，通用的参数解析、错误处理、日志记录等由基类提供 */
public abstract class AbstractBuiltInToolProvider implements BuiltInToolProvider {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final ObjectMapper objectMapper = new ObjectMapper();

    /** 模板方法：执行具体的工具逻辑
     * 
     * 子类只需实现这个方法，专注于核心业务逻辑
     * 
     * @param toolName 工具名称
     * @param arguments 已解析的参数JSON
     * @param agent Agent实体
     * @param memoryId 内存ID
     * @return 执行结果 */
    protected abstract String doExecute(String toolName, JsonNode arguments, AgentEntity agent, Object memoryId);

    /** 实现BuiltInToolProvider接口：执行工具
     * 
     * 提供统一的执行流程，包括参数解析、错误处理、日志记录 */
    @Override
    public final String executeTools(String toolName, String arguments, AgentEntity agent, Object memoryId) {
        try {
            logger.info("执行内置工具: {} for agent: {} (user: {})", toolName, agent.getId(), agent.getUserId());

            // 参数验证
            if (!StringUtils.hasText(toolName)) {
                return formatError("工具名称不能为空");
            }

            if (!StringUtils.hasText(arguments)) {
                return formatError("工具参数不能为空");
            }

            // 解析参数
            JsonNode argsNode = parseArguments(arguments);
            if (argsNode == null) {
                return formatError("参数解析失败：无效的JSON格式");
            }

            // 检查是否支持该工具
            if (!supportsTools(toolName, agent)) {
                return formatError("不支持的工具: " + toolName);
            }

            // 委托给子类实现
            String result = doExecute(toolName, argsNode, agent, memoryId);

            logger.debug("内置工具执行成功: {} for agent: {}", toolName, agent.getId());
            return result;

        } catch (IllegalArgumentException e) {
            logger.warn("内置工具参数错误: {} for agent: {} - {}", toolName, agent.getId(), e.getMessage());
            return formatError("参数错误: " + e.getMessage());
        } catch (Exception e) {
            logger.error("内置工具执行失败: {} for agent: {} - {}", toolName, agent.getId(), e.getMessage(), e);
            return formatError("执行失败: " + e.getMessage());
        }
    }

    /** 实现BuiltInToolProvider接口：创建工具
     * 
     * 使用代理模式，内部委托回executeTools方法 保持与现有架构的兼容性 */
    @Override
    public final Map<ToolSpecification, ToolExecutor> createTools(AgentEntity agent) {
        List<ToolDefinition> definitions = defineTools(agent);

        if (definitions == null || definitions.isEmpty()) {
            return new HashMap<>();
        }

        Map<ToolSpecification, ToolExecutor> tools = new HashMap<>();

        for (ToolDefinition definition : definitions) {
            ToolSpecification spec = definition.toSpecification();
            ToolExecutor executor = createToolExecutor(agent);
            tools.put(spec, executor);
        }

        logger.debug("为Agent {} 创建了 {} 个内置工具", agent.getId(), tools.size());
        return tools;
    }

    /** 检查是否支持指定的工具
     * 
     * 默认实现：检查工具名称是否在defineTools返回的定义中 子类可以重写此方法提供更复杂的逻辑 */
    protected boolean supportsTools(String toolName, AgentEntity agent) {
        List<ToolDefinition> definitions = defineTools(agent);
        if (definitions == null) {
            return false;
        }

        return definitions.stream().anyMatch(def -> toolName.equals(def.getName()));
    }

    /** 创建工具执行器
     * 
     * 使用代理模式委托回executeTools方法 */
    private ToolExecutor createToolExecutor(AgentEntity agent) {
        return (request, memoryId) -> executeTools(request.name(), request.arguments(), agent, memoryId);
    }

    // ====== 通用工具方法 ======

    /** 解析参数JSON
     * 
     * @param arguments JSON字符串
     * @return 解析后的JsonNode，解析失败返回null */
    protected JsonNode parseArguments(String arguments) {
        try {
            return objectMapper.readTree(arguments);
        } catch (Exception e) {
            logger.warn("参数解析失败: {} - {}", arguments, e.getMessage());
            return null;
        }
    }

    /** 格式化错误信息
     * 
     * @param message 错误消息
     * @return 格式化后的错误信息 */
    protected String formatError(String message) {
        return "❌ " + message;
    }

    /** 格式化成功信息
     * 
     * @param message 成功消息
     * @return 格式化后的成功信息 */
    protected String formatSuccess(String message) {
        return "✅ " + message;
    }

    /** 格式化带数据的成功信息
     * 
     * @param title 标题
     * @param content 内容
     * @return 格式化后的信息 */
    protected String formatSuccessWithData(String title, String content) {
        StringBuilder result = new StringBuilder();
        result.append("✅ ").append(title).append("\n\n");
        result.append(content);
        return result.toString();
    }

    /** 获取字符串参数
     * 
     * @param arguments 参数JSON
     * @param paramName 参数名
     * @param defaultValue 默认值
     * @return 参数值 */
    protected String getStringParameter(JsonNode arguments, String paramName, String defaultValue) {
        if (arguments.has(paramName)) {
            return arguments.get(paramName).asText();
        }
        return defaultValue;
    }

    /** 获取字符串参数（必需）
     * 
     * @param arguments 参数JSON
     * @param paramName 参数名
     * @return 参数值
     * @throws IllegalArgumentException 如果参数不存在或为空 */
    protected String getRequiredStringParameter(JsonNode arguments, String paramName) {
        if (!arguments.has(paramName)) {
            throw new IllegalArgumentException("缺少必需参数: " + paramName);
        }

        String value = arguments.get(paramName).asText();
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("参数不能为空: " + paramName);
        }

        return value;
    }

    /** 获取整数参数
     * 
     * @param arguments 参数JSON
     * @param paramName 参数名
     * @param defaultValue 默认值
     * @return 参数值 */
    protected int getIntegerParameter(JsonNode arguments, String paramName, int defaultValue) {
        if (arguments.has(paramName)) {
            return arguments.get(paramName).asInt(defaultValue);
        }
        return defaultValue;
    }

    /** 获取双精度参数
     * 
     * @param arguments 参数JSON
     * @param paramName 参数名
     * @param defaultValue 默认值
     * @return 参数值 */
    protected double getDoubleParameter(JsonNode arguments, String paramName, double defaultValue) {
        if (arguments.has(paramName)) {
            return arguments.get(paramName).asDouble(defaultValue);
        }
        return defaultValue;
    }

    /** 获取布尔参数
     * 
     * @param arguments 参数JSON
     * @param paramName 参数名
     * @param defaultValue 默认值
     * @return 参数值 */
    protected boolean getBooleanParameter(JsonNode arguments, String paramName, boolean defaultValue) {
        if (arguments.has(paramName)) {
            return arguments.get(paramName).asBoolean(defaultValue);
        }
        return defaultValue;
    }

    /** 验证参数值范围
     * 
     * @param value 数值
     * @param min 最小值
     * @param max 最大值
     * @param paramName 参数名（用于错误信息）
     * @throws IllegalArgumentException 如果值超出范围 */
    protected void validateRange(double value, double min, double max, String paramName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    String.format("参数 %s 的值 %.2f 超出范围 [%.2f, %.2f]", paramName, value, min, max));
        }
    }

    /** 验证参数值范围
     * 
     * @param value 整数
     * @param min 最小值
     * @param max 最大值
     * @param paramName 参数名（用于错误信息）
     * @throws IllegalArgumentException 如果值超出范围 */
    protected void validateRange(int value, int min, int max, String paramName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(String.format("参数 %s 的值 %d 超出范围 [%d, %d]", paramName, value, min, max));
        }
    }

    // ====== 实现BuiltInToolProvider接口的方法 ======

    /** 获取工具提供者名称 默认实现：从@BuiltInTool注解中获取 */
    @Override
    public String getName() {
        BuiltInTool annotation = this.getClass().getAnnotation(BuiltInTool.class);
        return annotation != null ? annotation.name() : this.getClass().getSimpleName();
    }

    /** 获取工具提供者描述 默认实现：从@BuiltInTool注解中获取 */
    @Override
    public String getDescription() {
        BuiltInTool annotation = this.getClass().getAnnotation(BuiltInTool.class);
        return annotation != null ? annotation.description() : "内置工具";
    }

    /** 获取工具提供者优先级 默认实现：从@BuiltInTool注解中获取 */
    @Override
    public int getPriority() {
        BuiltInTool annotation = this.getClass().getAnnotation(BuiltInTool.class);
        return annotation != null ? annotation.priority() : 100;
    }
}