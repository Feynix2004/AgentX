package org.feynix.application.conversation.service.message.builtin;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.feynix.domain.agent.model.AgentEntity;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** 内置工具注册器
 * 
 * 负责自动发现、注册和管理所有内置工具提供者 通过Spring的ApplicationContext自动扫描带有@BuiltInTool注解的组件 */
@Component
public class BuiltInToolRegistry {

    private static final Logger logger = LoggerFactory.getLogger(BuiltInToolRegistry.class);

    private final ApplicationContext applicationContext;

    /** 所有已注册的内置工具提供者，按优先级排序 */
    private final List<BuiltInToolProvider> toolProviders = new ArrayList<>();

    /** 工具提供者的元数据缓存 */
    private final Map<String, BuiltInToolMetadata> toolMetadataCache = new ConcurrentHashMap<>();

    public BuiltInToolRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /** 初始化方法，在Spring上下文刷新完成后自动执行 扫描并注册所有带有@BuiltInTool注解的工具提供者 */
    @EventListener(ContextRefreshedEvent.class)
    public void initialize() {
        logger.info("开始初始化内置工具注册器...");

        // 获取所有带有@BuiltInTool注解的Bean
        Map<String, Object> builtInToolBeans = applicationContext.getBeansWithAnnotation(BuiltInTool.class);

        List<BuiltInToolProvider> providers = new ArrayList<>();

        for (Map.Entry<String, Object> entry : builtInToolBeans.entrySet()) {
            String beanName = entry.getKey();
            Object bean = entry.getValue();

            if (bean instanceof BuiltInToolProvider) {
                BuiltInToolProvider provider = (BuiltInToolProvider) bean;
                BuiltInTool annotation = provider.getClass().getAnnotation(BuiltInTool.class);

                if (annotation != null && annotation.enabled()) {
                    providers.add(provider);

                    // 缓存工具元数据
                    BuiltInToolMetadata metadata = new BuiltInToolMetadata(annotation.name(), annotation.description(),
                            annotation.priority(), annotation.enabled(), provider.getClass().getSimpleName());
                    toolMetadataCache.put(annotation.name(), metadata);

                    logger.info("注册内置工具: {} - {} (优先级: {}, Bean: {})", annotation.name(), annotation.description(),
                            annotation.priority(), beanName);
                } else {
                    logger.info("跳过已禁用的内置工具: {} (Bean: {})", annotation != null ? annotation.name() : "未知", beanName);
                }
            } else {
                logger.warn("Bean {} 标记为@BuiltInTool但未实现BuiltInToolProvider接口", beanName);
            }
        }

        // 按优先级排序（优先级数值越小越优先）
        providers.sort(Comparator.comparingInt(BuiltInToolProvider::getPriority));

        // 线程安全地更新工具提供者列表
        synchronized (toolProviders) {
            toolProviders.clear();
            toolProviders.addAll(providers);
        }

        logger.info("内置工具注册器初始化完成，共注册 {} 个内置工具", toolProviders.size());

        // 打印所有注册的工具信息
        if (logger.isDebugEnabled()) {
            toolProviders.forEach(provider -> {
                logger.debug("内置工具详情: {} - {}", provider.getName(), provider.getDescription());
            });
        }
    }

    /** 为指定Agent创建所有适用的内置工具
     * 
     * @param agent Agent实体
     * @return 所有内置工具的合并映射 */
    public Map<ToolSpecification, ToolExecutor> createToolsForAgent(AgentEntity agent) {
        Map<ToolSpecification, ToolExecutor> allTools = new HashMap<>();

        synchronized (toolProviders) {
            for (BuiltInToolProvider provider : toolProviders) {
                try {
                    if (provider.supports(agent)) {
                        Map<ToolSpecification, ToolExecutor> providerTools = provider.createTools(agent);

                        if (providerTools != null && !providerTools.isEmpty()) {
                            allTools.putAll(providerTools);

                            logger.debug("为Agent {} 添加内置工具 {} 的 {} 个工具", agent.getId(), provider.getName(),
                                    providerTools.size());
                        }
                    }
                } catch (Exception e) {
                    logger.error("为Agent {} 创建内置工具 {} 时发生异常: {}", agent.getId(), provider.getName(), e.getMessage(), e);
                }
            }
        }

        logger.info("为Agent {} 创建了 {} 个内置工具", agent.getId(), allTools.size());
        return allTools;
    }

    /** 获取所有已注册的工具提供者
     * 
     * @return 工具提供者列表的副本 */
    public List<BuiltInToolProvider> getAllProviders() {
        synchronized (toolProviders) {
            return new ArrayList<>(toolProviders);
        }
    }

    /** 根据名称获取工具提供者
     * 
     * @param name 工具名称
     * @return 工具提供者，如果不存在则返回null */
    public BuiltInToolProvider getProviderByName(String name) {
        synchronized (toolProviders) {
            return toolProviders.stream().filter(provider -> name.equals(provider.getName())).findFirst().orElse(null);
        }
    }

    /** 获取所有工具的元数据信息
     * 
     * @return 工具元数据列表 */
    public List<BuiltInToolMetadata> getAllToolMetadata() {
        return new ArrayList<>(toolMetadataCache.values());
    }

    /** 获取已注册工具的数量
     * 
     * @return 工具数量 */
    public int getToolCount() {
        synchronized (toolProviders) {
            return toolProviders.size();
        }
    }

    /** 检查是否有工具支持指定的Agent
     * 
     * @param agent Agent实体
     * @return 如果有工具支持该Agent则返回true */
    public boolean hasToolsForAgent(AgentEntity agent) {
        synchronized (toolProviders) {
            return toolProviders.stream().anyMatch(provider -> provider.supports(agent));
        }
    }

    /** 内置工具元数据类 用于缓存和查询工具信息 */
    public static class BuiltInToolMetadata {
        private final String name;
        private final String description;
        private final int priority;
        private final boolean enabled;
        private final String className;

        public BuiltInToolMetadata(String name, String description, int priority, boolean enabled, String className) {
            this.name = name;
            this.description = description;
            this.priority = priority;
            this.enabled = enabled;
            this.className = className;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public int getPriority() {
            return priority;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getClassName() {
            return className;
        }

        @Override
        public String toString() {
            return String.format(
                    "BuiltInToolMetadata{name='%s', description='%s', priority=%d, enabled=%s, className='%s'}", name,
                    description, priority, enabled, className);
        }
    }
}