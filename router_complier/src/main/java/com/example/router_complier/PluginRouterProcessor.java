package com.example.router_complier;

import com.example.router_annotaions.PluginRouter;
import com.example.router_annotaions.bean.RouterBean;
import com.example.router_complier.consts.ProcessorConst;
import com.example.router_complier.utils.ProcessorUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

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
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

//todo 在META-INF中注册该注解处理器，类似于四大组件需要注册一样
@AutoService(Processor.class)
@SupportedAnnotationTypes({ProcessorConst.AROUTER_PACKAGE})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
//todo 接收从gradle编译时传过来的参数
@SupportedOptions({ProcessorConst.MODULENAME, ProcessorConst.APT_PACKAGE_DIR})
public class PluginRouterProcessor extends AbstractProcessor {

    //操作Element的工具类
    private Elements elementTool;

    //类型工具
    private Types typeTool;

    //messager编译时打印日志的工具
    private Messager messager;

    //编译时生成文件的工具
    private Filer filer;

    //用于接收gradle传递的参数
    private String aptPackageDir;
    private String moduleName;

    //eachModuleBean：保存(moduleName)每个组件中被注解修饰的(Activity Bean)集合
    //eachModule：保存每个module的(名字)和对应(实现类字符串)
    private Map<String, List<RouterBean>> eachModuleBean = new HashMap<>();
    private Map<String, String> eachModule = new HashMap<>();

    private static final int SPLITWITHSPACENUM = 1;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementTool = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        typeTool = processingEnv.getTypeUtils();

        moduleName = processingEnv.getOptions().get(ProcessorConst.MODULENAME);
        aptPackageDir = processingEnv.getOptions().get(ProcessorConst.APT_PACKAGE_DIR);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>>>>>moduleName:" + moduleName);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>>>>>aptPackageDir:" + aptPackageDir);
        if (moduleName != null && aptPackageDir != null) {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT>>>>>>>>>>>>>>OK");
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT>>>>>>>>>>>>>>FAILED");
        }
    }

    /**
     * 处理注解的函数
     *
     * @param set              每个组件中被注解修饰的TypeElement集合
     * @param roundEnvironment
     * @return 返回true时 执行完方法后会再检查一遍 看是否注解已经处理完了
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "没有发现被@PluginRouter注解的类");
            return false;
        }

        //todo 新增点call
        TypeElement callType = elementTool.getTypeElement(ProcessorConst.CALL_INTERFACE);
        TypeMirror callMirror = callType.asType();

        //得到被注解修饰的集合
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(PluginRouter.class);
        TypeElement activityType = elementTool.getTypeElement(ProcessorConst.ACTIVITY_PACKAGE);
        TypeMirror activityMirror = activityType.asType();

        for (Element element : elements) {
            //获取包名
            String packageName = elementTool.getPackageOf(element).getQualifiedName().toString();
            //获取简单类名
            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, packageName + "中的" + className + "被@PluginRouter修饰了");
            //获取注解
            PluginRouter pluginRouter = element.getAnnotation(PluginRouter.class);
            //创建bean
            RouterBean routerBean = new RouterBean.Builder()
                    .addClassName(pluginRouter.className())
                    .addElement(element)
                    .addModuleName(pluginRouter.moduleName())
                    .build();
            TypeMirror typeMirror = element.asType();
            if (typeTool.isSubtype(typeMirror, activityMirror)) {//@PluginRouter修饰了Activity及其子类
                //设置type为activity
                routerBean.setRouterType(RouterBean.RouterType.ACTIVITY);
            } else if (typeTool.isSubtype(typeMirror, callMirror)) {//@PluginRouter修饰了Call及其子类
                //todo 新增点call
                routerBean.setRouterType(RouterBean.RouterType.CALL);
            } else {
                //todo something else
                throw new RuntimeException("@PluginRouter注解目前仅限用于Activity类之上");
            }
            //万一用户没有在注解上声明className和moduleName时，给用户补全参数
            if (ProcessorUtils.isEmpty(routerBean.getClassName())) {
                routerBean.setModuleName("/" + moduleName + "/" + className);
            }
            if (ProcessorUtils.isEmpty(routerBean.getModuleName())) {
                routerBean.setModuleName(moduleName);
            }
            //检查bean中参数的合规性
            if (checkRouterPath(routerBean)) {
                messager.printMessage(Diagnostic.Kind.NOTE, "RouterBean Check Success>>>>>>>>>>>>" + routerBean.toString());
                //检查eachModuleBean的map中有没有缓存
                List<RouterBean> beanList = eachModuleBean.get(routerBean.getModuleName());
                if (ProcessorUtils.isEmpty(beanList)) {//无缓存
                    beanList = new ArrayList<>();
                    beanList.add(routerBean);
                    eachModuleBean.put(routerBean.getModuleName(), beanList);
                } else {
                    beanList.add(routerBean);
                }
            } else {
                messager.printMessage(Diagnostic.Kind.ERROR, "@PluginRouter注解未按规范配置，如：/app/MainActivity");
            }
        }
        //todo for循环结束意味着保存了该组件下所有的bean了

        //分别得到两个接口类型
        TypeElement IRouterModuleType = elementTool.getTypeElement(ProcessorConst.INETRFACE_OF_IROUTERMODULE);
        TypeElement IRouterClassType = elementTool.getTypeElement(ProcessorConst.INETRFACE_OF_IROUTERCLASS);

        try {
            createImplOfIRouterClass(IRouterClassType);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            createImplOfIRouterModule(IRouterModuleType, IRouterClassType);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * public class ARouter$$Group$$order implements ARouterGroup {
     *
     * @param iRouterModuleType
     * @param iRouterClassType
     * @throws IOException
     * @Override public Map<String, Class<? extends ARouterPath>> getGroupMap() {
     * Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
     * groupMap.put("order", ARouter$$Path$$order.class);       // 寻找Path
     * return groupMap;
     * }
     * }
     */
    private void createImplOfIRouterModule(TypeElement iRouterModuleType, TypeElement iRouterClassType) throws IOException {

        if (ProcessorUtils.isEmpty(eachModule) || ProcessorUtils.isEmpty(eachModuleBean)) {
            return;
        }

        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(iRouterClassType)))
        );

        MethodSpec.Builder methodBuidler = MethodSpec.methodBuilder(ProcessorConst.GETMODULEMAP)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(methodReturns);

        methodBuidler.addStatement("$T<$T, $T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(iRouterClassType))),
                ProcessorConst.MODULE_VAR1,
                ClassName.get(HashMap.class));

        methodBuidler.addStatement("$N.put($S, $T.class)",
                ProcessorConst.MODULE_VAR1,
                moduleName,
                ClassName.get(aptPackageDir, eachModule.get(moduleName)));

        methodBuidler.addStatement("return $N", ProcessorConst.MODULE_VAR1);

        String finalClassName = ProcessorConst.MODULE_FILE_NAME + moduleName;

        messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由组Group类文件：" +
                aptPackageDir + "." + finalClassName);

        // 生成类文件：ARouter$$Group$$app
        JavaFile.builder(aptPackageDir, // 包名
                TypeSpec.classBuilder(finalClassName) // 类名
                        .addSuperinterface(ClassName.get(iRouterModuleType)) // 实现ARouterLoadGroup接口 implements ARouterGroup
                        .addModifiers(Modifier.PUBLIC) // public修饰符
                        .addMethod(methodBuidler.build()) // 方法的构建（方法参数 + 方法体）
                        .build()) // 类构建完成
                .build() // JavaFile构建完成
                .writeTo(filer); // 文件生成器开始生成类文件
    }

    /**
     * 创建IRouterClass的实现类
     * public class ARouter$$Path$$order implements ARouterPath {
     *
     * @param iRouterClassType
     * @Override public Map<String, RouterBean> getPathMap() {
     * Map<String, RouterBean> pathMap = new HashMap<>();
     * pathMap.put("/order/Order_MainActivity",
     * RouterBean.create(RouterBean.TypeEnum.ACTIVITY,
     * Order_MainActivity.class,
     * "/order/Order_MainActivity",
     * "order"
     * ));
     * pathMap.put("/order/Order_MainActivity2",
     * RouterBean.create(RouterBean.TypeEnum.ACTIVITY,
     * Order_MainActivity.class,
     * "/order/Order_MainActivity",
     * "order"
     * ));
     * return pathMap;
     * }
     * }
     */
    private void createImplOfIRouterClass(TypeElement iRouterClassType) throws IOException {
        //如果bean缓存为空 则说明没有地方使用注解直接返回
        if (ProcessorUtils.isEmpty(eachModuleBean)) {
            return;
        }
        // 方法返回值 Map<String, RouterBean>
        TypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouterBean.class)
        );

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConst.GETJUMPPATHMAP)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(methodReturn);

        methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouterBean.class),
                ProcessorConst.PATH_VAR1,
                ClassName.get(HashMap.class));

        List<RouterBean> beanList = eachModuleBean.get(moduleName);
        for (RouterBean routerBean : beanList) {
            methodBuilder.addStatement("$N.put($S, $T.create($T.$L, $T.class, $S, $S))",
                    ProcessorConst.PATH_VAR1,
                    routerBean.getClassName(),
                    ClassName.get(RouterBean.class),
                    ClassName.get(RouterBean.RouterType.class),
                    routerBean.getRouterType(),
                    ClassName.get((TypeElement) routerBean.getElement()),
                    routerBean.getClassName(),
                    routerBean.getModuleName());
        }

        methodBuilder.addStatement("return $N", ProcessorConst.PATH_VAR1);

        String finalClassName = ProcessorConst.PATH_FILE_NAME + moduleName;

        messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由Path类文件：" +
                aptPackageDir + "." + finalClassName);

        // 生成类文件：ARouter$$Path$$personal
        JavaFile.builder(aptPackageDir, // 包名  APT 存放的路径
                TypeSpec.classBuilder(finalClassName) // 类名
                        .addSuperinterface(ClassName.get(iRouterClassType)) // 实现ARouterLoadPath接口  implements ARouterPath==pathType
                        .addModifiers(Modifier.PUBLIC) // public修饰符
                        .addMethod(methodBuilder.build()) // 方法的构建（方法参数 + 方法体）
                        .build()) // 类构建完成
                .build() // JavaFile构建完成
                .writeTo(filer); // 文件生成器开始生成类文件

        eachModule.put(moduleName, finalClassName);
    }

    /**
     * 校验bean中的参数是否合规
     * 因为bean中的className和moduleName都是使用注解获取的
     * 然而用户可能没有在注解上赋值，进而bean中没有获取到真正的值
     *
     * @param routerBean 待检测的bean
     * @return
     */
    private boolean checkRouterPath(RouterBean routerBean) {
        String mn = routerBean.getModuleName();
        String cm = routerBean.getClassName();
        if (ProcessorUtils.isEmpty(mn) || ProcessorUtils.isEmpty(cm)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "bean中className和ModuleName设置失败");
            return false;
        }
        if (!cm.startsWith("/")) {
            messager.printMessage(Diagnostic.Kind.ERROR, "注解中className必须是</模块名/类名>为格式");
            return false;
        }
        if (cm.split(" ").length != SPLITWITHSPACENUM) {
            messager.printMessage(Diagnostic.Kind.ERROR, "注解中className不能有空格");
            return false;
        }
        if (!mn.equals(moduleName)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "moduleName获取错误");
            return false;
        }
        //todo 还可以做更加严谨的其他校验 后期补充

        return true;
    }
}