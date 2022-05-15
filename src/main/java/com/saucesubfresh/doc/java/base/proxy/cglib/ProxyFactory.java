package com.saucesubfresh.doc.java.base.proxy.cglib;

import net.sf.cglib.proxy.Enhancer;

/**
 * @author lijunping on 2021/8/27
 */
public class ProxyFactory {

    /**
     * 实例化动态代理对象
     * @param tClass 代理目标对象类型
     * @param <T> 代理目标对象类型泛型
     * @return 动态代理对象
     */
    public static <T> T getProxyInstance(Class<T> tClass) {
        return getProxyInstance(tClass, tClass);
    }

    /**
     * 实例化动态代理对象，并转换为对应接口类型
     * @param tClass 代理目标对象类型
     * @param interfaceClass 代理目标对象接口类型
     * @param <T> 代理目标对象接口类型泛型
     * @return 动态代理对象
     */
    public static <T> T getProxyInstance(Class<? extends T> tClass, Class<T> interfaceClass) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(tClass);
        enhancer.setCallback(CglibProxy.callback); // 实现了 MethodInterceptor 接口的类
        return interfaceClass.cast(enhancer.create());
    }
}
