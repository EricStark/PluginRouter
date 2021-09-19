package com.example.router_complier;

import com.example.router_annotaions.Parameter;
import com.example.router_complier.consts.ProcessorConst;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * 目的：生成以下代码
 *
 * @Override public void getParameter(Object targetParameter) {
 * Personal_MainActivity t = (Personal_MainActivity) targetParameter;
 * t.name = t.getIntent().getStringExtra("name");
 * t.sex = t.getIntent().getStringExtra("sex");
 * t.age = t.getIntent().getIntExtra("age",t.age);
 * }
 */
public class ParameterFactory {

    // 方法的构建
    private MethodSpec.Builder method;

    // 类名，如：MainActivity  /  Personal_MainActivity
    private ClassName className;

    // Messager用来报告错误，警告和其他提示信息
    private Messager messager;

    //方法参数
    private ParameterSpec parameterSpec;

    // type(类信息)工具类，包含用于操作TypeMirror的工具方法
    private Types typeUtils;

    private Elements elementUtils;

    private TypeMirror callMirror;

    private ParameterFactory(Builder builder) {
        this.className = builder.className;
        this.messager = builder.messager;
        this.parameterSpec = builder.parameterSpec;
        this.typeUtils = builder.typeUtils;
        this.elementUtils = builder.elementUtils;
        /**
         * 生成：public void getParameter(Object targetParameter)
         */
        method = MethodSpec.methodBuilder(ProcessorConst.PARAMETER_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameterSpec);
        callMirror = elementUtils.getTypeElement(ProcessorConst.CALL_INTERFACE).asType();
    }

    /**
     * 只有一行
     * Personal_MainActivity t = (Personal_MainActivity) targetParameter;
     */
    public void addFirstStatement() {
        method.addStatement("$T t = ($T)" + ProcessorConst.PARAMS, className, className);
    }

    public MethodSpec build() {
        return method.build();
    }

    public void buildStatement(Element element) {
        //element自描述
        TypeMirror typeMirror = element.asType();

        //得到类型的序列号 int object bool等
        int type = typeMirror.getKind().ordinal();

        String fieldName = element.getSimpleName().toString();

        String finalValue = "t." + fieldName;
        String methodContent = finalValue + " = t.getIntent().";

        if (type == TypeKind.INT.ordinal()) {
            // todo t.s = t.getIntent().getIntExtra("age", t.age);
            methodContent += "getIntExtra($S," + finalValue + ")";
        } else if (type == TypeKind.BOOLEAN.ordinal()) {
            // todo t.s = t.getIntent().getBooleanExtra("isSuccess", t.age);
            methodContent += "getBooleanExtra($S, " + finalValue + ")";  // 有默认值
        } else {
            // todo t.s = t.getIntent.getStringExtra("s");
            if (typeMirror.toString().equalsIgnoreCase(ProcessorConst.STRING)) {
                // String类型
                methodContent += "getStringExtra($S)"; // 没有默认值
            } else if (typeUtils.isSubtype(typeMirror,callMirror)) {//OrderDrawable类型
                //得到注解上的值
                String annotationValue = element.getAnnotation(Parameter.class).annotationValue();
                // t.orderDrawable = (OrderDrawable) RouterManager.getInstance().build("/order/getDrawable").navigation(t);
                methodContent = "t." + fieldName + " = ($T) $T.getInstance().build($S).navigation(t)";
                method.addStatement(methodContent,
                        TypeName.get(typeMirror),
                        ClassName.get(ProcessorConst.ROUTERMANAGERPKG,ProcessorConst.ROUTERMANAGER),
                        annotationValue);
                return;
            } else {
                //序列化对象
                methodContent = "t.getIntent().getSerializableExtra($S)";
            }
        }
        method.addStatement(methodContent, fieldName);
    }

    public static class Builder {
        // Messager用来报告错误，警告和其他提示信息
        private Messager messager;

        // 类名，如：MainActivity
        private ClassName className;

        // 方法参数体
        private ParameterSpec parameterSpec;

        // 操作Element工具类 (类、函数、属性都是Element)
        private Elements elementUtils;

        // type(类信息)工具类，包含用于操作TypeMirror的工具方法
        private Types typeUtils;

        public Builder() {
        }

        public Builder setElementUtils(Elements elementUtils) {
            this.elementUtils = elementUtils;
            return this;
        }

        public Builder setTypeUtils(Types typeUtils) {
            this.typeUtils = typeUtils;
            return this;
        }

        public Builder setMessager(Messager messager) {
            this.messager = messager;
            return this;
        }

        public Builder setClassName(ClassName className) {
            this.className = className;
            return this;
        }

        public Builder setParameterSpec(ParameterSpec parameterSpec) {
            this.parameterSpec = parameterSpec;
            return this;
        }

        public ParameterFactory build() {
            if (parameterSpec == null) {
                throw new IllegalArgumentException("parameterSpec方法参数体为空");
            }

            if (className == null) {
                throw new IllegalArgumentException("方法内容中的className为空");
            }

            if (messager == null) {
                throw new IllegalArgumentException("messager为空，Messager用来报告错误、警告和其他提示信息");
            }
            return new ParameterFactory(this);
        }
    }
}

