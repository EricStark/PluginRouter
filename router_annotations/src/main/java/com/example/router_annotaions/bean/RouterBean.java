package com.example.router_annotaions.bean;

import javax.lang.model.element.Element;

/**
 * 路由表中每个Activity.class 对应一个 bean
 * bean里面保存对应 class对象 模块名 模块名+类名 被注解修饰的类型等参数
 */
public class RouterBean {

    public enum RouterType {
        ACTIVITY,
        CALL
    }

    private RouterType routerType;
    private Element element;
    private Class<?> aClass;
    private String className;
    private String moduleName;

    public RouterType getRouterType() {
        return routerType;
    }

    public void setRouterType(RouterType routerType) {
        this.routerType = routerType;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public Class<?> getaClass() {
        return aClass;
    }

    public void setaClass(Class<?> aClass) {
        this.aClass = aClass;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    private RouterBean(RouterType routerType,Class<?> aClass, String className, String moduleName) {
        this.routerType = routerType;
        this.aClass = aClass;
        this.className = className;
        this.moduleName = moduleName;
    }

    /**
     * create 方式创建对象
     *
     * @param routerType
     * @param aClass
     * @param className
     * @param moduleName
     * @return
     */
    public static RouterBean create(RouterType routerType, Class<?> aClass, String className, String moduleName) {
        return new RouterBean(routerType, aClass, className, moduleName);
    }

    /**
     * 建造者模式创建对象
     *
     * @param builder
     */
    private RouterBean(Builder builder) {
        this.routerType = builder.type;
        this.element = builder.element;
        this.aClass = builder.clazz;
        this.className = builder.className;
        this.moduleName = builder.moduleName;
    }

    /**
     * 构建者模式
     */
    public static class Builder {

        // 枚举类型：Activity
        private RouterType type;
        // 类节点
        private Element element;
        // 注解使用的类对象
        private Class<?> clazz;
        // 路由类名
        private String className;
        // 路由组件
        private String moduleName;

        public Builder addType(RouterType type) {
            this.type = type;
            return this;
        }

        public Builder addElement(Element element) {
            this.element = element;
            return this;
        }

        public Builder addClazz(Class<?> clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder addClassName(String path) {
            this.className = path;
            return this;
        }

        public Builder addModuleName(String group) {
            this.moduleName = group;
            return this;
        }

        // 最后的build或者create，往往是做参数的校验或者初始化赋值工作
        public RouterBean build() {
            if (className == null || className.length() == 0) {
                throw new IllegalArgumentException("path必填项为空，如：/app/MainActivity");
            }
            return new RouterBean(this);
        }
    }

    @Override
    public String toString() {
        return "RouterBean{" +
                "routerType=" + routerType +
                ", element=" + element +
                ", aClass=" + aClass +
                ", className='" + className + '\'' +
                ", moduleName='" + moduleName + '\'' +
                '}';
    }
}
