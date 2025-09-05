package org.feynix.application.conversation.service.message.builtin;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/** 内置工具标识注解
 * 
 * 用于标识一个类是内置工具提供者，该注解同时具有@Component的功能 被标记的类会自动注册到BuiltInToolRegistry中，并可以被Spring容器管理 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface BuiltInTool {

    /** 内置工具的名称 必须唯一，用于标识和日志记录
     * 
     * @return 工具名称 */
    String name();

    /** 内置工具的描述 用于说明该工具的功能和用途
     * 
     * @return 工具描述，默认为空字符串 */
    String description() default "";

    /** 工具的优先级 数值越小优先级越高，用于控制工具的执行顺序
     * 
     * @return 优先级值，默认为100 */
    int priority() default 100;

    /** 是否启用该工具 可以通过配置动态控制工具的启用状态
     * 
     * @return true表示启用，false表示禁用，默认为true */
    boolean enabled() default true;

    /** Spring组件的名称 作为@Component注解的别名
     * 
     * @return Spring Bean的名称 */
    @AliasFor(annotation = Component.class)
    String value() default "";
}