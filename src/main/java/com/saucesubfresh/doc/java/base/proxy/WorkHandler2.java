package com.saucesubfresh.doc.java.base.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author: 李俊平
 * @Date: 2021-04-18 13:37
 */
public class WorkHandler2 implements InvocationHandler {

  private Object obj;

  public WorkHandler2(Object obj) {
    this.obj = obj;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    System.out.println("before 动态代理...");
    System.out.println(proxy.getClass().getName());
    System.out.println(this.obj.getClass().getName());
    if(method.getName().equals("work")) {
      method.invoke(this.obj, args);
      System.out.println("after 动态代理...");
      return proxy;
    } else {
      System.out.println("after 动态代理...");
      return method.invoke(this.obj, args);
    }
  }

}
