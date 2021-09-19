package com.example.common.inter;

import java.util.Map;

/**
 * 定义合规
 * 这个接口可以通过module名，找到对应的IRouterClass的实现
 */
public interface IRouterModule {

    //kay：模块名  value：对应模块中唯一的接口实现类
    Map<String, Class<? extends IRouterClass>> getModuleMap();
}
