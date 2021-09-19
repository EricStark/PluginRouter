package com.example.common.inter;

import com.example.router_annotaions.bean.RouterBean;

import java.util.Map;

public interface IRouterClass {

    //key：被注解修饰的activity  value：对应的javabean
    Map<String, RouterBean> getJumpPathMap();
}
