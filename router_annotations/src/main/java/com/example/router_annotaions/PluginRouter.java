package com.example.router_annotaions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 生命周期：SOURCE < CLASS < RUNTIME
 *
 * 1、一般如果需要在运行时去动态获取注解信息，用RUNTIME注解
 * 2、要在编译时进行一些预处理操作，如ButterKnife，用CLASS注解。注解会在class文件中存在，但是在运行时会被丢弃
 * 3、做一些检查性的操作，如@Override，用SOURCE源码注解。注解仅存在源码级别，在编译的时候丢弃该注解
 *
 * 该注解用在每个组件的Activity上面，
 * APT会根据此注解生成全局路由表
 * 及彼岸组件之间没有依赖，也可以在不同组件的Activity之间的跳转
 */

@Target(ElementType.TYPE) // 该注解用在类或接口上
@Retention(RetentionPolicy.CLASS) // 在编译时生效
public @interface PluginRouter {
    /**
     * 模块名+类名 ： app/MainActivity personal/xxxactivity...
     *
     * @return
     */
    String className() default "";

    /**
     * 模块名 ： app  personal  order...
     *
     * @return
     */
    String moduleName() default "";
}
