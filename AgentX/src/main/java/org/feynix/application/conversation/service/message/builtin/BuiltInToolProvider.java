package org.feynix.application.conversation.service.message.builtin;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import org.feynix.domain.agent.model.AgentEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 内置工具提供者接口
 * 
 * 所有内置工具都必须实现此接口，用于向AgentX系统提供工具能力 内置工具是系统预置的功能模块，如RAG搜索、天气查询、计算器等
 * 
 * 融合架构：工具提供者既负责定义工具规范，也负责执行工具逻辑 简化了工具开发流程，提高了代码内聚性 */
public interface BuiltInToolProvider {

    /** 定义工具规范（新增融合架构方法）
     * 
     * 子类实现此方法来定义工具的规范，包括名称、描述、参数等 替代原来的静态Specification类，简化工具定义
     * 
     * @param agent Agent实体，包含工具配置信息
     * @return 工具定义列表，如果不支持该Agent则返回null或空List */
    List<ToolDefinition> defineTools(AgentEntity agent);

    /** 执行工具逻辑（新增融合架构方法）
     * 
     * 子类实现此方法来执行具体的工具逻辑 替代原来的独立Executor类，提高代码内聚性
     * 
     * @param toolName 工具名称
     * @param arguments 工具参数JSON字符串
     * @param agent Agent实体
     * @param memoryId 内存ID
     * @return 执行结果 */
    String executeTools(String toolName, String arguments, AgentEntity agent, Object memoryId);

    /** 为指定的Agent创建工具（保留向后兼容）
     * 
     * 使用内部代理模式，委托给defineTools和executeTools方法 保持与现有BuiltInToolRegistry的兼容性
     * 
     * @param agent Agent实体，包含工具配置信息
     * @return 工具规范和执行器的映射，如果不支持该Agent则返回null或空Map */
    default Map<ToolSpecification, ToolExecutor> createTools(AgentEntity agent) {
        List<ToolDefinition> definitions = defineTools(agent);

        if (definitions == null || definitions.isEmpty()) {
            return new HashMap<>();
        }

        Map<ToolSpecification, ToolExecutor> tools = new HashMap<>();

        for (ToolDefinition definition : definitions) {
            ToolSpecification spec = definition.toSpecification();
            ToolExecutor executor = (request, memoryId) -> executeTools(request.name(), request.arguments(), agent,
                    memoryId);
            tools.put(spec, executor);
        }

        return tools;
    }

    /** 检查是否支持指定的Agent
     * 
     * 默认实现：检查defineTools是否返回非空列表 子类可以重写此方法提供更复杂的支持判断逻辑
     * 
     * @param agent Agent实体
     * @return 如果支持该Agent则返回true，否则返回false */
    default boolean supports(AgentEntity agent) {
        List<ToolDefinition> definitions = defineTools(agent);
        return definitions != null && !definitions.isEmpty();
    }

    /** 获取工具提供者的名称 用于日志记录和调试
     * 
     * @return 工具提供者名称 */
    String getName();

    /** 获取工具提供者的描述 用于说明该工具的功能和用途
     * 
     * @return 工具提供者描述 */
    String getDescription();

    /** 获取工具提供者的优先级 数值越小优先级越高，用于工具执行顺序控制
     * 
     * @return 优先级值，默认为100 */
    default int getPriority() {
        return 100;
    }
}