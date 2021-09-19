package com.example.router_complier;

import com.example.router_annotaions.Parameter;
import com.example.router_complier.consts.ProcessorConst;
import com.example.router_complier.utils.ProcessorUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes({ProcessorConst.PARAMETER_PACKAGE})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ParameterProcessor extends AbstractProcessor {

    private Elements elementUtils; // 类信息
    private Types typeUtils;  // 具体类型
    private Messager messager; // 日志
    private Filer filer;  // 文件生成器

    //map用来存放被注解修饰的参数 key：类节点 如MainActivity  value：被修饰的属性集合
    private Map<TypeElement, List<Element>> tempParameterMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        if (elementUtils != null && typeUtils != null && messager != null && filer != null) {
            messager.printMessage(Diagnostic.Kind.NOTE, "init ParameterProcessor>>>>>>>>OK");
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "init ParameterProcessor>>>>>>>>FAILED");
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "并没有发现 被@ARouter注解的属性");
            return false; // 没有机会处理
        }

        if (!ProcessorUtils.isEmpty(set)) {
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Parameter.class);
            //todo 遍历注解修饰的属性集合 并且设置缓存
            for (Element element : elements) {

                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

                Name s1 = enclosingElement.getSimpleName();
                Name s2 = element.getSimpleName();
                messager.printMessage(Diagnostic.Kind.NOTE, s1 + "类中" + s2 + "属性被@Parameter修饰");

                if (tempParameterMap.containsKey(enclosingElement)) {
                    tempParameterMap.get(enclosingElement).add(element);
                } else {
                    List<Element> fields = new ArrayList<>();
                    fields.add(element);
                    tempParameterMap.put(enclosingElement, fields);
                }
            }
            //todo for 循环结束意味着此Activity中所有被注解修饰的属性都保存了
            if (ProcessorUtils.isEmpty(tempParameterMap)) return true;

            //得到接口类型
            TypeElement IParameterGetType = elementUtils.getTypeElement(ProcessorConst.PARAMETER_INTERFACE);
            //得到activity类型 todo 用于后续判断注解是否在activity中使用
            TypeElement activityType = elementUtils.getTypeElement(ProcessorConst.ACTIVITY_PACKAGE);
            //生成函数形参 (Object targetParameter)
            ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT, ProcessorConst.PARAMS).build();

            for (Map.Entry<TypeElement, List<Element>> entry : tempParameterMap.entrySet()) {
                TypeElement typeElement = entry.getKey();
                //注解是在activity中使用的
                if (typeUtils.isSubtype(typeElement.asType(), activityType.asType())) {
                    //MainActivity
                    ClassName className = ClassName.get(typeElement);

                    ParameterFactory factory = new ParameterFactory.Builder()
                            .setParameterSpec(parameterSpec)
                            .setClassName(className)
                            .setMessager(messager)
                            .setElementUtils(elementUtils)
                            .setTypeUtils(typeUtils)
                            .build();
                    factory.addFirstStatement();
                    //循环每个Activity中被注解修饰的属性
                    for (Element element : entry.getValue()) {
                        factory.buildStatement(element);
                    }

                    // 最终生成的类文件名（类名$$Parameter） 例如：Personal_MainActivity$$Parameter
                    String finalClassName = typeElement.getSimpleName() + ProcessorConst.PARAMETER_FILE_NAME;
                    messager.printMessage(Diagnostic.Kind.NOTE, "APT生成获取参数类文件：" +
                            className.packageName() + "." + finalClassName);

                    try {
                        JavaFile.builder(className.packageName(),
                                TypeSpec.classBuilder(finalClassName)
                                        .addSuperinterface(ClassName.get(IParameterGetType))
                                        .addModifiers(Modifier.PUBLIC)
                                        .addMethod(factory.build())
                                        .build())
                                .build()
                                .writeTo(filer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }
}
