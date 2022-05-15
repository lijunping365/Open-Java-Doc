package com.saucesubfresh.doc.java.base.proxy.cglib;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author lijunping on 2021/8/27
 */
public class CglibProxy implements MethodInterceptor {

    static CglibProxy callback = new CglibProxy();

    private CglibProxy() {
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {

        // 方法调用返回结果
        Object result = null;

        // 直接调用方法
        result = proxy.invokeSuper(obj, args);

        return result;
    }

    /**
     * 实例化动态代理对象，并转换为对应类型
     * @param tClass 代理目标对象类型
     * @param <T> 代理目标对象类型泛型
     * @return 动态代理对象
     */
    public static <T> T getProxyInstance(Class<? extends T> tClass) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(tClass);
        enhancer.setCallback(callback);
        return tClass.cast(enhancer.create());
    }
}
