package com.example.common.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import com.example.common.inter.Call;
import com.example.common.inter.IRouterClass;
import com.example.common.inter.IRouterModule;
import com.example.router_annotaions.bean.RouterBean;

/**
 * 类目标实现：
 * 第一步：查找 module的 map 通过module名字找到对应组件的实现类
 * 第二部：得到组件对应的实现类后，得到jump的 map 找到对应Activity的class对象
 * 第三步：实现跳转
 */
public class RouterManager {

    //模块名
    private String module;
    //要跳转的Activity的path==/module/className
    private String path;

    //定义两个缓存分别保存两个接口的实现，提高性能
    private LruCache<String, IRouterClass> IRouterClassImplLruCache;
    private LruCache<String, IRouterModule> IRouterModuleImplLruCache;

    //接口实现类对应的前缀
    private final static String PATH_FILE_NAME = "PluginRouter$$PathImpl$$";
    private final static String MODULE_FILE_NAME = "PluginRouter$$ModuleImpl$$";
    private static volatile RouterManager instance = null;

    private RouterManager() {
        IRouterClassImplLruCache = new LruCache<>(10);
        IRouterModuleImplLruCache = new LruCache<>(10);
    }

    public static RouterManager getInstance() {
        if (instance == null) {
            synchronized (RouterManager.class) {
                if (instance == null) {
                    instance = new RouterManager();
                }
            }
        }
        return instance;
    }

    /**
     * 通过 模块名/类名创建对应的BundleManager，用于Activity之间传递参数
     *
     * @param path 模块名/类名
     * @return BundleManager
     */
    public BundleManager buildBundle(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new IllegalArgumentException("failed arguments!");
        }
        //todo 此处可以添加更加严谨的入参合规判断

        //得到模块名
        String module = path.substring(1, path.indexOf("/", 1));

        if (TextUtils.isEmpty(module)) {
            throw new IllegalArgumentException("unCorrect module name!");
        }

        this.module = module;//app
        this.path = path;//app/MainActivity

        return new BundleManager();
    }


    public Object navigationTo(Context context, BundleManager bundleManager) {

        String implPath = context.getPackageName() + "." + MODULE_FILE_NAME + module;
        Log.e("bohou >>>", "navigation: implPath=" + implPath);

        //先从缓存中获取iRouterModuleImpl对象
        IRouterModule iRouterModuleImpl = IRouterModuleImplLruCache.get(implPath);
        if (iRouterModuleImpl == null) {
            //通过类加载 得到class对象
            try {
                //todo 第一步 通过全类名得到 iRouterModuleImpl 对象
                Class<?> clazz = Class.forName(implPath);
                iRouterModuleImpl = (IRouterModule) clazz.newInstance();
                IRouterModuleImplLruCache.put(module, iRouterModuleImpl);

                //todo 第二步 通过 iRouterModuleImpl 得到对应的 iROouterClassImpl对象
                IRouterClass iRouterClassImpl = IRouterClassImplLruCache.get(path);
                if (iRouterClassImpl == null) {
                    Class<? extends IRouterClass> mClazz = iRouterModuleImpl.getModuleMap().get(module);
                    iRouterClassImpl = mClazz.newInstance();
                    IRouterClassImplLruCache.put(path, iRouterClassImpl);
                }
                //todo 第三步 执行跳转
                if (iRouterClassImpl.getJumpPathMap().isEmpty()) {
                    throw new RuntimeException("路由表Path报废了...");
                }

                RouterBean routerBean = iRouterClassImpl.getJumpPathMap().get(path);
                if (routerBean != null) {
                    switch (routerBean.getRouterType()){
                        case ACTIVITY:
                            Intent intent = new Intent(context, routerBean.getaClass());
                            intent.putExtras(bundleManager.getBundle());
                            context.startActivity(intent);
                            break;
                        case CALL:
                            Class<?> aClass = routerBean.getaClass();
                            Call impl = (Call) aClass.newInstance();
                            bundleManager.setCall(impl);
                            return bundleManager.getCall();
                        default:
                            break;
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
