package com.example.router_complier.consts;

public interface ProcessorConst {

    /**
     * @ARouter注解 的 包名 + 类名
     */
    String AROUTER_PACKAGE = "com.example.router_annotaions.PluginRouter";
    /**
     * 从gradle 接收参数的TAG标记
     */
    String MODULENAME = "moduleName";

    String APT_PACKAGE_DIR = "packageNameForAPT";

    /**
     * Activity的全类名
     */
    String ACTIVITY_PACKAGE = "android.app.Activity";

    String ROUTER_API_PACKAGE = "com.example.common.inter";

    String INETRFACE_OF_IROUTERMODULE = ROUTER_API_PACKAGE + ".IRouterModule";

    String INETRFACE_OF_IROUTERCLASS = ROUTER_API_PACKAGE + ".IRouterClass";

    String GETJUMPPATHMAP = "getJumpPathMap";

    String GETMODULEMAP = "getModuleMap";

    String PATH_VAR1 = "pathMap";

    String MODULE_VAR1 = "moduleMap";

    String PATH_FILE_NAME = "PluginRouter$$PathImpl$$";

    String MODULE_FILE_NAME = "PluginRouter$$ModuleImpl$$";

    String PARAMETER_INTERFACE = "com.example.common.inter.IParameterGet";

    String PARAMETER_PACKAGE =  "com.example.router_annotaions.Parameter";

    String PARAMS = "targetParameter";

    // ARouter api 的 ParmeterGet 方法的名字
    String PARAMETER_METHOD_NAME = "getParameter";

    // String全类名
    String STRING = "java.lang.String";

    // ARouter aip 的 ParmeterGet 的 生成文件名称 $$Parameter
    String PARAMETER_FILE_NAME = "$$Parameter";

    //Call的全路径
    String CALL_INTERFACE = "com.example.common.inter.Call";

    String ROUTERMANAGERPKG = "com.example.common.manager";

    String ROUTERMANAGER = ".RouterManager";
}
