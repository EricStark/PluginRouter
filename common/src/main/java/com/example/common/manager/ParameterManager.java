package com.example.common.manager;

import android.app.Activity;
import android.util.LruCache;

import com.example.common.inter.IParameterGet;

/**
 * 第一步：查找 Personal_MainActivity$$Parameter
 * 第二步：使用 Personal_MainActivity$$Parameter  this 给他
 */
public class ParameterManager {

    private static volatile ParameterManager instance;

    //缓存 key：类 value：接口实现
    private LruCache<String, IParameterGet> lruCache;

    private static final String FILE_SUFFIX_NAME = "$$Parameter";

    private ParameterManager() {
        lruCache = new LruCache<>(10);
    }

    public static ParameterManager getInstance() {
        if (instance == null) {
            synchronized (ParameterManager.class) {
                if (instance == null) {
                    instance = new ParameterManager();
                }
            }
        }
        return instance;
    }

    /**
     * 这个load方法需要用户在Activity中手动调用 传进来this
     * 通过this对象查找对应的实现类，从而调用方法给该Activity
     * 的参数赋值
     *
     * @param activity
     */
    public void loadParameter(Activity activity) {
        //得到类名
        String className = activity.getClass().getName();

        //先从缓存中获取
        IParameterGet iParameterGet = lruCache.get(className);
        if (iParameterGet == null) {
            Class<?> aClass = null;
            try {
                aClass = Class.forName(className + FILE_SUFFIX_NAME);

                iParameterGet = (IParameterGet) aClass.newInstance();
                //设置缓存
                lruCache.put(className, iParameterGet);
                //调用方法设置Activity参数
                iParameterGet.getParameter(activity);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }
}
